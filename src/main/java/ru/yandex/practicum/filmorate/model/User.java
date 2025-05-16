package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"id"})
public class User {
    private Integer id;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^\\S*$", message = "логин не должен содержать пробелы")
    private String login;
    private String name;
    @NotNull
    @NotBlank
    @Email
    private String email;
    @PastOrPresent
    private LocalDate birthday;
}