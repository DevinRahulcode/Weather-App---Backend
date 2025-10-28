package com.example.weather_service.controller;

import com.example.weather_service.dto.internal.SimpleWeatherDTO;
import com.example.weather_service.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/weather")
@CrossOrigin(origins = "*") // Allow requests from any frontend (change for production)
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    // GET /api/v1/weather?city=London
    @GetMapping
    public ResponseEntity<SimpleWeatherDTO> getWeather(@RequestParam String city) {
        if (city == null || city.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City parameter is required");
        }
        try {
            SimpleWeatherDTO weather = weatherService.getWeatherForCity(city);
            return ResponseEntity.ok(weather);
        } catch (IllegalArgumentException e) {
            // Handle city not found or other errors from the service
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (RuntimeException e) {
            // Catch broader errors (like API failures)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve weather data: " + e.getMessage());
        }
    }
}