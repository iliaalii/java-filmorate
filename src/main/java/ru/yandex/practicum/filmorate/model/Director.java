package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"id"})
public class Director {
    private Integer id;
    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
}
