package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();

    User findUser(int id);

    User create(User user);

    User update(User newUser);

    void addFriend(int id, int friendId);

    void removeFriend(int id, int friendId);

    Collection<User> findFriends(int id);

    Collection<User> findCommonFriends(int id, int otherId);

    void removeUser(int id);
}
