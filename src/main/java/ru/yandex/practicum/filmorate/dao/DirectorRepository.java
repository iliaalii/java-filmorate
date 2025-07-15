package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.DataConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectorRepository {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Directors WHERE director_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM Directors";
    private static final String CREATE_QUERY = "INSERT INTO Directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE Directors SET name = ? WHERE director_id = ?";
    private static final String REMOVE_QUERY = "DELETE FROM Directors WHERE director_id = ?";
    private static final String FIND_ALL_DIRECTOR_QUERY = "SELECT d.*, fd.film_id FROM Directors d " +
            "JOIN Film_Directors fd ON d.director_id = fd.director_id";
    private static final String FIND_DIRECTOR_BY_FILM_QUERY = "SELECT d.* FROM Directors d " +
            "JOIN Film_Directors fd ON d.director_id = fd.director_id WHERE fd.film_id = ?";
    private static final String FIND_ALL_FILM_SORT_BY_YEAR =
            "SELECT f.* FROM Films f " +
                    "JOIN Film_Directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
    private static final String FIND_ALL_FILM_SORT_BY_LIKES =
            "SELECT f.* FROM Films f " +
                    "LEFT JOIN (SELECT film_id, COUNT(user_id) AS likes_count FROM Likes GROUP BY film_id) l " +
                    "ON f.film_id = l.film_id " +
                    "JOIN Film_Directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY l.likes_count DESC";

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorMapper;
    private final FilmRowMapper filmMapper;

    public Director findDirector(int id) {
        try {
            log.info("Поиск режиссера по id: {}", id);
            return jdbc.queryForObject(FIND_BY_ID_QUERY, directorMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Указанный режиссер не найден!");
        }
    }

    public Collection<Director> findAllDirectors() {
        log.info("Поиск всех доступных режиссеров");
        return jdbc.query(FIND_ALL_QUERY, directorMapper);
    }

    public Director create(Director director) {
        log.info("Добавляем нового режиссера");
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(CREATE_QUERY, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, director.getName());
                return ps;
            }, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }
        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            director.setId(id);
            log.info("Создан режиссер с id: {}", id);
            return director;
        } else {
            throw new DataConflictException("Не удалось сохранить данные");
        }
    }

    public Director update(Director newDirector) {
        log.info("Обновляем режиссера");
        try {
            int rowsUpdated = jdbc.update(UPDATE_QUERY,
                    newDirector.getName(),
                    newDirector.getId());

            if (rowsUpdated == 0) {
                throw new NotFoundException("Режиссер с id=" + newDirector.getId() + " не найден");
            }

            log.info("Обновлен режиссер под id: {}", newDirector.getId());
            return findDirector(newDirector.getId());
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }
    }

    public void removeDirector(int directorId) {
        log.info("Удаление режиссера");
        try {
            jdbc.update(REMOVE_QUERY, directorId);
            log.info("Режиссер удален");
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }
    }

    public void saveFilmDirectors(final Film film) {
        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            String ins = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
            for (Director d : film.getDirectors()) {
                jdbc.update(ins, film.getId(), d.getId());
            }
        }
        log.info("Обновлен список режиссеров фильма (id): {}", film.getId());
    }

    public Collection<Film> sortDirectorByYear(int directorId) {
        log.info("Поиск всех фильмов одного режиссера отсортированный по году");
        return jdbc.query(FIND_ALL_FILM_SORT_BY_YEAR, filmMapper, directorId);
    }

    public Collection<Film> sortDirectorByLikes(int directorId) {
        log.info("Поиск всех фильмов одного режиссера отсортированный по лайкам");
        return jdbc.query(FIND_ALL_FILM_SORT_BY_LIKES, filmMapper, directorId);
    }

    public Map<Integer, Set<Director>> findAllDirectorsByFilms() {
        log.info("Поиск режиссеров для каждого фильма");
        return jdbc.query(FIND_ALL_DIRECTOR_QUERY, rs -> {
            Map<Integer, Set<Director>> map = new HashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("film_id");
                Director director = new Director();
                director.setId(rs.getInt("director_id"));
                director.setName(rs.getString("name"));
                map.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
            }
            return map;
        });
    }

    public Set<Director> findDirectorsFilm(int id) {
        log.info("Поиск режиссеров для фильма");
        return new HashSet<>(jdbc.query(FIND_DIRECTOR_BY_FILM_QUERY, directorMapper, id));
    }
}
