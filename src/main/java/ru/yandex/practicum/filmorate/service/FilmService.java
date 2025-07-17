package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventService eventService;
    private final GenreService genreService;
    private final RatingService ratingService;
    private final DirectorService directorService;

    public Collection<Film> findAll() {
        log.info("Обрабатываем запрос на поиск всех фильмов");
        return enrichFilms(filmStorage.findAll());
    }

    public Film findFilm(final int id) {
        log.info("Обрабатываем запрос на поиск фильма (id): {}", id);
        return enrichFilm(filmStorage.findFilm(id));
    }

    public Film create(final Film film) {
        log.info("Обрабатываем запрос на добавление фильма");
        validationFilm(film);

        Film createdFilm = filmStorage.create(film);

        genreService.saveFilmGenres(createdFilm);
        directorService.saveFilmDirectors(createdFilm);

        return enrichFilm(createdFilm);
    }

    public Film update(final Film newFilm) {
        log.info("Обрабатываем запрос на обновление фильма");
        validationFilm(newFilm);

        genreService.saveFilmGenres(newFilm);
        directorService.saveFilmDirectors(newFilm);
        Film updatedFilm = filmStorage.update(newFilm);

        return enrichFilm(updatedFilm);
    }

    public void removeFilm(final int filmId) {
        log.info("Обрабатываем запрос на удаление фильма (filmId): {}", filmId);
        filmStorage.removeFilm(filmId);
    }

    public void addLike(final int id, final int userId) {
        log.info("Обрабатываем запрос на выставление лайка фильму (id): {}, от пользователя (id): {}", id, userId);
        if (userStorage.findUser(userId) != null && filmStorage.findFilm(id) != null) {
            filmStorage.addLike(id, userId);
            eventService.createNowEvent(userId, id, EventType.LIKE, OperationType.ADD);
        }
    }

    public void removeLike(final int id, final int userId) {
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

    public Collection<Film> getRecommendFilms(final long userId) {
        log.trace("Подбираются рекомендации для пользователя с id: {}.", userId);
        return enrichFilms(filmStorage.recommendFilms(userId));
    }

    public Collection<Film> getDirectorFilmsSorted(final int directorId, final String sortBy) {
        return enrichFilms(directorService.getDirectorFilmsSorted(directorId, sortBy));
    }

    private void validationFilm(final Film film) {
        log.info("Проводим проверку валидности");
        ratingService.validateRating(film);
        genreService.validateGenres(film);
        directorService.validateDirectors(film);
    }

    public Collection<Film> search(final String query, final String by) {
        log.info("Поиск фильмов по '{}' в: {}", query, by);
        var criteria = Arrays.stream(by.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
        return enrichFilms(filmStorage.search(query, criteria));
    }

    public Film enrichFilm(final Film film) {
        return enrichFilms(List.of(film)).iterator().next();
    }

    public Collection<Film> enrichFilms(final Collection<Film> films) {
        List<Integer> filmIds = films.stream().map(Film::getId).toList();
        Map<Integer, Set<Genre>> genresFilms = genreService.findAllGenresByFilms(filmIds);
        Map<Integer, Rating> ratingsFilms = ratingService.findAllRatingsByFilms(filmIds);
        Map<Integer, Set<Director>> directorsFilms = directorService.findAllDirectorsByFilms(filmIds);
        Map<Integer, Set<Integer>> likesFilms = filmStorage.findAllLikes(filmIds);

        for (Film film : films) {
            film.setGenres(genresFilms.getOrDefault(film.getId(), Set.of()));
            film.setMpa(ratingsFilms.get(film.getId()));
            film.setDirectors(directorsFilms.getOrDefault(film.getId(), Set.of()));
            film.setLikes(likesFilms.getOrDefault(film.getId(), Set.of()));
        }
        return films;
    }
}
