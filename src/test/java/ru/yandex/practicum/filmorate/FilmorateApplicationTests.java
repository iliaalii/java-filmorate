package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.dao.RatingDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        UserDbStorage.class, UserRowMapper.class,
        FilmDbStorage.class, FilmRowMapper.class,
        GenreDbStorage.class, GenreRowMapper.class,
        RatingDbStorage.class, RatingRowMapper.class
})
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final RatingDbStorage ratingStorage;
    User user;
    Film film;

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
    }

    @Test
    public void testCreateUserAndFindById() {
        user = userStorage.create(user);

        Optional<User> userOptional = Optional.ofNullable(userStorage.findUser(user.getId()));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", user.getId())
                );
    }

    @Test
    public void testUpdateUser() {
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
    public void testAddAndRemoveFriend() {
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
    public void testCreateFilmAndFindById() {
        film = filmStorage.create(film);

        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.findFilm(film.getId()));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("id", film.getId())
                );
    }

    @Test
    public void testUpdateFilm() {
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
    public void testGetGenres() {
        Collection<Genre> genres = genreStorage.findAllGenre();
        assertThat(genres).size().isEqualTo(6);
    }
}