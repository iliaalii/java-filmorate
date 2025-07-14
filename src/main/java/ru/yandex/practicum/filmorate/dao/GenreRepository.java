package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreRepository {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Genres WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM Genres";
    private static final String FIND_ALL_GENRE_QUERY = "SELECT g.*, fg.film_id FROM Genres g " +
            "JOIN Films_Genres fg ON g.genre_id = fg.genre_id";
    private static final String FIND_GENRE_BY_FILM_QUERY = "SELECT g.* FROM Genres g " +
            "JOIN Films_Genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";
    private static final String CLEAR_GENRE_BY_FILM_QUERY = "DELETE FROM Films_Genres WHERE film_id = ?";
    private static final String ADD_GENRE_BY_FILM_QUERY = "INSERT INTO Films_Genres (film_id, genre_id) VALUES (?, ?)";

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

    public Set<Genre> findGenresFilm(final int id) {
        log.info("проводим поиск жанров для фильма");
        return new HashSet<>(jdbc.query(FIND_GENRE_BY_FILM_QUERY, mapper, id));
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
