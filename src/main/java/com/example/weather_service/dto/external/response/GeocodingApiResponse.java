package com.example.weather_service.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingApiResponse {
    private List<Result> results;
    private double generationtime_ms;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private int id;
        private String name;
        private double latitude;
        private double longitude;
        private String country;
        private String admin1; // State/Province etc.
    }
}
