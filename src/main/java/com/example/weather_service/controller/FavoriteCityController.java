package com.example.weather_service.controller;

import com.example.weather_service.dto.internal.FavoriteCityDTO;
import com.example.weather_service.service.FavoriteCityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // For handling errors

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@CrossOrigin(origins = "*") // Allow requests from any frontend (change for production)
public class FavoriteCityController {

    @Autowired
    private FavoriteCityService favoriteCityService;

    // GET /api/v1/favorites
    @GetMapping
    public ResponseEntity<List<FavoriteCityDTO>> getAllFavorites() {
        List<FavoriteCityDTO> favorites = favoriteCityService.getAllFavorites();
        return ResponseEntity.ok(favorites);
    }

    // POST /api/v1/favorites
    @PostMapping
    public ResponseEntity<FavoriteCityDTO> addFavorite(@RequestBody FavoriteCityDTO favoriteCityDTO) {
        if (favoriteCityDTO.getCityName() == null || favoriteCityDTO.getCityName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "City name cannot be empty");
        }
        try {
            FavoriteCityDTO addedCity = favoriteCityService.addFavorite(favoriteCityDTO.getCityName());
            return new ResponseEntity<>(addedCity, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Handle case where city is already a favorite
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // DELETE /api/v1/favorites/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long id) {
        try {
            favoriteCityService.removeFavorite(id);
            return ResponseEntity.noContent().build(); // Standard response for successful DELETE
        } catch (IllegalArgumentException e) {
            // Handle case where ID is not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}