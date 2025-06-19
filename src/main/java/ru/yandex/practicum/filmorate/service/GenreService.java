package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {
    private final GenreDbStorage storage;

    public Genre findGenre(int id) {
        log.info("Обрабатываем запрос на поиск жанра");
        return storage.findGenre(id);
    }

    public Collection<Genre> findAllGenre() {
        log.info("Обрабатываем запрос на поиск всех жанров");
        return storage.findAllGenre();
    }
}