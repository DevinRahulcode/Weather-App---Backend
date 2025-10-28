package com.example.weather_service.service;


import com.example.weather_service.dto.internal.FavoriteCityDTO;
import com.example.weather_service.entity.FavoriteCity;
import com.example.weather_service.repository.FavoriteCityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteCityService {

    @Autowired
    private FavoriteCityRepository repository;

    public List<FavoriteCityDTO> getAllFavorites() {
        return repository.findAll().stream()
                .map(city -> new FavoriteCityDTO(city.getId(), city.getCityName()))
                .collect(Collectors.toList());
    }

    public FavoriteCityDTO addFavorite(String cityName) {
        // Prevent duplicates (case-insensitive)
        Optional<FavoriteCity> existing = repository.findByCityNameIgnoreCase(cityName);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("City '" + cityName + "' is already a favorite.");
        }
        FavoriteCity newCity = new FavoriteCity(cityName);
        FavoriteCity savedCity = repository.save(newCity);
        return new FavoriteCityDTO(savedCity.getId(), savedCity.getCityName());
    }

    public void removeFavorite(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Favorite city with ID " + id + " not found.");
        }
        repository.deleteById(id);
    }
}