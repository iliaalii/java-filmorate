MERGE INTO Rating (name) KEY(name) VALUES
    ('G'),
    ('PG'),
    ('PG-13'),
    ('R'),
    ('NC-17');

MERGE INTO Genres (name) KEY(name) VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');