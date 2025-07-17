package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.model.validation.AfterDate;

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
    @AfterDate(value = "1895-12-27", message = "Дата не ранее 28.12.1895.")
    private LocalDate releaseDate;
    @Positive(message = "Продолжительность должна быть положительным числом")
    private int duration;
    private Rating mpa;
    private Set<Director> directors = new HashSet<>();
    private Set<Genre> genres = new HashSet<>();
    private Set<Integer> likes = new HashSet<>();
}
