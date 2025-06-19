package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;


import java.util.Collection;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Genres WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM Genres";

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
}