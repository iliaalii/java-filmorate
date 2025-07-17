package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Validated
public class DirectorController {
    private final DirectorService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Director> findAll() {
        return service.findAllDirector().values();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Director findDirector(@PathVariable @Positive int id) {
        return service.findDirector(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@RequestBody @Valid Director director) {
        return service.create(director);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Director update(@RequestBody Director newDirector) {
        return service.update(newDirector);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDirector(@PathVariable("id") @Positive int id) {
        service.removeDirector(id);
    }
}
