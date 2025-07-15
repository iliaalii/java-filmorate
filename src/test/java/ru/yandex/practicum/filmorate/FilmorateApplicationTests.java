/*
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
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({

        UserDbStorage.class, UserRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        RatingDbStorage.class, RatingRowMapper.class,
        FilmService.class, ReviewDbStorage.class,
        ReviewRowMapper.class, EventRowMapper.class,
        EventDbStorage.class, EventService.class,
        DirectorDbStorage.class, DirectorRowMapper.class

})
class FilmorateApplicationTests {
    private final JdbcTemplate jdbc;
    private final UserRepository userStorage;
    private final FilmRepository filmStorage;
    private final GenreRepository genreStorage;
    private final FilmService filmService;

    private final ReviewDbStorage reviewStorage;
    private final RatingDbStorage ratingStorage;

    User user;
    Film film, film1, film2, film3;
    Review review;

    @BeforeEach
    void setup() {
        user = new User();
        user.setLogin("testlogin");
        user.setEmail("test@mail.com");
        user.setBirthday(LocalDate.now());

        Collection<Genre> genres = genreStorage.findAllGenre();
        List<Genre> genreList = new ArrayList<>(genres);
        Genre firstGenre = genreList.get(0);
        Genre fourthGenre = genreList.get(3);
        Collection<Rating> ratings = ratingStorage.findAllRating();
        List<Rating> ratingList = new ArrayList<>(ratings);
        Rating firstRating = ratingList.getFirst();
        Rating thirdRating = ratingList.get(3);

        film = new Film();
        film.setName("test");
        film.setDuration(50);
        film.setReleaseDate(LocalDate.now());

        film1 = new Film();
        film1.setName("Академия чудес");
        film1.setDuration(120);
        film1.setReleaseDate(LocalDate.of(2021, 3, 10));
        film1.setGenres(Set.of(firstGenre));
        film1.setMpa(firstRating);

        film2 = new Film();
        film2.setName("Академия чудес 2");
        film2.setDuration(120);
        film2.setReleaseDate(LocalDate.of(2021, 12, 10));
        film2.setGenres(Set.of(firstGenre));
        film2.setMpa(firstRating);

        film3 = new Film();
        film3.setName("Закатные твари");
        film3.setDuration(120);
        film3.setReleaseDate(LocalDate.of(2023, 3, 21));
        film3.setGenres(Set.of(fourthGenre));
        film3.setMpa(thirdRating);

        filmStorage.create(film1);
        filmStorage.create(film2);
        filmStorage.create(film3);

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
    void testReturnMostPopularFilmsByYearFilter() {
        Collection<Film> result = filmService.getPopularFilms(3, null, 2021);

        assertThat(result)
                .extracting(Film::getId)
                .containsExactly(film1.getId(), film2.getId());
    }

    @Test
    void testReturnMostPopularFilmsByGenreFilter() {
        Collection<Film> result = filmService.getPopularFilms(3, 1, null);

        assertThat(result)
                .extracting(Film::getId)
                .containsExactly(film1.getId(), film2.getId());
    }

    @Test
    void testReturnMostPopularFilmsByGenreAndYearFilters() {
        Collection<Film> result = filmService.getPopularFilms(3, 4, 2023);

        assertThat(result)
                .extracting(Film::getId)
                .containsExactly(film3.getId());
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
}*/
