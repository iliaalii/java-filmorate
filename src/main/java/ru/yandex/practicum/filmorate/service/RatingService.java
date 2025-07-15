package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.RatingRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingService {
    private final RatingRepository storage;

    public Rating findRating(int id) {
        log.info("Обрабатываем запрос на поиск рейтинга");
        return storage.findRating(id);
    }

    public Collection<Rating> findAllRating() {
        log.info("Обрабатываем запрос на поиск всех рейтингов");
        return storage.findAllRating();
    }

    public Map<Integer, Rating> findAllRatingsByFilms(final List<Integer> films) {
        return storage.findAllRatingsByFilm(films);
    }

    public void validateRating(final Film film) {
        if (film.getMpa() != null) {
            findRating(film.getMpa().getId());
        }
    }
}