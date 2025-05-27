package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage storage;

    public void addFriend(int id, int friendId) {
        if (storage.findUser(friendId) != null) {
            log.info("Пользователи с id: {} и {} теперь друзья", id, friendId);
            storage.findUser(id).getFriends().add(friendId);
            storage.findUser(friendId).getFriends().add(id);
        }
    }

    public void removeFriend(int id, int friendId) {
        if (storage.findUser(friendId) != null) {
            log.info("Пользователи с id: {} и {} больше не друзья", id, friendId);
            storage.findUser(id).getFriends().remove(friendId);
            storage.findUser(friendId).getFriends().remove(id);
        }
    }

    public Collection<User> findFriends(int id) {
        return List.copyOf(storage.findUser(id).getFriends().stream()
                .map(storage::findUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public Collection<User> findCommonFriends(int id, int otherId) {
        HashSet<Integer> commonId = new HashSet<>(storage.findUser(id).getFriends());
        commonId.retainAll(storage.findUser(otherId).getFriends());

        return List.copyOf(commonId.stream()
                .map(storage::findUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }
}