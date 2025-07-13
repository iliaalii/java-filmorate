package ru.yandex.practicum.filmorate.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationType {
    ADD(1),
    REMOVE(2),
    UPDATE(3);

    private final int id;

    public static OperationType fromId(final int id) {
        return values()[id - 1];
    }
}
