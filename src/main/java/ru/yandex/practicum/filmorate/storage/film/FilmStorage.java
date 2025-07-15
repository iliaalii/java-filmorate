package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findFilm(int id);

    Film create(Film film);

    Film update(Film newFilm);

    void addLike(int id, int userId);

    void removeLike(int id, int userId);

    Collection<Film> search(String query, List<String> by);

    Collection<Film> recommendFilms(final long userId);

    void removeFilm(int filmId);

    Collection<Film> getCommonFilms(final int id, final int friendId);

    Collection<Film> getPopularFilms(final Integer count, final Integer genreId, final Integer year);

    Map<Integer, Set<Integer>> findAllLikes(final List<Integer> filmIds);
}
