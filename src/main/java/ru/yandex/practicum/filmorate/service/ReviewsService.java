package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewDbStorage;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewsService {
    private final ReviewDbStorage storage;
    private final EventService eventService;


    public Review add(Review review) {
        Review savedReview = storage.add(review);

        eventService.createNowEvent(savedReview.getUserId(), savedReview.getReviewId(), EventType.REVIEW, OperationType.ADD);

        return savedReview;
    }

    public Review update(Review review) {
        Review savedReview = storage.update(review);
        //log.info("Review updated: reviewId={}, userId={}", review.getReviewId(), review.getUserId());
        log.info("Обновлён reviewId={}, userId={}", savedReview.getReviewId(), savedReview.getUserId());
        eventService.createNowEvent(savedReview.getUserId(), savedReview.getReviewId(), EventType.REVIEW, OperationType.UPDATE);

        return savedReview;
    }

    public void remove(int id) {
        Review savedReview = storage.findById(id);
        eventService.createNowEvent(savedReview.getUserId(), savedReview.getReviewId(), EventType.REVIEW, OperationType.REMOVE);

        storage.remove(id);
    }

    public Review findById(int id) {
        return storage.findById(id);
    }

    public Collection<Review> findAll(Integer filmId, int count) {
        return storage.findAll(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        storage.addLike(reviewId, userId);
        //eventService.createNowEvent(userId, reviewId, EventType.LIKE, OperationType.ADD); //<- Review REMOVE
    }

    public void removeLike(int reviewId, int userId) {
        //eventService.createNowEvent(userId, reviewId, EventType.LIKE, OperationType.REMOVE);
        storage.removeLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        storage.addDislike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        storage.removeDislike(reviewId, userId);
    }
}
