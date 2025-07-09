package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDbStorage;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {
    private final EventDbStorage storage;

    public void createNowEvent(final long userId, final long entityId,
                                       final Event.EventType eventType, Event.Operation operation) {
        log.trace("Создается ивент.");

        storage.addEvent(Event.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build()
        );
    }

    public Collection<Event> getUserEvents(final long id) {
        log.trace("Запрошены все ивенты пользователя с id: {}.", id);
        return storage.getUserEvents(id);
    }
}
