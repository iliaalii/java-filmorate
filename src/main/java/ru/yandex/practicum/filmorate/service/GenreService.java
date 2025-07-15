package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreRepository storage;


    public Genre findGenre(final int id) {
        log.info("Обрабатываем запрос на поиск жанра");
        return Optional.ofNullable(findAllGenre().get(id))
                .orElseThrow(() -> new NotFoundException("Жанр с id " + id + " не найден"));
    }

    @Cacheable("allGenres")
    public Map<Integer, Genre> findAllGenre() {
        log.info("Обрабатываем запрос на поиск всех жанров");
        return storage.findAllGenre();
    }

    public Map<Integer, Set<Genre>> findAllGenresByFilms(final List<Integer> films) {
        return storage.findAllGenresByFilms(films);
    }

    public void saveFilmGenres(final Film film) {
        storage.saveFilmGenre(film);
    }

    public void validateGenres(final Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        film.getGenres().stream()
                .map(Genre::getId)
                .forEach(this::findGenre);
    }
}