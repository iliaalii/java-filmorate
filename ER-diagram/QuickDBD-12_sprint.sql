-- Упрощенный вариант. создан исключительно для удобного комментирования ревью :)

CREATE TABLE Films (
  film_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name TEXT NOT NULL,
  description VARCHAR(200) NOT NULL,
  release_date DATE NOT NULL,
  duration INTEGER NOT NULL,
  rating_id INTEGER NOT NULL
);

CREATE TABLE Users (
  user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  login TEXT NOT NULL,
  name TEXT NULL,
  email VARCHAR(256) NOT NULL,
  birthday DATE NOT NULL
);

CREATE TABLE Friends (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  user_id INTEGER NOT NULL,
  friend_id INTEGER NOT NULL,
  confirmed boolean NOT NULL,
  FOREIGN KEY (user_id) REFERENCES Users(user_id),
  FOREIGN KEY (friend_id) REFERENCES Users(user_id)
);

CREATE TABLE Likes (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  film_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  FOREIGN KEY (film_id) REFERENCES Films(film_id),
  FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE Rating (
  rating_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE Genres (
  genre_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE Films_Genres (
  id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  film_id INTEGER NOT NULL,
  genre_id INTEGER NOT NULL,
  FOREIGN KEY (film_id) REFERENCES Films(film_id),
  FOREIGN KEY (genre_id) REFERENCES Genres(genre_id)
);