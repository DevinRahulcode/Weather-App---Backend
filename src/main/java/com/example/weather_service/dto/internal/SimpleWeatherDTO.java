package com.example.weather_service.dto.internal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder // Using Builder pattern for easy creation in the service
public class SimpleWeatherDTO {
    private String cityName;
    private Double temperature; // Celsius
    private Double windSpeed;   // km/h
    private String conditionDescription; // e.g., "Clear sky" (needs mapping)
    private String lastUpdated; // Time from API
}
