package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.DataConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.*;
import java.sql.Date;
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

    private static final String FIND_ALL_RATINGS_BY_FILMS = "SELECT f.film_id, r.rating_id, r.name FROM Films f " +
            "JOIN Rating r ON f.rating_id = r.rating_id";

    private static final String ADD_LIKE_QUERY = "INSERT INTO Likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM Likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKE_BY_FILM_QUERY = "SELECT user_id FROM Likes WHERE film_id = ?";
    private static final String FIND_ALL_LIKES = "SELECT film_id, user_id FROM Likes";

    private static final String CLEAR_GENRE_BY_FILM_QUERY = "DELETE FROM Films_Genres WHERE film_id = ?";
    private static final String ADD_GENRE_BY_FILM_QUERY = "INSERT INTO Films_Genres (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_GENRE_BY_FILM_QUERY = "SELECT g.* FROM Genres g " +
            "JOIN Films_Genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";
    private static final String FIND_ALL_GENRE_QUERY = "SELECT g.*, fg.film_id FROM Genres g " +
            "JOIN Films_Genres fg ON g.genre_id = fg.genre_id";

    private static final String GET_COMMON_FILMS = "SELECT f.film_id, f.name, f.description, f.release_date," +
            " f.duration, f.rating_id FROM Films f" +
            " JOIN Likes l1 ON f.film_id = l1.film_id AND l1.user_id = ?" +
            " JOIN Likes l2 ON f.film_id = l2.film_id AND l2.user_id = ?" +
            " ORDER BY (SELECT COUNT(*) FROM Likes l WHERE l.film_id = f.film_id) DESC";

    @Override
    public Collection<Film> findAll() {
        log.info("Поиск всех фильмов");
        Collection<Film> films = jdbc.query(FIND_ALL_QUERY, mapper);

        Map<Integer, Set<Integer>> likesByFilm = findAllLikes();
        Map<Integer, Set<Genre>> genresByFilmS = findAllGenresByFilms();
        Map<Integer, Rating> ratingByFilm = findAllRatingsByFilm();

        for (Film film : films) {
            film.setGenres(genresByFilmS.getOrDefault(film.getId(), Set.of()));
            film.setLikes(likesByFilm.getOrDefault(film.getId(), Set.of()));
            film.setMpa(ratingByFilm.getOrDefault(film.getId(), null));
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

        try {
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
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }


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
        try {
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
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }
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

    public List<Film> getCommonFilms(final int id, final int friendId) {
        log.trace("Получение общих фильмов из базы данных.");
        return jdbc.query(GET_COMMON_FILMS, mapper, id, friendId);
    }

    private void setFilmGenres(Integer filmId, Set<Genre> genres) {
        jdbc.update(CLEAR_GENRE_BY_FILM_QUERY, filmId);
        jdbc.batchUpdate(ADD_GENRE_BY_FILM_QUERY, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Genre genre = genres.stream().toList().get(i);
                ps.setInt(1, filmId);
                ps.setInt(2, genre.getId());
            }

            public int getBatchSize() {
                return genres.size();
            }
        });
        log.info("Обновлен список жанров фильма (id): {}", filmId);
    }

    private Map<Integer, Set<Genre>> findAllGenresByFilms() {
        log.info("Поиск жанров для каждого фильма");
        return jdbc.query(FIND_ALL_GENRE_QUERY, rs -> {
            Map<Integer, Set<Genre>> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return map;
        });
    }

    private Set<Integer> findLikeFilm(int id) {
        log.info("Поиск лайков фильма (id): {}", id);
        return new HashSet<>(jdbc.query(FIND_LIKE_BY_FILM_QUERY,
                (rs, rowNum) -> rs.getInt("user_id"), id));
    }

    private Map<Integer, Set<Integer>> findAllLikes() {
        log.info("Поиск лайков для каждого фильма");
        return jdbc.query(FIND_ALL_LIKES, rs -> {
            Map<Integer, Set<Integer>> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                int userId = rs.getInt("user_id");
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
            }
            return map;
        });
    }

    private Rating findRatingsFilm(int id) {
        log.info("Отправляем запрос рейтинга для фильма");
        return rStorage.findRating(id);
    }

    private Map<Integer, Rating> findAllRatingsByFilm() {
        log.info("Поиск рейтинга для всех фильмов");
        return jdbc.query(FIND_ALL_RATINGS_BY_FILMS, rs -> {
            Map<Integer, Rating> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Rating rating = new Rating();
                rating.setId(rs.getInt("rating_id"));
                rating.setName(rs.getString("name"));
                map.put(filmId, rating);
            }
            return map;
        });
    }

    private Set<Genre> findGenresFilm(int id) {
        log.info("проводим поиск жанров для фильма");
        return new HashSet<>(jdbc.query(FIND_GENRE_BY_FILM_QUERY, new GenreRowMapper(), id));
    }
}