package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DirectorRepository.class, DirectorRowMapper.class,
FilmRowMapper.class})
class DirectorDbStorageTest {

    private final DirectorRepository directorStorage;
    private final JdbcTemplate jdbc;

    private Director director;

    @BeforeEach
    void setup() {
        director = new Director();
        director.setName("Кристофер Нолан");
    }

    @Test
    void testCreateAndFindDirector() {
        director = directorStorage.create(director);

        Optional<Director> found = Optional.ofNullable(directorStorage.findDirector(director.getId()));
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(d ->
                        assertThat(d).hasFieldOrPropertyWithValue("name", "Кристофер Нолан"));
    }

    @Test
    void testUpdateDirector() {
        director = directorStorage.create(director);
        director.setName("Обновленный режиссер");

        directorStorage.update(director);
        Director updated = directorStorage.findDirector(director.getId());

        assertThat(updated.getName()).isEqualTo("Обновленный режиссер");
    }

    @Test
    void testFindAllDirectors() {
        directorStorage.create(director);

        Collection<Director> all = directorStorage.findAllDirectors();

        assertThat(all).isNotEmpty();
        assertThat(all.stream().anyMatch(d -> d.getName().equals("Кристофер Нолан"))).isTrue();
    }

    @Test
    void testRemoveDirector() {
        director = directorStorage.create(director);
        Integer id = director.getId();

        directorStorage.removeDirector(id);

        assertThatThrownBy(() -> directorStorage.findDirector(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Указанный режиссер не найден");
    }

    @Test
    void testFindNonExistentDirector() {
        assertThatThrownBy(() -> directorStorage.findDirector(9999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Указанный режиссер не найден");
    }
}
