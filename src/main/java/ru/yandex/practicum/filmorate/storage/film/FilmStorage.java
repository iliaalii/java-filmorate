package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findFilm(int id);

    Film create(Film film);

    Film update(Film newFilm);

    void addLike(int id, int userId);

    void removeLike(int id, int userId);

    List<Film> getCommonFilms(final int id, final int friendId);
}