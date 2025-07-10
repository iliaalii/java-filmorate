package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;


import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Genres WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM Genres";
    private static final String GET_FILM_GENRES = "SELECT g.genre_id, g.name FROM Film_Genres AS fg " +
            "JOIN Genres AS g ON fg.genre_id = g.genre_id WHERE film_id = ?";
    private static final String GET_FILMS_GENRES = "SELECT fg.film_id, g.genre_id, g.name " +
                                                   "FROM Films_Genres fg JOIN Genres g ON fg.genre_id = g.genre_id " +
                                                   "WHERE fg.film_id IN ";

    public Genre findGenre(int id) {
        try {
            log.info("Поиск жанра по id: {}", id);
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Указанный жанр не найден!");
        }
    }

    public Collection<Genre> findAllGenre() {
        log.info("Поиск всех доступных жанров");
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public Collection<Genre> getFilmsGenres(final int filmId) {
        return jdbc.query(GET_FILM_GENRES, mapper, filmId);
    }

    public Map<Integer, Set<Genre>> getFilmsGenres(List<Integer> filmsId) {
        if (filmsId.isEmpty()) {
            return Collections.emptyMap();
        }

        String idsInSql = filmsId.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", ", "(", ")"));

        return jdbc.query(GET_FILMS_GENRES + idsInSql, rs -> {
            Map<Integer,Set<Genre>> genresMap = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("name"));
                genresMap.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
            }
            return genresMap;
        }, filmsId.toArray());
    }
}