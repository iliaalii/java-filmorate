package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private final LocalDate cinemaBirthday = LocalDate.of(1895, Month.DECEMBER, 28);
    private int nextId = 1;
    private UserStorage uStorage;

    @Override
    public Collection<Film> findAll() {
        return List.copyOf(films.values());
    }

    @Override
    public Film findFilm(int id) {
        if (films.get(id) == null) {
            throw new NotFoundException("По указанному id (" + id + ") фильм не обнаружен");
        }
        return films.get(id);
    }

    @Override
    public Film create(Film film) {
        if (film.getReleaseDate().isBefore(cinemaBirthday)) {
            throw new ValidationException("Данные не проходят проверку:\n" +
                    "-дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        film.setId(nextId++);
        log.info("В список добавлен новый фильм: {}", film);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getReleaseDate().isBefore(cinemaBirthday)) {
                throw new ValidationException("Данные не проходят проверку:\n" +
                        "-дата релиза — не раньше 28 декабря 1895 года.");
            }
            log.info("Обновлены данные фильма: {}, на новые: {}", oldFilm, newFilm);
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    @Override
    public void addLike(int id, int userId) {
        Film film = findFilm(id);
        if (uStorage.findUser(userId) != null) {
            film.getLikes().add(userId);
        }
    }

    @Override
    public void removeLike(int id, int userId) {
        Film film = findFilm(id);
        if (uStorage.findUser(userId) != null) {
            film.getLikes().remove(userId);
        }
    }

    @Override
    public void removeFilm(int filmId) {
        if (films.get(filmId) != null) {
            films.remove(filmId);
        }
    }
}
