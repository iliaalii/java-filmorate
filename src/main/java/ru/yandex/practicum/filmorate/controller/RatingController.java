package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Validated
public class RatingController {
    private final RatingService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Rating> findAll() {
        return service.findAllRating();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Rating findRating(@PathVariable @Positive int id) {
        return service.findRating(id);
    }
}
