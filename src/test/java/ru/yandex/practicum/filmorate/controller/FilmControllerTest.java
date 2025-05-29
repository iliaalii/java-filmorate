package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

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
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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
}