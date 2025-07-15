
package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.RatingService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    User user;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    UserService userService;

    @Autowired
    FilmService filmService;

    @Autowired
    RatingService ratingStorage;

    @BeforeEach
    void setup() {
        user = new User();
    }

    @Test
    void createUserIfNameBlank() {
        user.setLogin("testlogin");
        user.setEmail("test@mail.com");
        user.setName(" ");
        user.setBirthday(LocalDate.now());

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertNotNull(response.getBody());
        assertEquals("testlogin", response.getBody().getName());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void createUserIfTheLoginIsEmpty() {

        user.setLogin("");
        user.setEmail("test@mail.com");
        user.setBirthday(LocalDate.now());

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserIfTheLoginContainsSpaces() {
        user.setLogin("bad login");
        user.setEmail("test@mail.com");
        user.setBirthday(LocalDate.now());

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserBornInTheFuture() {
        user.setLogin("Fry");
        user.setName("Philip J.");
        user.setEmail("futurama@mail.com");
        user.setBirthday(LocalDate.of(2999, 12, 31));

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserBornToday() {
        user.setLogin("newUser");
        user.setEmail("test@mail.com");
        user.setBirthday(LocalDate.now());

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("newUser", response.getBody().getName());
    }

    @Test
    void createUserWithoutMail() {
        user.setLogin("test");
        user.setBirthday(LocalDate.now());

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserWithAnIncorrectlyFilledInEmail() {
        user.setLogin("test");
        user.setEmail("testmail.com");
        user.setBirthday(LocalDate.now());

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        user.setEmail("@mailcom");

        response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRecommendFilms() {
        Collection<Rating> ratings = ratingStorage.findAllRating();
        List<Rating> ratingList = new ArrayList<>(ratings);
        Rating secondRating = ratingList.get(1);
        Rating thirdRating = ratingList.get(2);
        User user1 = new User();
        user1.setLogin("user1");
        user1.setEmail("novacancy@mail.ru");
        user1.setBirthday(LocalDate.of(2025,7, 12));

        User user2 = new User();
        user2.setLogin("user2");
        user2.setEmail("vacancy@mail.ru");
        user2.setBirthday(LocalDate.of(2025,7, 12));

        user1 = userService.create(user1);
        user2 = userService.create(user2);

        Film film1 = new Film();
        film1.setName("Фильм_А");
        film1.setDescription("Очевидно это фильм А.");
        film1.setReleaseDate(LocalDate.of(2025, 7,12));
        film1.setDuration(120);
        film1.setMpa(secondRating);

        Film film2 = new Film();
        film2.setName("Фильм_Б");
        film2.setDescription("Очевидно это фильм Б.");
        film2.setReleaseDate(LocalDate.of(2025, 7,12));
        film2.setDuration(120);
        film2.setMpa(thirdRating);

        film1 = filmService.create(film1);
        film2 = filmService.create(film2);

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film2.getId(), user1.getId());

        filmService.addLike(film1.getId(), user2.getId());

        Collection<Film> recommendations = filmService.getRecommendFilms(user2.getId());

        assertNotNull(recommendations);
        Film film3 = film2;
        assertTrue(recommendations.stream().anyMatch(f -> f.getId().equals(film3.getId())));
    }

    @Test
    void testUpdateUserSuccessfully() {
        user.setLogin("Test");
        user.setEmail("test@mail.com");
        user.setBirthday(LocalDate.now());

        User created = restTemplate.postForEntity("/users", user, User.class).getBody();
        assertNotNull(created);

        created.setName("Updated Name");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<User> request = new HttpEntity<>(created, headers);

        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, request, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Name", response.getBody().getName());
    }
}
