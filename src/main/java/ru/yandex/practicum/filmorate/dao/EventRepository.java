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
public class EventRepository {
    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    private static final String ADD_EVENT = "INSERT INTO events (event_time, user_id, event_type_id, " +
            "operation_type_id, entity_id) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_USER_EVENTS = "SELECT event_id, event_time, user_id, event_type_id, " +
            "operation_type_id, entity_id FROM events WHERE user_id = ?";


    public void addEvent(final Event event) {
        jdbc.update(con -> {
            log.debug("Добавляется ивент.");
            PreparedStatement ps = con.prepareStatement(ADD_EVENT, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, event.getTimestamp());
            ps.setLong(2, event.getUserId());
            ps.setInt(3, event.getEventType().getId());
            ps.setInt(4, event.getOperation().getId());
            ps.setLong(5, event.getEntityId());
            return ps;
        });
    }

    public Collection<Event> getUserEvents(final long id) {
        log.debug("Возвращаются все ивенты пользователя с id: {}.", id);
        return jdbc.query(GET_USER_EVENTS, eventRowMapper, id);
    }
}
