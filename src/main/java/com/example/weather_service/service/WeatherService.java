package com.example.weather_service.service;

import com.example.weather_service.dto.external.response.GeocodingApiResponse;
import com.example.weather_service.dto.external.response.WeatherApiResponse;
import com.example.weather_service.dto.internal.SimpleWeatherDTO;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException; // Import RestClientException
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeatherService {

    // --- Logger ---
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.open-meteo.geocoding.url}")
    private String geocodingApiUrl;

    @Value("${api.open-meteo.forecast.url}")
    private String forecastApiUrl;

    // --- WMO Weather Code Mapping ---
    // (See: https://open-meteo.com/en/docs#weathervariables)
    private static final Map<Integer, String> WMO_CODES = new HashMap<>();
    static {
        WMO_CODES.put(0, "Clear sky");
        WMO_CODES.put(1, "Mainly clear");
        WMO_CODES.put(2, "Partly cloudy");
        WMO_CODES.put(3, "Overcast");
        WMO_CODES.put(45, "Fog");
        WMO_CODES.put(48, "Depositing rime fog");
        WMO_CODES.put(51, "Light drizzle");
        WMO_CODES.put(53, "Moderate drizzle");
        WMO_CODES.put(55, "Dense drizzle");
        WMO_CODES.put(56, "Light freezing drizzle");
        WMO_CODES.put(57, "Dense freezing drizzle");
        WMO_CODES.put(61, "Slight rain");
        WMO_CODES.put(63, "Moderate rain");
        WMO_CODES.put(65, "Heavy rain");
        WMO_CODES.put(66, "Light freezing rain");
        WMO_CODES.put(67, "Heavy freezing rain");
        WMO_CODES.put(71, "Slight snow fall");
        WMO_CODES.put(73, "Moderate snow fall");
        WMO_CODES.put(75, "Heavy snow fall");
        WMO_CODES.put(77, "Snow grains");
        WMO_CODES.put(80, "Slight rain showers");
        WMO_CODES.put(81, "Moderate rain showers");
        WMO_CODES.put(82, "Violent rain showers");
        WMO_CODES.put(85, "Slight snow showers");
        WMO_CODES.put(86, "Heavy snow showers");
        WMO_CODES.put(95, "Thunderstorm");
        WMO_CODES.put(96, "Thunderstorm with slight hail");
        WMO_CODES.put(99, "Thunderstorm with heavy hail");
    }
    // --- End WMO Mapping ---


    public SimpleWeatherDTO getWeatherForCity(String cityName) {
        // Step 1: Geocode city name to coordinates
        GeocodingApiResponse.Result cityCoordinates = getCoordinatesForCity(cityName);
        if (cityCoordinates == null) {
            log.warn("Could not find coordinates for city: {}", cityName);
            throw new IllegalArgumentException("Could not find coordinates for city: " + cityName);
        }
        log.info("Found coordinates for {}: Lat={}, Lon={}", cityName, cityCoordinates.getLatitude(), cityCoordinates.getLongitude());

        // Step 2: Use coordinates to get weather forecast
        WeatherApiResponse weatherResponse = getWeatherForecast(cityCoordinates.getLatitude(), cityCoordinates.getLongitude());

        // Step 3: Map external DTO to our internal SimpleWeatherDTO
        return mapToSimpleWeatherDTO(cityName, weatherResponse);
    }

    private GeocodingApiResponse.Result getCoordinatesForCity(String cityName) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(geocodingApiUrl)
                .queryParam("name", cityName)
                .queryParam("count", 1); // Only need the top result

        String url = uriBuilder.toUriString();
        log.debug("Calling Geocoding API: {}", url);

        try {
            GeocodingApiResponse response = restTemplate.getForObject(url, GeocodingApiResponse.class);

            if (response != null && response.getResults() != null && !response.getResults().isEmpty()) {
                return response.getResults().getFirst(); // Return the first (best) match
            } else {
                log.warn("Geocoding API returned no results for city: {}", cityName);
                return null; // City not found
            }
        } catch (HttpClientErrorException e) {
            log.error("Error calling Geocoding API for city {}: {} - {}", cityName, e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null; // Or throw a specific exception
        } catch (RestClientException e) {
            log.error("Network or other error calling Geocoding API for city {}: {}", cityName, e.getMessage(), e);
            return null; // Or throw a specific exception
        }
    }

    private WeatherApiResponse getWeatherForecast(double latitude, double longitude) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(forecastApiUrl)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("current", "temperature_2m,wind_speed_10m,weather_code") // Request specific current weather fields
                .queryParam("temperature_unit", "celsius")
                .queryParam("wind_speed_unit", "kmh")
                .queryParam("timezone", "auto"); // Automatically detect timezone

        String url = uriBuilder.toUriString();
        log.debug("Calling Weather Forecast API: {}", url);

        try {
            WeatherApiResponse response = restTemplate.getForObject(url, WeatherApiResponse.class);
            if (response == null || response.getCurrent() == null) {
                log.error("Weather API returned invalid response for coords Lat={}, Lon={}", latitude, longitude);
                throw new RuntimeException("Received invalid weather data from API.");
            }
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Error calling Weather API for coords Lat={}, Lon={}: {} - {}", latitude, longitude, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Error fetching weather data: " + e.getMessage());
        } catch (RestClientException e) {
            log.error("Network or other error calling Weather API for coords Lat={}, Lon={}: {}", latitude, longitude, e.getMessage(), e);
            throw new RuntimeException("Error fetching weather data: " + e.getMessage());
        }
    }

    private SimpleWeatherDTO mapToSimpleWeatherDTO(String cityName, WeatherApiResponse apiResponse) {
        WeatherApiResponse.Current current = apiResponse.getCurrent();

        // Map the WMO code to a description
        String conditionDescription = WMO_CODES.getOrDefault(current.getWeatherCode(), "Unknown code: " + current.getWeatherCode());

        return SimpleWeatherDTO.builder()
                .cityName(cityName) // Use the original city name requested
                .temperature(current.getTemperature2m())
                .windSpeed(current.getWindSpeed10m())
                .conditionDescription(conditionDescription)
                .lastUpdated(current.getTime()) // Use the timestamp from the API response
                .build();
    }
}

