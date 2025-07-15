package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreRepository storage;

    public Genre findGenre(int id) {
        log.info("Обрабатываем запрос на поиск жанра");
        return storage.findGenre(id);
    }

    public Collection<Genre> findAllGenre() {
        log.info("Обрабатываем запрос на поиск всех жанров");
        return storage.findAllGenre();
    }

    public Map<Integer, Set<Genre>> findAllGenresByFilms() {
        return storage.findAllGenresByFilms();
    }

    public Set<Genre> findGenresFilm(final int id) {
        return storage.findGenresFilm(id);
    }

    public void saveFilmGenres(final Film film) {
        storage.saveFilmGenre(film);
    }

    public void validateGenres(final Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreId = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            Set<Integer> validGenreIds = storage.findAllGenre().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            genreId.removeAll(validGenreIds);

            if (!genreId.isEmpty()) {
                throw new NotFoundException("Указаны не существующие жанры: " + genreId);
            }
        }
    }
}