package com.example.weather_service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {
    private double latitude;
    private double longitude;
    @JsonProperty("generationtime_ms") // JSON field name is different
    private double generationTimeMs;
    @JsonProperty("utc_offset_seconds")
    private int utcOffsetSeconds;
    private String timezone;
    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;
    private double elevation;
    @JsonProperty("current_units")
    private CurrentUnits currentUnits;
    private Current current;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentUnits {
        private String time;
        private String interval;
        @JsonProperty("temperature_2m")
        private String temperature2m; // e.g., "°C"
        @JsonProperty("wind_speed_10m")
        private String windSpeed10m; // e.g., "km/h"
        @JsonProperty("weather_code")
        private String weatherCode; // Unit description (can ignore)
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        private String time; // e.g., "2025-10-28T13:00"
        private int interval;
        @JsonProperty("temperature_2m")
        private double temperature2m;
        @JsonProperty("wind_speed_10m")
        private double windSpeed10m;
        @JsonProperty("weather_code")
        private int weatherCode; // WMO Weather code (needs mapping)
    }
}