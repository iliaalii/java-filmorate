package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewsService {
    private final ReviewDbStorage storage;

    public Review add(Review review) {
        return storage.add(review);
    }

    public Review update(Review review) {
        return storage.update(review);
    }

    public void remove(int id) {
        storage.remove(id);
    }

    public Review findById(int id) {
        return storage.findById(id);
    }

    public Collection<Review> findAll(Integer filmId, int count) {
        return storage.findAll(filmId, count);
    }

    public void addLike(int id, int userId) {
        storage.addLike(id, userId);
    }

    public void addDislike(int id, int userId) {
        storage.addDislike(id, userId);
    }

    public void removeLike(int id, int userId) {
        storage.removeLike(id, userId);
    }

    public void removeDislike(int id, int userId) {
        storage.removeDislike(id, userId);
    }
}
