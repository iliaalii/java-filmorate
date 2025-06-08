-- Упрощенный вариант. создан исключительно для удобного комментирования ревью :)
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

CREATE TABLE Films (
  film_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name TEXT NOT NULL,
  description VARCHAR(200) NOT NULL,
  release_date DATE,
  duration INTEGER,
  rating_id INTEGER,
  CONSTRAINT films_duration_chk CHECK(duration > 0),
  CONSTRAINT films_release_date_chk CHECK(release_date >= DATE '1895-12-28'),
  FOREIGN KEY (rating_id) REFERENCES Rating(rating_id)
);

CREATE TABLE Users (
  user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  login TEXT NOT NULL,
  name TEXT,
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