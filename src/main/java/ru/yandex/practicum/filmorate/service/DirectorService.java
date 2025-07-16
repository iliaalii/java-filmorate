package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {

    private final DirectorRepository storage;

    public Director findDirector(final int id) {
        log.info("Обрабатываем запрос на поиск режиссера");

        return Optional.ofNullable(findAllDirector().get(id))
                .orElseThrow(() -> new NotFoundException("Режиссер с id " + id + " не найден"));
    }

    @Cacheable("allDirectors")
    public Map<Integer, Director> findAllDirector() {
        log.info("Обрабатываем запрос на поиск всех режиссеров");
        return storage.findAllDirectors();
    }

    @CacheEvict(value = "allDirectors", allEntries = true)
    public Director create(final Director director) {
        log.info("Обрабатываем запрос на добавление режиссера");
        return storage.create(director);
    }

    @CacheEvict(value = "allDirectors", allEntries = true)
    public Director update(final Director newDirector) {
        log.info("Обрабатываем запрос на обновление режиссера");
        return storage.update(newDirector);
    }

    @CacheEvict(value = "allDirectors", allEntries = true)
    public void removeDirector(final int directorId) {
        log.info("Обрабатываем запрос на удаление режиссера");
        storage.removeDirector(directorId);
    }

    public Map<Integer, Set<Director>> findAllDirectorsByFilms(final List<Integer> filmsIds) {
        return storage.findAllDirectorsByFilms(filmsIds);
    }

    public Collection<Film> getDirectorFilmsSorted(final int directorId, final String sortBy) {
        if (storage.findDirector(directorId) == null) {
            throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");
        }

        return switch (sortBy) {
            case "year" -> storage.sortDirectorByYear(directorId);

            case "likes" -> storage.sortDirectorByLikes(directorId);

            default -> throw new ValidationException("Недопустимый параметр sortBy: " + sortBy);
        };
    }

    public void saveFilmDirectors(final Film film) {
        storage.saveFilmDirectors(film);
    }

    public void validateDirectors(final Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Integer> idsDirectors = film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            Set<Integer> directorsInDb = new HashSet<>(storage.findAllDirectorsByFilm(idsDirectors));
            List<Integer> missing = idsDirectors.stream()
                    .filter(id -> !directorsInDb.contains(id))
                    .toList();
            if (!missing.isEmpty()) {
                throw new ValidationException("Найдены несуществующие режиссёры: " + missing);
            }
        }
    }
}
