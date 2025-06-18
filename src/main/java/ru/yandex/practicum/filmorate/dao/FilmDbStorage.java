package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.DataConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final RatingDbStorage rStorage;

    private static final String FIND_ALL_QUERY = "SELECT * FROM Films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Films WHERE film_id = ?";
    private static final String CREATE_QUERY = "INSERT INTO Films (name, description, release_date, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE Films SET name = ?,description = ?, release_date = ?," +
            " duration = ?, rating_id = ? WHERE film_id = ?";

    private static final String ADD_LIKE_QUERY = "INSERT INTO Likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM Likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKE_BY_FILM_QUERY = "SELECT user_id FROM Likes WHERE film_id = ?";

    private static final String CLEAR_GENRE_BY_FILM_QUERY = "DELETE FROM Films_Genres WHERE film_id = ?";
    private static final String ADD_GENRE_BY_FILM_QUERY = "INSERT INTO Films_Genres (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_GENRE_BY_FILM_QUERY = "SELECT g.* FROM Genres g " +
            "JOIN Films_Genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";

    @Override
    public Collection<Film> findAll() {
        log.info("Поиск всех фильмов");
        Collection<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);
        for (Film f : films) {
            f.setLikes(findLikeFilm(f.getId()));
            if (f.getMpa() != null && f.getMpa().getId() != null) {
                f.setMpa(findRatingsFilm(f.getMpa().getId()));
            }
            f.setGenres(findGenresFilm(f.getId()));
        }
        return films;
    }

    @Override
    public Film findFilm(int id) {
        try {
            log.info("Поиск фильма с id: {}", id);
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            film.setLikes(findLikeFilm(id));
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                film.setMpa(findRatingsFilm(film.getMpa().getId()));
            }
            film.setGenres(findGenresFilm(film.getId()));
            return film;
        } catch (DataAccessException e) {
            throw new NotFoundException("По указанному id (" + id + ") фильм не обнаружен");
        }
    }

    @Override
    public Film create(Film film) {
        log.info("Добавляем новый фильм");
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(CREATE_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            if (film.getGenres() != null && !film.getGenres().isEmpty()) {
                setFilmGenres(id, film.getGenres());
            }
            film.setId(id);
            log.info("Создан фильм с id: {}", id);
            return film;
        } else {
            throw new DataConflictException("Не удалось сохранить данные");
        }
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Обновляем фильм");
        Integer ratingId = null;
        if (newFilm.getMpa() != null && newFilm.getMpa().getId() != null) {
            ratingId = newFilm.getMpa().getId();
        }
        int rowsUpdated = jdbc.update(UPDATE_QUERY,
                newFilm.getName(),
                newFilm.getDescription(),
                Date.valueOf(newFilm.getReleaseDate()),
                newFilm.getDuration(),
                ratingId,
                newFilm.getId());
        if (rowsUpdated == 0) {
            throw new DataConflictException("Не удалось обновить данные");
        }
        setFilmGenres(newFilm.getId(), newFilm.getGenres());
        log.info("Обновлен фильм под id: {}", newFilm.getId());
        return findFilm(newFilm.getId());
    }

    @Override
    public void addLike(int id, int userId) {
        try {
            jdbc.update(ADD_LIKE_QUERY, id, userId);
            log.info("Пользователь (id): {}, поставил лайк фильму (id): {}", userId, id);
        } catch (DataAccessException e) {
            throw new DataConflictException("Лайк уже поставлен");
        }
    }

    @Override
    public void removeLike(int id, int userId) {
        jdbc.update(REMOVE_LIKE_QUERY, id, userId);
        log.info("Пользователь (id): {}, убрал лайк фильму (id): {}", userId, id);
    }

    private void setFilmGenres(Integer filmId, Set<Genre> genres) {
        jdbc.update(CLEAR_GENRE_BY_FILM_QUERY, filmId);
        for (Genre genre : genres) {
            jdbc.update(ADD_GENRE_BY_FILM_QUERY, filmId, genre.getId());
        }
        log.info("Обновлен список жанров фильма (id): {}", filmId);
    }

    private Set<Integer> findLikeFilm(int id) {
        log.info("Поиск лайков фильма (id): {}", id);
        return new HashSet<>(jdbc.query(FIND_LIKE_BY_FILM_QUERY,
                (rs, rowNum) -> rs.getInt("user_id"), id));
    }

    private Rating findRatingsFilm(int id) {
        log.info("Отправляем запрос рейтинга для фильма");
        return rStorage.findRating(id);
    }

    private Set<Genre> findGenresFilm(int id) {
        log.info("проводим поиск жанров для фильма");
        return new HashSet<>(jdbc.query(FIND_GENRE_BY_FILM_QUERY, new GenreRowMapper(), id));
    }
}