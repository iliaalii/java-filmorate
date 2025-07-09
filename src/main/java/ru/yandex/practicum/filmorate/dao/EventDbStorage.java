package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EventDbStorage {
    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    private static final String ADD_EVENT = "INSERT INTO Events (event_time, user_id, event_type, operation," +
            " entity_id) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_USER_EVENTS = "SELECT event_id, event_time, user_id, event_type, operation," +
            " entity_id FROM Events WHERE user_id = ?";


    public void addEvent(Event event) {
        jdbc.update(con -> {
            log.info("Добавляется ивент.");
            PreparedStatement ps = con.prepareStatement(ADD_EVENT, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, event.getTimestamp());
            ps.setLong(2, event.getUserId());
            ps.setString(3, event.getEventType().toString());
            ps.setString(4, event.getOperation().toString());
            ps.setLong(5, event.getEntityId());
            return ps;
        });
    }

    public Collection<Event> getUserEvents(long id) {
        log.trace("Возвращаются все ивенты пользователя с id: {}.", id);
        return jdbc.query(GET_USER_EVENTS, eventRowMapper, id);
    }
}
