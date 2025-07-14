package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.dao.GenreRepository;
import ru.yandex.practicum.filmorate.dao.RatingRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, Month.DECEMBER, 28);
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final RatingRepository ratingStorage;
    private final GenreRepository genreStorage;
    private final EventService eventService;
    private final DirectorRepository directorStorage;
    private final GenreService genreService;
    private final RatingService ratingService;
    private final DirectorService directorService;


    public Collection<Film> findAll() {
        log.info("Обрабатываем запрос на поиск всех фильмов");
        return enrichFilms(filmStorage.findAll());
    }

    public Film findFilm(int id) {
        log.info("Обрабатываем запрос на поиск фильма (id): {}", id);
        return enrichFilm(filmStorage.findFilm(id));
    }

    public Film create(Film film) {
        log.info("Обрабатываем запрос на добавление фильма");
        validationFilm(film);
        Film createdFilm = filmStorage.create(film);
        genreService.saveFilmGenre(createdFilm);
        return enrichFilm(createdFilm);
    }

    public Film update(Film newFilm) {
        log.info("Обрабатываем запрос на обновление фильма");
        validationFilm(newFilm);
        genreService.saveFilmGenre(newFilm);
        Film updatedFilm = filmStorage.update(newFilm);
        return enrichFilm(updatedFilm);
    }

    public void addLike(int id, int userId) {
        log.info("Обрабатываем запрос на выставление лайка фильму (id): {}, от пользователя (id): {}", id, userId);
        if (userStorage.findUser(userId) != null && filmStorage.findFilm(id) != null) {
            filmStorage.addLike(id, userId);
            eventService.createNowEvent(userId, id, EventType.LIKE, OperationType.ADD);
        }
    }

    public void removeLike(int id, int userId) {
        log.info("Обрабатываем запрос на удаление лайка фильму (id): {}, от пользователя (id): {}", id, userId);
        if (userStorage.findUser(userId) != null && filmStorage.findFilm(id) != null) {
            filmStorage.removeLike(id, userId);
            eventService.createNowEvent(userId, id, EventType.LIKE, OperationType.REMOVE);
        }
    }

    public Collection<Film> getPopularFilms(final Integer count, final Integer genreId, final Integer year) {
        log.info("Обрабатывается запрос популярных фильмов по жанру {} и/или году {}", genreId, year);
        return enrichFilms(filmStorage.getPopularFilms(count, genreId, year));
    }

    public Collection<Film> getCommonFilms(final int id, final int userId) {
        log.trace("Получение общих фильмов пользователей с id {} и {}.", id, userId);

        return enrichFilms(filmStorage.getCommonFilms(id, userId));
    }

    public void removeFilm(int filmId) {
        log.info("Обрабатываем запрос на удаление фильма (filmId): {}", filmId);
        filmStorage.removeFilm(filmId);
    }

    public Collection<Film> recommendFilms(final long userId) {
        log.trace("Подбираются рекомендации для пользователя с id: {}.", userId);
        return enrichFilms(filmStorage.recommendFilms(userId));
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

    public Collection<Film> sortDirector(int directorId, String sortBy) {
        if (directorStorage.findDirector(directorId) == null) {
            throw new NotFoundException("Режиссер отсутствует");
        }
        if (sortBy.equals("year")) {
            log.info("Проводим сортировку фильмов по году");
            return enrichFilms(filmStorage.sortDirectorByYear(directorId));
        } else if (sortBy.equals("likes")) {
            log.info("Проводим сортировку фильмов по лайкам");
            return enrichFilms(filmStorage.sortDirectorByLikes(directorId));
        } else {
            throw new ValidationException("Неверный параметр sortBy: " + sortBy);
        }

    }

    public Collection<Film> search(String query, String by) {
        log.info("Поиск фильмов по '{}' в: {}", query, by);
        var criteria = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        return enrichFilms(filmStorage.search(query, criteria));
    }

    public Collection<Film> enrichFilms(final Collection<Film> films) {
        Map<Integer, Rating> ratingMap = ratingService.findAllRatingsByFilm();
        Map<Integer, Set<Genre>> genresByFilmS = genreService.findAllGenresByFilms();
        Map<Integer, Set<Director>> directorsMap = directorService.findAllDirectorsByFilms();
        Map<Integer, Set<Integer>> likesMap = filmStorage.findAllLikes();

        for (Film film : films) {
            film.setGenres(genresByFilmS.getOrDefault(film.getId(), Set.of()));
            film.setMpa(ratingMap.getOrDefault(film.getId(), null));
            film.setDirectors(directorsMap.getOrDefault(film.getId(), Set.of()));
            film.setLikes(likesMap.getOrDefault(film.getId(), Set.of()));
        }
        return films;
    }

    public Film enrichFilm(final Film film) {
        film.setDirectors(directorService.findDirectorsFilm(film.getId()));
        film.setGenres(genreService.findGenresFilm(film.getId()));
        return film;
    }
}

