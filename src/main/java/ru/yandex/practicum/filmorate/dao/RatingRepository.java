package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RatingRepository {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final JdbcTemplate jdbc;

    private final RatingRowMapper mapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM rating ";
    private static final String FIND_ALL_RATINGS_BY_FILMS = "SELECT f.film_id, r.rating_id, r.name FROM Films f " +
            "JOIN Rating r ON f.rating_id = r.rating_id WHERE f.film_id IN (:filmIds)";


    public Collection<Rating> findAllRating() {
        log.info("Поиск всех доступных рейтингов");
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public Map<Integer, Rating> findAllRatingsByFilm(final List<Integer> filmIds) {
        log.info("Поиск рейтингов для фильмов: {}", filmIds);

        Map<String, Object> params = Map.of("filmIds", filmIds);

        return namedJdbc.query(FIND_ALL_RATINGS_BY_FILMS, params, rs -> {
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
}