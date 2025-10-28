package com.example.weather_service.repository;

import com.example.weather_service.entity.FavoriteCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FavoriteCityRepository extends JpaRepository<FavoriteCity, Long> {
    // Finds a city ignoring case (e.g., "London" matches "london")
    Optional<FavoriteCity> findByCityNameIgnoreCase(String cityName);
}
