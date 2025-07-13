package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"reviewId"})
public class Review {
    private Integer reviewId;
    @NotBlank(message = "Обзор не может быть пустым")
    @Size(max = 2000, message = "Максимальный размер обзора не должен превышать 2000 символов")
    private String content;
    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;
    @NotNull(message = "Пользователь обязательно должен быть указан")
    private Integer userId;
    @NotNull(message = "Фильм обязательно должен быть указан")
    private Integer filmId;
    private int useful;
}