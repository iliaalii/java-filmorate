package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final LocalDate cinemaBirthday = LocalDate.of(1895, Month.DECEMBER, 28);

    @GetMapping
    public Collection<Film> findAll() {
        return List.copyOf(films.values());
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(cinemaBirthday)) {
            log.warn("Ошибка в дате выхода фильма при добавлении: {}", film.getReleaseDate());
            throw new ValidationException("Данные не проходят проверку:\n" +
                    "-дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        film.setId(getNextId());
        log.info("В список добавлен новый фильм: {}", film);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации: не указан id при обновлении фильма");
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getReleaseDate().isBefore(cinemaBirthday)) {
                log.warn("Ошибка в дате выхода фильма при обновлении: {}", newFilm.getReleaseDate());
                throw new ValidationException("Данные не проходят проверку:\n" +
                        "-дата релиза — не раньше 28 декабря 1895 года.");
            }
            log.info("Обновлены данные фильма: {}, на новые: {}", oldFilm, newFilm);
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        }
        log.warn("Ошибка: фильм с id = {} не найден для обновления", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}