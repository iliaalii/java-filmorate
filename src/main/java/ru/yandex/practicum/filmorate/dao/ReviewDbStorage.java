package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.DataConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ReviewDbStorage {
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private static final String ADD_QUERY = "INSERT INTO Reviews (film_id, user_id, content, is_positive) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE Reviews SET film_id = ?,user_id = ?, content = ?," +
            " is_positive = ? WHERE review_id = ?";
    private static final String REMOVE_QUERY = "DELETE FROM Reviews WHERE review_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT r.*, COALESCE(SUM(CASE WHEN rl.is_like = true THEN 1 " +
            "WHEN rl.is_like = false THEN -1 ELSE 0 END), 0) AS useful " +
            "FROM reviews r LEFT JOIN review_likes rl ON r.review_id = rl.review_id " +
            "WHERE r.review_id = ? GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id";
    private static final String FIND_ALL_QUERY = "SELECT r.*, COALESCE(SUM(CASE WHEN rl.is_like = true THEN 1 " +
            "WHEN rl.is_like = false THEN -1 ELSE 0 END), 0) AS useful " +
            "FROM reviews r LEFT JOIN review_likes rl ON r.review_id = rl.review_id " +
            "WHERE r.film_id = ? GROUP BY r.review_id, r.content, r.is_positive, r.user_id, r.film_id LIMIT ?";

    private static final String ADD_LIKE_QUERY = "MERGE INTO Review_Likes KEY(review_id, user_id) VALUES (?, ?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM Review_Likes WHERE review_id = ? AND user_id = ?";

    public Review add(Review review) {
        log.info("Добавляем новый обзор");
        checkRelevance(review);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(ADD_QUERY, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, review.getFilmId());
                ps.setInt(2, review.getUserId());
                ps.setString(3, review.getContent());
                ps.setBoolean(4, review.getIsPositive());
                return ps;
            }, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            review.setReviewId(id);
            log.info("Создан обзор с id: {}", id);
            return review;
        } else {
            throw new DataConflictException("Не удалось сохранить данные");
        }
    }

    public Review update(Review newReview) {
        log.info("Обновляем обзор");
        findById(newReview.getReviewId());
        checkRelevance(newReview);
        try {
            int rowsUpdated = jdbc.update(UPDATE_QUERY,
                    newReview.getFilmId(),
                    newReview.getUserId(),
                    newReview.getContent(),
                    newReview.getIsPositive(),
                    newReview.getReviewId());
            if (rowsUpdated == 0) {
                throw new DataConflictException("Не удалось обновить данные");
            }
            return findById(newReview.getReviewId());
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }
    }

    public void remove(int id) {
        jdbc.update(REMOVE_QUERY, id);
        log.info("Обзор под номером (id): {}, был удален", id);
    }

    public Review findById(int id) {
        try {
            log.info("Поиск обзора по id: {}", id);
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("По указанному id (" + id + ") обзор не обнаружен");
        }
    }

    public Collection<Review> findAll(Integer filmId, int count) {
        log.info("Поиск {} обзора(ов) фильма под номером: {}", count, filmId);
        return jdbc.query(FIND_ALL_QUERY, mapper, filmId, count);
    }

    public void addLike(int filmId, int userId) {
        log.info("Добавлен лайк фильму {} от пользователя {}", filmId, userId);
        jdbc.update(ADD_LIKE_QUERY, filmId, userId, true);
    }

    public void addDislike(int filmId, int userId) {
        log.info("Добавлен дизлайк фильму {} от пользователя {}", filmId, userId);
        jdbc.update(ADD_LIKE_QUERY, filmId, userId, false);
    }

    public void removeLike(int filmId, int userId) {
        log.info("Добавлен лайк фильму {} от пользователя {} был удален", filmId, userId);
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    public void removeDislike(int filmId, int userId) {
        log.info("Добавлен дизлайк фильму {} от пользователя {} был удален", filmId, userId);
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    private void checkRelevance(Review review) {
        log.info("Проводим проверку наличия пользователя и фильма");
        filmStorage.findFilm(review.getFilmId());
        userStorage.findUser(review.getUserId());
    }
}