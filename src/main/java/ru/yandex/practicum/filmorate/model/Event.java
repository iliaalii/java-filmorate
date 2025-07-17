package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.OperationType;

import java.sql.Timestamp;

@Value
@Builder
public class Event {

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Timestamp timestamp;
    Long userId;
    EventType eventType;
    OperationType operation;
    Long entityId;
    @With
    Long eventId;
}
