package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(int id, int userId) {
        if (userStorage.findUser(userId) != null) {
            log.info("Пользователь под номером: {}, поставил лайк фильму под номером: {}", userId, id);
            filmStorage.findFilm(id).getLikes().add(userId);
        }
    }

    public void removeLike(int id, int userId) {
        if (userStorage.findUser(userId) != null) {
            log.info("Пользователь под номером: {}, удалил лайк фильму под номером: {}", userId, id);
            filmStorage.findFilm(id).getLikes().remove(userId);
        }
    }

    public Collection<Film> getPopularFilms(Integer count) {
        if (count <= 0) {
            count = 10;
        }
        return List.copyOf(filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList());
    }
}