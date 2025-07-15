package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FilmControllerTest {
    Film film;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    void setup() {
        film = new Film();
    }

    @Test
    void emptyTitle() {
        film.setDuration(50);
        film.setReleaseDate(LocalDate.of(1999, 1, 1));
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        film.setName(" ");
        response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void descriptionLengthExceedsLimit() {
        film.setName("test");
        film.setDescription("nice film ".repeat(21));
        film.setDuration(50);
        film.setReleaseDate(LocalDate.of(1999, 1, 1));

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void releaseBeforeTheFirstFilm() {
        film.setName("test");
        film.setDuration(50);
        film.setReleaseDate(LocalDate.of(1895, 12, 20));

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void releaseInTheFuture() {
        film.setName("test");
        film.setDuration(50);
        film.setReleaseDate(LocalDate.of(2999, 1, 1));

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void releaseNotCompleted() {
        film.setName("test");
        film.setDuration(50);

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void durationNegativeNumber() {
        film.setName("test");
        film.setDuration(-1);
        film.setReleaseDate(LocalDate.of(2999, 1, 1));

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        film.setDuration(0);
        response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createFilm() {
        film.setName("test");
        film.setDuration(50);
        film.setReleaseDate(LocalDate.now());

        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testUpdateFilmSuccessfully() {
        film.setName("test");
        film.setDuration(50);
        film.setReleaseDate(LocalDate.now());

        Film created = restTemplate.postForEntity("/films", film, Film.class).getBody();
        assertNotNull(created);

        created.setDescription("Updated description");
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Film> request = new HttpEntity<>(created, headers);

        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, request, Film.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated description", response.getBody().getDescription());
    }

    @Test
    void testGetCommonFilms() {
        int userId = (int) Objects.requireNonNull(restTemplate.postForEntity("/users", Map.of(
                "email", "user1@mail.ru",
                "login", "user1",
                "name", "User",
                "birthday", "2025-07-10"
        ), Map.class).getBody()).get("id");

        int friendId = (int) Objects.requireNonNull(restTemplate.postForEntity("/users", Map.of(
                "email", "user2@mail.ru",
                "login", "user2",
                "name", "User",
                "birthday", "2025-07-10"
        ), Map.class).getBody()).get("id");

        Map<String, Object> filmRequest = Map.of(
                "name", "SuperFilm",
                "description", "filmDescription",
                "duration", 120,
                "releaseDate", "2025-07-10",
                "mpa", Map.of("id", 4),
                "genres", List.of(Map.of("id", 1))
        );

        int filmId = (int) Objects.requireNonNull(restTemplate.postForEntity("/films", filmRequest, Map.class)
                .getBody()).get("id");

        restTemplate.put("/films/{id}/like/{userId}", null, filmId, userId);
        restTemplate.put("/films/{id}/like/{userId}", null, filmId, friendId);

        restTemplate.put("/users/{id}/friends/{friendId}", null, userId, friendId);

        ResponseEntity<Film[]> response = restTemplate.getForEntity(
                "/films/common?userId={userId}&friendId={friendId}",
                Film[].class,
                userId, friendId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Film[] films = response.getBody();
        assertNotNull(films);
        assertEquals(1, films.length);
        assertEquals("SuperFilm", films[0].getName());
    }
}