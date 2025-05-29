package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {
    User user;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    void setup() {
        user = new User();
    }

    @Test
    void createUserIfNameBlank() {
        user.setLogin("testlogin");
        user.setEmail("test@mail.com");
        user.setName(" ");

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);

        assertNotNull(response.getBody());
        assertEquals("testlogin", response.getBody().getName());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void createUserIfTheLoginIsEmpty() {

        user.setLogin("");
        user.setEmail("test@mail.com");

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserIfTheLoginContainsSpaces() {
        user.setLogin("bad login");
        user.setEmail("test@mail.com");

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

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createUserWithAnIncorrectlyFilledInEmail() {
        user.setLogin("test");
        user.setEmail("testmail.com");

        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        user.setEmail("@mailcom");

        response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
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