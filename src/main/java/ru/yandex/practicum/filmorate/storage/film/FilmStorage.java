package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findFilm(int id);

    Film create(Film film);

    Film update(Film newFilm);

    void addLike(int id, int userId);

    void removeLike(int id, int userId);

    Collection<Film> sortDirectorByYear(int directorId);
    Collection<Film> sortDirectorByLikes(int directorId);
}