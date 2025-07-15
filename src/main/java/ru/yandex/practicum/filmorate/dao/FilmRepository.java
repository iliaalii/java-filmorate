package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.DataConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.*;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FilmRepository implements FilmStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM Films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Films WHERE film_id = ?";
    private static final String CREATE_QUERY = "INSERT INTO Films (name, description, release_date, duration," +
            " rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE Films SET name = ?,description = ?, release_date = ?," +
            " duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "MERGE INTO Likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM Likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKE_BY_FILM_QUERY = "SELECT user_id FROM Likes WHERE film_id = ?";
    private static final String FIND_ALL_LIKES = "SELECT film_id, user_id FROM Likes";
    private static final String REMOVE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String GET_COMMON_FILMS = "SELECT f.film_id, f.name, f.description, f.release_date," +
            " f.duration, f.rating_id FROM Films f" +
            " JOIN Likes l1 ON f.film_id = l1.film_id AND l1.user_id = ?" +
            " JOIN Likes l2 ON f.film_id = l2.film_id AND l2.user_id = ?" +
            " ORDER BY (SELECT COUNT(*) FROM Likes l WHERE l.film_id = f.film_id) DESC";
    private static final String GET_POPULAR_FILMS = "SELECT f.film_id, f.name, f.description, f.release_date," +
            " f.duration, f.rating_id, COUNT(l.user_id) AS likes_count FROM Films AS f " +
            "LEFT JOIN Likes l ON f.film_id = l.film_id LEFT JOIN Films_Genres fg ON f.film_id = fg.film_id " +
            "LEFT JOIN Genres g ON fg.genre_id = g.genre_id " +
            "WHERE (? IS NULL OR g.genre_id = ?) AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?) " +
            "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id " +
            "ORDER BY likes_count DESC LIMIT ?";
    private static final String RECOMMEND_FILMS_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id " +
                    "FROM Films f " +
                    "JOIN Likes l ON f.film_id = l.film_id " +
                    "LEFT JOIN Likes l2 ON l2.film_id = f.film_id AND l2.user_id = ? " +
                    "WHERE l.user_id = ( " +
                    "SELECT l2.user_id FROM Likes l1 JOIN Likes l2 ON l1.film_id = l2.film_id " +
                    "WHERE l1.user_id = ? AND l2.user_id != ? GROUP BY l2.user_id ORDER BY COUNT(*) DESC LIMIT 1) " +
                    "AND l2.film_id IS NULL";

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final RatingRepository ratingStorage;

    @Override
    public Collection<Film> findAll() {
        log.info("Поиск всех фильмов");
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public Film findFilm(int id) {
        try {
            log.info("Поиск фильма с id: {}", id);
            Film film = jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
            assert film != null;
            film.setLikes(findLikeFilm(id));
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                film.setMpa(findRatingsFilm(film.getMpa().getId()));
            }
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
        if (id == null) {
            throw new DataConflictException("Не удалось сохранить данные");
        }
        film.setId(id);
        log.info("Создан фильм с id: {}", id);
        return film;
    }


    @Override
    public Film update(Film newFilm) {
        log.info("Обновляем фильм");
        try {
            int rowsUpdated = jdbc.update(UPDATE_QUERY,
                    newFilm.getName(),
                    newFilm.getDescription(),
                    Date.valueOf(newFilm.getReleaseDate()),
                    newFilm.getDuration(),
                    newFilm.getMpa() != null ? newFilm.getMpa().getId() : null,
                    newFilm.getId());
            if (rowsUpdated == 0) {
                throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
            }
            log.info("Обновлен фильм под id: {}", newFilm.getId());
            return findFilm(newFilm.getId());
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД: " + e.getMessage());
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

    public Collection<Film> recommendFilms(final long userId) {
        log.trace("Запрос рекомендаций для пользователя с id: {}", userId);
        try {
            return jdbc.query(RECOMMEND_FILMS_QUERY, mapper, userId, userId, userId);
        } catch (EmptyResultDataAccessException e) {
            log.trace("Рекомендаций для пользователя с id: {} не найдено.", userId);
            return List.of();
        }
    }

    public void removeFilm(int filmId) {
        int affected = jdbc.update(REMOVE_FILM_QUERY, filmId);
        if (affected == 0) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        log.info("Фильм с {filmId}: {} был удалён", filmId);
    }

    public Collection<Film> getCommonFilms(final int id, final int friendId) {
        log.trace("Получение общих фильмов из базы данных.");
        return jdbc.query(GET_COMMON_FILMS, mapper, id, friendId);
    }

    public Collection<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        return jdbc.query(GET_POPULAR_FILMS, mapper, genreId, genreId, year, year, count);
    }

    private Set<Integer> findLikeFilm(int id) {
        log.info("Поиск лайков фильма (id): {}", id);
        return new HashSet<>(jdbc.query(FIND_LIKE_BY_FILM_QUERY,
                (rs, rowNum) -> rs.getInt("user_id"), id));
    }

    public Map<Integer, Set<Integer>> findAllLikes() {
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
        return ratingStorage.findRating(id);
    }

    @Override
    public List<Film> search(String query, List<String> by) {
        boolean byTitle = by.contains("title");
        boolean byDirector = by.contains("director");
        if (!byTitle && !byDirector) {
            throw new IllegalArgumentException("Параметр by должен содержать 'title' или 'director'");
        }

        StringBuilder sql = new StringBuilder(
                "SELECT f.*, COUNT(l.user_id) AS likes_count " +
                        "FROM Films f " +
                        "LEFT JOIN Likes l ON f.film_id = l.film_id "
        );
        if (byDirector) {
            sql.append("LEFT JOIN Film_Directors fd ON f.film_id = fd.film_id ")
                    .append("LEFT JOIN Directors d ON d.director_id = fd.director_id ");
        }
        sql.append("WHERE ");

        List<Object> params = new ArrayList<>();
        String p = "%" + query.toLowerCase() + "%";
        List<String> cond = new ArrayList<>();
        if (byTitle) {
            cond.add("LOWER(f.name) LIKE ?");
            params.add(p);
        }
        if (byDirector) {
            cond.add("LOWER(d.name) LIKE ?");
            params.add(p);
        }
        sql.append(String.join(" OR ", cond));
        sql.append(" GROUP BY f.film_id ORDER BY likes_count DESC");

        return jdbc.query(sql.toString(), mapper, params.toArray());
    }
}