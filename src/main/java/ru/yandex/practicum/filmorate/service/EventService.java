package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventRepository;
import ru.yandex.practicum.filmorate.dao.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventStorage;
    private final UserRepository userService;

    public void createNowEvent(final long userId, final long entityId,
                               final EventType eventType, final OperationType operationType) {
        log.trace("Создается ивент.");

        eventStorage.addEvent(Event.builder()
                .timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .userId(userId)
                .eventType(eventType)
                .operation(operationType)
                .entityId(entityId)
                .build()
        );
    }

    public Collection<Event> getUserEvents(final long id) {
        log.trace("Запрошены все ивенты пользователя с id: {}.", id);
        if (userService.findUser((int) id) == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        return eventStorage.getUserEvents(id);
    }
}
