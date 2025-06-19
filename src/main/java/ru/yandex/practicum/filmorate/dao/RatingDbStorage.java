package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RatingDbStorage {
    private final JdbcTemplate jdbc;
    private final RatingRowMapper mapper;

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Rating  WHERE rating_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM rating ";

    public Rating findRating(int id) {
        try {
            log.info("Поиск рейтинга по id: {}", id);
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Указанный рейтинг не найден!");
        }
    }

    public Collection<Rating> findAllRating() {
        log.info("Поиск всех доступных рейтингов");
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }
}