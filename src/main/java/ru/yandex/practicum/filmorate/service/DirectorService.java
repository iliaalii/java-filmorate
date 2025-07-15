package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {
    private final DirectorRepository storage;

    public Director findDirector(int id) {
        log.info("Обрабатываем запрос на поиск режиссера");
        return storage.findDirector(id);
    }

    public Collection<Director> findAllDirector() {
        log.info("Обрабатываем запрос на поиск всех режиссеров");
        return storage.findAllDirectors();
    }

    public Director create(Director director) {
        log.info("Обрабатываем запрос на добавление режиссера");
        return storage.create(director);
    }

    public Director update(Director newDirector) {
        log.info("Обрабатываем запрос на обновление режиссера");
        return storage.update(newDirector);
    }

    public void removeDirector(int directorId) {
        log.info("Обрабатываем запрос на удаление режиссера");
        storage.removeDirector(directorId);
    }

    public Map<Integer, Set<Director>> findAllDirectorsByFilms() {
        return storage.findAllDirectorsByFilms();
    }

    public Set<Director> findDirectorsFilm(final int id) {
        return storage.findDirectorsFilm(id);
    }

    public Collection<Film> getDirectorFilmsSorted(int directorId, String sortBy) {
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
            for (Director director : film.getDirectors()) {
                try {
                    storage.findDirector(director.getId());
                } catch (NotFoundException e) {
                    throw new ValidationException("Указан несуществующий режиссер: " + director.getId());
                }
            }
        }
    }
}
