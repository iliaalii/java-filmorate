package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.RatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, Month.DECEMBER, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final RatingDbStorage ratingStorage;
    private final GenreDbStorage genreStorage;
    private final DirectorDbStorage directorStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       RatingDbStorage ratingStorage,
                       GenreDbStorage genreStorage,
                       DirectorDbStorage directorStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.ratingStorage = ratingStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
    }

    public Collection<Film> findAll() {
        log.info("Обрабатываем запрос на поиск всех фильмов");
        return filmStorage.findAll();
    }

    public Film findFilm(int id) {
        log.info("Обрабатываем запрос на поиск фильма (id): {}", id);
        return filmStorage.findFilm(id);
    }

    public Film create(Film film) {
        log.info("Обрабатываем запрос на добавление фильма");
        validationFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        log.info("Обрабатываем запрос на обновление фильма");
        validationFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public void addLike(int id, int userId) {
        log.info("Обрабатываем запрос на выставление лайка фильму (id): {}, от пользователя (id): {}", id, userId);
        if (userStorage.findUser(userId) != null && filmStorage.findFilm(id) != null) {
            filmStorage.addLike(id, userId);
        }
    }

    public void removeLike(int id, int userId) {
        log.info("Обрабатываем запрос на удаление лайка фильму (id): {}, от пользователя (id): {}", id, userId);
        if (userStorage.findUser(userId) != null && filmStorage.findFilm(id) != null) {
            filmStorage.removeLike(id, userId);
        }
    }

    public Collection<Film> getPopularFilms(Integer count) {
        log.info("Обрабатываем запрос на вывод популярных фильмов");
        return List.copyOf(filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList());
    }


    private void validationFilm(Film film) {
        log.info("Проводим проверку валидности");
        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Данные не проходят проверку: " +
                    "дата релиза должна быть не раньше 28 декабря 1895 года.");
        }

        if (film.getMpa() != null) {
            ratingStorage.findRating(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreId = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            Set<Integer> validGenreIds = genreStorage.findAllGenre().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            genreId.removeAll(validGenreIds);

            if (!genreId.isEmpty()) {
                throw new NotFoundException("Указаны не существующие жанры: " + genreId);
            }
        }
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director director : film.getDirectors()) {
                try {
                    directorStorage.findDirector(director.getId());
                } catch (NotFoundException e) {
                    throw new ValidationException("Указан несуществующий режиссер: " + director.getId());
                }
            }
        }
    }

    public Collection<Film> sortDirectorByYear(int directorId) {
        log.info("Проводим сортировку фильмов по году");
        return filmStorage.sortDirectorByYear(directorId);
    }

    public Collection<Film> sortDirectorByLikes(int directorId) {
        log.info("Проводим сортировку фильмов по лайкам");
        return filmStorage.sortDirectorByLikes(directorId);
    }
}