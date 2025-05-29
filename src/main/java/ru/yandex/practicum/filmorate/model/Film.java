package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    private Integer id;
    @NotBlank(message = "Название фильма не должно быть пустым")
    private String name;
    @Size(max = 200, message = "Максимальный размер описания не должен превышать 200 символов")
    private String description;
    @NotNull(message = "Дата релиза не должна быть пустой")
    @PastOrPresent(message = "Релиз не может быть в будущем (на данный момент только вышедшие фильмы)")
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность должна быть положительным числом")
    private int duration;
    private Set<Integer> likes = new HashSet<>();
}