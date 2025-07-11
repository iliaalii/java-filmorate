package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.dao.mappers.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        UserDbStorage.class, UserRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        ReviewDbStorage.class, ReviewRowMapper.class
})
class FilmorateApplicationTests {
    private final JdbcTemplate jdbc;
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final ReviewDbStorage reviewStorage;

    User user;
    Film film;
    Review review;
    @Autowired
    private UserDbStorage userDbStorage;

    @BeforeEach
    void setup() {
        user = new User();
        user.setLogin("testlogin");
        user.setEmail("test@mail.com");
        user.setBirthday(LocalDate.now());

        film = new Film();
        film.setName("test");
        film.setDuration(50);
        film.setReleaseDate(LocalDate.now());

        review = new Review();
        review.setContent("тестовый отзыв");
        review.setIsPositive(true);
    }

    @Test
    void testCreateUserAndFindById() {
        user = userStorage.create(user);

        Optional<User> userOptional = Optional.ofNullable(userStorage.findUser(user.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }

    @Test
    void testUpdateUser() {
        user = userStorage.create(user);
        user.setName("UpdateName");
        userStorage.update(user);

        Optional<User> userOptional = Optional.ofNullable(userStorage.findUser(user.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("name", "UpdateName")
                );
    }

    @Test
    void testAddAndRemoveFriend() {
        user = userStorage.create(user);
        System.out.println(user);
        User friend = new User();
        friend.setLogin("testlogin");
        friend.setEmail("test@mail.com");
        friend.setBirthday(LocalDate.now());

        friend = userStorage.create(friend);

        userStorage.addFriend(user.getId(), friend.getId());
        User finalFriend = friend;
        Collection<User> friends = userStorage.findFriends(user.getId());

        assertThat(friends.isEmpty()).isFalse();
        assertThat(friends.stream().findFirst())
                .hasValueSatisfying(u ->
                        assertThat(u).isEqualTo(finalFriend));

        userStorage.removeFriend(user.getId(), friend.getId());
        friends = userStorage.findFriends(user.getId());
        assertThat(friends.isEmpty()).isTrue();
    }

    @Test
    void testCreateFilmAndFindById() {
        film = filmStorage.create(film);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.findFilm(film.getId()));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", film.getId())
                );
    }

    @Test
    void testUpdateFilm() {
        film = filmStorage.create(film);
        film.setName("UpdateName");
        filmStorage.update(film);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.findFilm(film.getId()));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("name", "UpdateName")
                );
    }

    @Test
    void testGetGenres() {
        Collection<Genre> genres = genreStorage.findAllGenre();
        assertThat(genres).size().isEqualTo(6);
    }

    @Test
    void testThatExistingUserCanRemove() {
        user = userStorage.create(user);
        Integer countBefore = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE USER_ID = " + user.getId(), Integer.class);
        assertEquals(1, countBefore);

        userStorage.removeUser(user.getId());
        Integer countAfter = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USERS WHERE USER_ID = " + user.getId(), Integer.class);
        assertEquals(0, countAfter);
    }

    @Test
    void testThatExistingFilmCanRemove() {
        film = filmStorage.create(film);
        Integer countBefore = jdbc.queryForObject(
                "SELECT COUNT(*) FROM FILMS WHERE FILM_ID = " + film.getId(), Integer.class);
        assertEquals(1, countBefore);

        filmStorage.removeFilm(film.getId());
        Integer countAfter = jdbc.queryForObject(
                "SELECT COUNT(*) FROM FILMS WHERE FILM_ID = " + film.getId(), Integer.class);
        assertEquals(0, countAfter);
    }

    @Test
    void testAddReviewFindByIdAndDelete() {
        user = userStorage.create(user);
        film = filmStorage.create(film);

        review.setFilmId(film.getId());
        review.setUserId(user.getId());
        review = reviewStorage.add(review);

        Optional<Review> reviewOptional = Optional.ofNullable(reviewStorage.findById(review.getReviewId()));

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(r ->
                        assertThat(r).hasFieldOrPropertyWithValue("reviewId", review.getReviewId())
                );

        reviewStorage.remove(review.getReviewId());

        assertThatThrownBy(() -> {
            reviewStorage.findById(review.getReviewId());
        })
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("По указанному id (" + review.getReviewId() + ") обзор не обнаружен");
    }

    @Test
    void testAddLikeAndDislikeReview() {
        user = userStorage.create(user);
        film = filmStorage.create(film);

        review.setFilmId(film.getId());
        review.setUserId(user.getId());
        review = reviewStorage.add(review);

        Optional<Review> reviewOptional = Optional.ofNullable(reviewStorage.findById(review.getReviewId()));

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(r ->
                        assertThat(r).hasFieldOrPropertyWithValue("useful", 0)
                );

        reviewStorage.addLike(review.getReviewId(), user.getId());
        user = userStorage.create(user);
        reviewStorage.addLike(review.getReviewId(), user.getId());

        reviewOptional = Optional.ofNullable(reviewStorage.findById(review.getReviewId()));

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(r ->
                        assertThat(r).hasFieldOrPropertyWithValue("useful", 2)
                );

        user = userStorage.create(user);
        reviewStorage.addDislike(review.getReviewId(), user.getId());

        reviewOptional = Optional.ofNullable(reviewStorage.findById(review.getReviewId()));

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(r ->
                        assertThat(r).hasFieldOrPropertyWithValue("useful", 1)
                );
    }
}