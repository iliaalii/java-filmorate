package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.RatingDbStorage;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {
    private final RatingDbStorage storage;

    public Rating findRating(int id) {
        log.info("Обрабатываем запрос на поиск рейтинга");
        return storage.findRating(id);
    }

    public Collection<Rating> findAllRating() {
        log.info("Обрабатываем запрос на поиск всех рейтингов");
        return storage.findAllRating();
    }
}