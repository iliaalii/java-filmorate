package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmRepository;
import ru.yandex.practicum.filmorate.dao.ReviewRepository;
import ru.yandex.practicum.filmorate.dao.UserRepository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewsService {

    private final ReviewRepository storage;
    private final EventService eventService;
    private final FilmRepository filmStorage;
    private final UserRepository userStorage;

    public Review add(final Review review) {
        checkRelevance(review);
        Review savedReview = storage.add(review);
        eventService.createNowEvent(savedReview.getUserId(), savedReview.getReviewId(), EventType.REVIEW, OperationType.ADD);

        return savedReview;
    }

    public Review update(final Review review) {
        checkRelevance(review);
        Review savedReview = storage.update(review);
        eventService.createNowEvent(savedReview.getUserId(), savedReview.getReviewId(), EventType.REVIEW, OperationType.UPDATE);

        return savedReview;
    }

    public void remove(final int id) {
        Review savedReview = storage.findById(id);
        eventService.createNowEvent(savedReview.getUserId(), savedReview.getReviewId(), EventType.REVIEW, OperationType.REMOVE);

        storage.remove(id);
    }

    public Review findById(final int id) {
        return storage.findById(id);
    }

    public Collection<Review> findAll(final Integer filmId, final int count) {
        return storage.findAll(filmId, count);
    }

    public void addLike(final int reviewId, final int userId) {
        storage.addLike(reviewId, userId);
    }

    public void removeLike(final int reviewId, final int userId) {
        storage.removeLike(reviewId, userId);
    }

    public void addDislike(final int reviewId, final int userId) {
        storage.addDislike(reviewId, userId);
    }

    public void removeDislike(final int reviewId, final int userId) {
        storage.removeDislike(reviewId, userId);
    }

    private void checkRelevance(Review review) {
        log.info("Проводим проверку наличия пользователя и фильма");
        filmStorage.findFilm(review.getFilmId());
        userStorage.findUser(review.getUserId());
    }
}
