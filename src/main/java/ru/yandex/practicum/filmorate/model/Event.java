package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.sql.Timestamp;


@Value
@Builder
public class Event {

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
