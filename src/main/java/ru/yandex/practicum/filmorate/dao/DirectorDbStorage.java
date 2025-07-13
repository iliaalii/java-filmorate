package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exception.DataConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DirectorDbStorage {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Directors WHERE director_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM Directors";
    private static final String CREATE_QUERY = "INSERT INTO Directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE Directors SET name = ? WHERE director_id = ?";
    private static final String REMOVE_QUERY = "DELETE FROM Directors WHERE director_id = ?";

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper mapper;

    public Director findDirector(int id) {
        try {
            log.info("Поиск режиссера по id: {}", id);
            return jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Указанный режиссер не найден!");
        }
    }

    public Collection<Director> findAllDirectors() {
        log.info("Поиск всех доступных режиссеров");
        return jdbc.query(FIND_ALL_QUERY, mapper);
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
}