package com.example.weather_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class FavoriteCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cityName;

    // We could store lat/lon here too to avoid geocoding every time,
    // but for simplicity, we'll geocode on demand.
     private Double latitude;
     private Double longitude;

    public FavoriteCity(String cityName) {
        this.cityName = cityName;
    }
}
