package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final EventService eventService;
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        log.info("Обрабатываем запрос на поиск всех пользователей");
        return userStorage.findAll();
    }

    public User findUser(final int id) {
        log.info("Обрабатываем запрос на поиск пользователя");
        return userStorage.findUser(id);
    }

    public User create(final User user) {
        log.info("Обрабатываем запрос на добавление нового пользователя");
        return userStorage.create(user);
    }

    public User update(final User newUser) {
        log.info("Обрабатываем запрос на обновление пользователя");
        return userStorage.update(newUser);
    }

    public void addFriend(final int id, final int friendId) {
        log.info("Обрабатываем запрос на добавление в друзья");
        if (userStorage.findUser(id) != null && userStorage.findUser(friendId) != null) {
            userStorage.addFriend(id, friendId);
            eventService.createNowEvent(id, friendId, EventType.FRIEND, OperationType.ADD);
        }
    }

    public void removeFriend(final int id, final int friendId) {
        log.info("Обрабатываем запрос на удаление из друзей");
        if (userStorage.findUser(id) != null && userStorage.findUser(friendId) != null) {
            userStorage.removeFriend(id, friendId);
            eventService.createNowEvent(id, friendId, EventType.FRIEND, OperationType.REMOVE);
        }
    }

    public Collection<User> findFriends(final int id) {
        log.info("Обрабатываем запрос на поиск всех друзей пользователя");
        if (userStorage.findUser(id) != null) {
            return List.copyOf(userStorage.findFriends(id).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

    }

    public Collection<User> findCommonFriends(final int id, final int otherId) {
        log.info("Обрабатываем запрос на поиск общих друзей между пользователями");
        return userStorage.findCommonFriends(id, otherId);
    }

    public void removeUser(final int id) {
        log.info("Обрабатываем запрос на удаление пользователя (id): {}", id);
        userStorage.removeUser(id);
    }
}
