package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.DataConflictException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM Users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM Users WHERE user_id = ?";
    private static final String CREATE_QUERY = "INSERT INTO Users (login, name, email, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE Users SET login = ?, name = ?, email = ?, birthday = ? " +
            "WHERE user_id = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO Friends (user_id, friend_id) VALUES (?, ?)";
    private static final String REMOVE_FRIEND_QUERY = "DELETE FROM Friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT u.* FROM Users u " +
            "WHERE u.user_id IN (SELECT friend_id FROM Friends f WHERE f.user_id = ?)";
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT u.* FROM Friends f1 " +
            "INNER JOIN Friends f2 ON f1.friend_id = f2.friend_id " +
            "INNER JOIN Users u ON f1.friend_id = u.user_id " +
            "WHERE f1.user_id = ? AND f2.user_id = ?";
    private static final String REMOVE_USER_QUERY = "DELETE FROM Users WHERE user_id = ?";

    @Override
    public Collection<User> findAll() {
        log.info("Поиск всех фильмов");
        return jdbc.query(FIND_ALL_QUERY, userRowMapper);
    }

    @Override
    public User findUser(int id) {
        try {
            log.info("Поиск фильма по id: {}", id);
            return jdbc.queryForObject(FIND_BY_ID_QUERY, userRowMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("По указанному id (" + id + ") пользователь не обнаружен");
        }
    }

    @Override
    public User create(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbc.update(con -> {
                log.info("Добавляем нового пользователя");
                PreparedStatement ps = con.prepareStatement(CREATE_QUERY, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getLogin());
                if (user.getName() == null || user.getName().isBlank()) {
                    user.setName(user.getLogin());
                }
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.setDate(4, Date.valueOf(user.getBirthday()));
                return ps;
            }, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }


        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            user.setId(id);
            log.info("Добавлен пользователь с id: {}", id);
            return user;
        } else {
            throw new DataConflictException("Не удалось сохранить данные");
        }
    }

    @Override
    public User update(User newUser) {
        log.info("Обновляем пользователя");
        findUser(newUser.getId());
        try {
            int rowsUpdated = jdbc.update(UPDATE_QUERY,
                    newUser.getLogin(),
                    newUser.getName(),
                    newUser.getEmail(),
                    Date.valueOf(newUser.getBirthday()),
                    newUser.getId());
            if (rowsUpdated == 0) {
                throw new DataConflictException("Не удалось обновить данные");
            }
            return findUser(newUser.getId());
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Ошибка валидации при сохранении в БД");
        }
    }

    @Override
    public void addFriend(int id, int friendId) {
        try {
            jdbc.update(ADD_FRIEND_QUERY, id, friendId);
            log.info("Пользователь {} и {}, теперь друзья", id, friendId);
        } catch (DataAccessException e) {
            throw new DataConflictException("Дружба уже создана");
        }
    }

    @Override
    public void removeFriend(int id, int friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, id, friendId);
        log.info("Пользователи {} и {}, более не друзья", id, friendId);
    }

    @Override
    public Collection<User> findFriends(int id) {
        log.info("поиск всех друзей пользователя (id): {}", id);
        return jdbc.query(FIND_FRIENDS_QUERY, userRowMapper, id);
    }

    @Override
    public Collection<User> findCommonFriends(int id, int otherId) {
        log.info("Поиск общих друзей между {} и {}", id, otherId);
        return jdbc.query(FIND_COMMON_FRIENDS_QUERY, userRowMapper, id, otherId);
    }

    @Override
    public void removeUser(int id) {
        int affected = jdbc.update(REMOVE_USER_QUERY, id);
        if (affected == 0) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        log.info("Пользователь с {id}: {} был удалён", id);
    }
}
