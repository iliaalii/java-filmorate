package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.sql.Timestamp;


@Value
@Builder
public class Event {

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    Timestamp timestamp;
    Long userId;
    EventType eventType;
    Operation operation;
    Long entityId;
    @With
    Long eventId;


    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        REMOVE, ADD, UPDATE
    }
}
