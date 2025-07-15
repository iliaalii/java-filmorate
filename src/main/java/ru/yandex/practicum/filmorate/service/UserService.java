package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
    private final FilmStorage filmStorage;
    private final DirectorService directorService; //Тут подумать


    public Collection<User> findAll() {
        log.info("Обрабатываем запрос на поиск всех пользователей");
        return userStorage.findAll();
    }

    public User findUser(int id) {
        log.info("Обрабатываем запрос на поиск пользователя");
        return userStorage.findUser(id);
    }

    public User create(User user) {
        log.info("Обрабатываем запрос на добавление нового пользователя");
        return userStorage.create(user);
    }

    public User update(User newUser) {
        log.info("Обрабатываем запрос на обновление пользователя");
        return userStorage.update(newUser);
    }

    public void addFriend(int id, int friendId) {
        log.info("Обрабатываем запрос на добавление в друзья");
        if (userStorage.findUser(id) != null && userStorage.findUser(friendId) != null) {
            userStorage.addFriend(id, friendId);
            eventService.createNowEvent(id, friendId, EventType.FRIEND, OperationType.ADD);
        }
    }

    public void removeFriend(int id, int friendId) {
        log.info("Обрабатываем запрос на удаление из друзей");
        if (userStorage.findUser(id) != null && userStorage.findUser(friendId) != null) {
            userStorage.removeFriend(id, friendId);
            eventService.createNowEvent(id, friendId, EventType.FRIEND, OperationType.REMOVE);
        }
    }

    public Collection<User> findFriends(int id) {
        log.info("Обрабатываем запрос на поиск всех друзей пользователя");
        if (userStorage.findUser(id) != null) {
            return List.copyOf(userStorage.findFriends(id).stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

    }

    public Collection<User> findCommonFriends(int id, int otherId) {
        log.info("Обрабатываем запрос на поиск общих друзей между пользователями");
        return userStorage.findCommonFriends(id, otherId);
    }

    public void removeUser(int id) {
        log.info("Обрабатываем запрос на удаление пользователя (id): {}", id);
        userStorage.removeUser(id);
    }
}
