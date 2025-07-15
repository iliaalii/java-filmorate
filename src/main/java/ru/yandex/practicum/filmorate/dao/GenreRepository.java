package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreRepository {
    private final NamedParameterJdbcTemplate namedJdbc;
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_ALL_GENRES_BY_FILMS = "SELECT g.*, fg.film_id FROM genres g " +
            "JOIN films_genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id IN (:filmIds)";
    private static final String CLEAR_GENRE_BY_FILM_QUERY = "DELETE FROM films_genres WHERE film_id = ?";
    private static final String ADD_GENRE_BY_FILM_QUERY = "INSERT INTO films_genres (film_id, genre_id) VALUES (?, ?)";


    public Map<Integer, Genre> findAllGenre() {
        log.info("Поиск всех доступных жанров");
        List<Genre> genres = jdbc.query(FIND_ALL_QUERY, mapper);
        return genres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));
    }

    public Map<Integer, Set<Genre>> findAllGenresByFilms(final List<Integer> filmIds) {
        log.info("Поиск жанров для фильмов: {}", filmIds);

        Map<String, Object> params = Map.of("filmIds", filmIds);

        return namedJdbc.query(FIND_ALL_GENRES_BY_FILMS, params, rs -> {
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

    public void saveFilmGenre(final Film film) {
        Set<Genre> genres = film.getGenres();
        jdbc.update(CLEAR_GENRE_BY_FILM_QUERY, film.getId());
        jdbc.batchUpdate(ADD_GENRE_BY_FILM_QUERY, new BatchPreparedStatementSetter() {
            public void setValues(@NonNull PreparedStatement ps, int i) throws SQLException {
                Genre genre = genres.stream().toList().get(i);
                ps.setInt(1, film.getId());
                ps.setInt(2, genre.getId());
            }

            public int getBatchSize() {
                return genres.size();
            }
        });
        log.info("Обновлен список жанров фильма (id): {}", film.getId());
    }
}
