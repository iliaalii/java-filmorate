package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage storage;

    public UserService(UserStorage userStorage) {
        this.storage = userStorage;
    }

    public Collection<User> findAll() {
        log.info("Обрабатываем запрос на поиск всех пользователей");
        return storage.findAll();
    }

    public User findUser(int id) {
        log.info("Обрабатываем запрос на поиск пользователя");
        return storage.findUser(id);
    }

    public User create(User user) {
        log.info("Обрабатываем запрос на добавление нового пользователя");
        return storage.create(user);
    }

    public User update(User newUser) {
        log.info("Обрабатываем запрос на обновление пользователя");
        return storage.update(newUser);
    }

    public void addFriend(int id, int friendId) {
        log.info("Обрабатываем запрос на добавление в друзья");
        if (storage.findUser(id) != null && storage.findUser(friendId) != null) {
            storage.addFriend(id, friendId);
        }
    }

    public void removeFriend(int id, int friendId) {
        log.info("Обрабатываем запрос на удаление из друзей");
        if (storage.findUser(id) != null && storage.findUser(friendId) != null) {
            storage.removeFriend(id, friendId);
        }
    }

    public Collection<User> findFriends(int id) {
        log.info("Обрабатываем запрос на поиск всех друзей пользователя");
        if (storage.findUser(id) != null) {
            return List.copyOf(storage.findFriends(id).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

    }

    public Collection<User> findCommonFriends(int id, int otherId) {
        log.info("Обрабатываем запрос на поиск общих друзей между пользователями");
        return storage.findCommonFriends(id, otherId);
    }

    public void removeUser(int id) {
        log.info("Обрабатываем запрос на удаление пользователя (id): {}", id);
        if (storage.findUser(id) != null) {
            storage.removeUser(id);
        } else {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}