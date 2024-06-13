package com.bezkoder.spring.security.postgresql.controllers;

import com.bezkoder.spring.security.postgresql.Dto.FavoriteDto;
import com.bezkoder.spring.security.postgresql.models.Favorite;
import com.bezkoder.spring.security.postgresql.service.FavoriteServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")

public class FavoriteController {
@Autowired
private FavoriteServiceImp favoriteService;
    @GetMapping("/favoritesForCurrentUser")
    public ResponseEntity<List<FavoriteDto>> getFavoritesForCurrentUser() {
        List<Favorite> favorites = favoriteService.getFavoritesForCurrentUser();
        List<FavoriteDto> favoriteDtos = favorites.stream()
                .map(favoriteService::mapFavoriteToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().body(favoriteDtos);
    }
    @GetMapping
    public ResponseEntity<List<Favorite>> getAllFavorites() {
        return ResponseEntity.ok(favoriteService.getAllFavorites());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Favorite> getFavoriteById(@PathVariable Long id) {
        return ResponseEntity.ok(favoriteService.getFavoriteById(id));
    }
    @PostMapping
    public ResponseEntity<Favorite> saveFavorite(@RequestBody Favorite favorite) {
        return ResponseEntity.ok(favoriteService.saveFavorite(favorite));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFavorite(@PathVariable Long id) {
        favoriteService.deleteFavorite(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/markQuestionAsFavorite/{questionId}")
    public ResponseEntity<Favorite> markQuestionAsFavorite(@PathVariable Long questionId) {
        return ResponseEntity.ok(favoriteService.markQuestionAsFavorite(questionId));
    }

    @PostMapping("/markAnswerAsFavorite/{answerId}")
    public ResponseEntity<Favorite> markAnswerAsFavorite(@PathVariable Long answerId) {
        return ResponseEntity.ok(favoriteService.markAnswerAsFavorite(answerId));
    }

    @PostMapping("/markAnswerResponseAsFavorite/{answerResponseId}")
    public ResponseEntity<Favorite> markAnswerResponseAsFavorite(@PathVariable Long answerResponseId) {
        return ResponseEntity.ok(favoriteService.markAnswerResponseAsFavorite(answerResponseId));
    }
}
