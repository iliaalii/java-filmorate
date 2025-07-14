package ru.yandex.practicum.filmorate.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({EventRepository.class, EventRowMapper.class, UserRepository.class, UserRowMapper.class})
class EventDbStorageTest {

    @Autowired
    private EventRepository eventStorage;

    @Autowired
    private UserRepository userStorage;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setLogin("login");
        testUser.setName("name");
        testUser.setEmail("test@mail.ru");
        testUser.setBirthday(LocalDate.of(2025, 7, 9));

        testEvent = Event.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .userId(1L)
                .eventType(EventType.LIKE)
                .operation(OperationType.ADD)
                .entityId(100L)
                .build();
    }

    @Test
    void testAddAndGetEvents() {
        userStorage.create(testUser);
        eventStorage.addEvent(testEvent);

        Collection<Event> events = eventStorage.getUserEvents(1L);

        assertThat(events).hasSize(1);
        Event actual = events.iterator().next();

        assertThat(actual.getUserId()).isEqualTo(testEvent.getUserId());
        assertThat(actual.getEventType()).isEqualTo(testEvent.getEventType());
        assertThat(actual.getOperation()).isEqualTo(testEvent.getOperation());
        assertThat(actual.getEntityId()).isEqualTo(testEvent.getEntityId());
    }

    @Test
    void testGetEventsWhenNone() {
        Collection<Event> events = eventStorage.getUserEvents(999L);
        assertThat(events).isEmpty();
    }
}
