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
    private static final String FIND_ALL_GENRE_QUERY = "SELECT g.*, fg.film_id FROM Genres g " +
            "JOIN Films_Genres fg ON g.genre_id = fg.genre_id";

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

    public Map<Integer, Set<Genre>> findAllGenresByFilms() {
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
}
