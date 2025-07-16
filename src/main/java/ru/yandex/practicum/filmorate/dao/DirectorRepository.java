package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectorRepository {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors";
    private static final String CREATE_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String REMOVE_QUERY = "DELETE FROM directors WHERE director_id = ?";
    private static final String FIND_ALL_DIRECTORS_BY_FILMS = "SELECT d.*, fd.film_id FROM directors d " +
            "JOIN film_directors fd ON d.director_id = fd.director_id WHERE fd.film_id IN (:filmIds)";
    private static final String FIND_ALL_DIRECTORS_BY_FILM = "SELECT director_id FROM directors " +
            "WHERE director_id IN (:ids)";
    private static final String FIND_ALL_FILM_SORT_BY_YEAR =
            "SELECT f.* FROM films f " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";
    private static final String FIND_ALL_FILM_SORT_BY_LIKES =
            "SELECT f.* FROM films f " +
                    "LEFT JOIN (SELECT film_id, COUNT(user_id) AS likes_count FROM likes GROUP BY film_id) l " +
                    "ON f.film_id = l.film_id " +
                    "JOIN film_directors fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY l.likes_count DESC";

    private final NamedParameterJdbcTemplate namedJdbc;
    private final DirectorRowMapper directorMapper;
    private final FilmRowMapper filmMapper;

    public Director findDirector(final int id) {
        try {
            log.info("Поиск режиссера по id: {}", id);
            return namedJdbc.getJdbcOperations().queryForObject(FIND_BY_ID_QUERY, directorMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Указанный режиссер не найден!");
        }
    }

    public Map<Integer, Director> findAllDirectors() {
        log.info("Поиск всех доступных режиссеров");
        List<Director> directors = namedJdbc.getJdbcOperations().query(FIND_ALL_QUERY, directorMapper);

        return directors.stream()
                .collect(Collectors.toMap(Director::getId, Function.identity()));
    }

    public Director create(final Director director) {
        log.info("Добавляем нового режиссера");
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            namedJdbc.getJdbcOperations().update(con -> {
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

    public Director update(final Director newDirector) {
        log.info("Обновляем режиссера");
        try {
            int rowsUpdated = namedJdbc.getJdbcOperations().update(UPDATE_QUERY,
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

    public void removeDirector(final int directorId) {
        log.info("Удаление режиссера");
        try {
            namedJdbc.getJdbcOperations().update(REMOVE_QUERY, directorId);
            log.info("Режиссер удален");
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }
    }

    public void saveFilmDirectors(final Film film) {
        namedJdbc.getJdbcOperations().update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Director> directors = film.getDirectors();
            String ins = "INSERT INTO film_directors (film_id, director_id) VALUES (:filmId, :directorId)";

            List<MapSqlParameterSource> batchParams = directors.stream()
                    .map(d -> new MapSqlParameterSource()
                            .addValue("filmId", film.getId())
                            .addValue("directorId", d.getId()))
                    .toList();

            namedJdbc.batchUpdate(ins, batchParams.toArray(new SqlParameterSource[0]));
        }
        log.info("Обновлен список режиссеров фильма (id): {}", film.getId());
    }

    public Collection<Film> sortDirectorByYear(final int directorId) {
        log.info("Поиск всех фильмов одного режиссера отсортированный по году");
        return namedJdbc.getJdbcOperations().query(FIND_ALL_FILM_SORT_BY_YEAR, filmMapper, directorId);
    }

    public Collection<Film> sortDirectorByLikes(final int directorId) {
        log.info("Поиск всех фильмов одного режиссера отсортированный по лайкам");
        return namedJdbc.getJdbcOperations().query(FIND_ALL_FILM_SORT_BY_LIKES, filmMapper, directorId);
    }

    public Map<Integer, Set<Director>> findAllDirectorsByFilms(final List<Integer> filmIds) {

        Map<String, Object> params = Map.of("filmIds", filmIds);

        return namedJdbc.query(FIND_ALL_DIRECTORS_BY_FILMS, params, rs -> {
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

    public List<Integer> findAllDirectorsByFilm(Set<Integer> ids) {
        Map<String, Object> params = Map.of("ids", ids);

        return namedJdbc.query(FIND_ALL_DIRECTORS_BY_FILM, params,
                (rs, rowNum) -> rs.getInt("director_id"));
    }
}
