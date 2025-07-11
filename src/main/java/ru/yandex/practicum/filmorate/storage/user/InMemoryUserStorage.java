package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Qualifier("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public Collection<User> findAll() {
        return List.copyOf(users.values());
    }

    @Override
    public User findUser(int id) {
        if (users.get(id) == null) {
            throw new NotFoundException("По указанному id (" + id + ") пользователь не обнаружен");
        }
        return users.get(id);
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(nextId++);
        log.info("В список добавлен новый пользователь: {}", user);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            log.info("Обновлены данные пользователя: {}, на новые: {}", oldUser, newUser);
            oldUser.setLogin(newUser.getLogin());
            oldUser.setName(newUser.getName());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setBirthday(newUser.getBirthday());
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public void addFriend(int id, int friendId) {
        if (findUser(friendId) != null) {
            findUser(id).getFriends().add(friendId);
            findUser(friendId).getFriends().add(id);
        }
    }

    @Override
    public void removeFriend(int id, int friendId) {
        if (findUser(friendId) != null) {
            findUser(id).getFriends().remove(friendId);
            findUser(friendId).getFriends().remove(id);
        }
    }

    @Override
    public Collection<User> findFriends(int id) {
        return List.copyOf(findUser(id).getFriends().stream()
                .map(this::findUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    @Override
    public Collection<User> findCommonFriends(int id, int otherId) {
        HashSet<Integer> commonId = new HashSet<>(findUser(id).getFriends());
        commonId.retainAll(findUser(otherId).getFriends());

        return List.copyOf(commonId.stream()
                .map(this::findUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    @Override
    public void removeUser(int id) {
        if (users.get(id) != null) {
            users.remove(id);
        }
    }
}
