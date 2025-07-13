package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDbStorage;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectorService {
    private final DirectorDbStorage storage;

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
}
