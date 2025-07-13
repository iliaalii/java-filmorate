package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    LIKE(1),
    REVIEW(2),
    FRIEND(3);

    private final int id;

    public static EventType fromId(final int id) {
        return values()[id - 1];
    }
}
