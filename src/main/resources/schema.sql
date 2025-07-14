create TABLE IF NOT EXISTS Rating (
  rating_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(20) NOT NULL
);

create TABLE IF NOT EXISTS Genres (
  genre_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

create TABLE IF NOT EXISTS Directors (
  director_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);


create TABLE IF NOT EXISTS Films (
  film_id INTEGER AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(200),
  release_date DATE,
  duration INTEGER,
  rating_id INTEGER,
  director_id INTEGER,
  CONSTRAINT films_duration_chk CHECK(duration > 0),
  CONSTRAINT films_release_date_chk CHECK(release_date >= DATE '1895-12-28'),
  FOREIGN KEY (rating_id) REFERENCES Rating(rating_id),
  FOREIGN KEY (director_id) REFERENCES Directors(director_id)
);

create TABLE IF NOT EXISTS Film_Directors (
  film_id INTEGER NOT NULL,
  director_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, director_id),
  FOREIGN KEY (film_id) REFERENCES Films(film_id) ON delete CASCADE,
  FOREIGN KEY (director_id) REFERENCES Directors(director_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS Films_Genres (
  film_id INTEGER NOT NULL,
  genre_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, genre_id),
  FOREIGN KEY (film_id) REFERENCES Films(film_id) ON delete CASCADE,
  FOREIGN KEY (genre_id) REFERENCES Genres(genre_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS Users (
  user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  login VARCHAR(32) NOT NULL,
  name VARCHAR(100),
  email VARCHAR(256) NOT NULL,
  birthday DATE NOT NULL
);

create TABLE IF NOT EXISTS Friends (
  user_id INTEGER NOT NULL,
  friend_id INTEGER NOT NULL,
  PRIMARY KEY (user_id, friend_id),
  FOREIGN KEY (user_id) REFERENCES Users(user_id) ON delete CASCADE,
  FOREIGN KEY (friend_id) REFERENCES Users(user_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS Likes (
  film_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, user_id),

  FOREIGN KEY (film_id) REFERENCES Films(film_id) ON delete CASCADE,
  FOREIGN KEY (user_id) REFERENCES Users(user_id) ON delete CASCADE
);



create TABLE IF NOT EXISTS Reviews (
  review_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  film_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  content VARCHAR(2000) NOT NULL,
  is_positive BOOLEAN NOT NULL,
  FOREIGN KEY (film_id) REFERENCES Films(film_id) ON delete CASCADE,
  FOREIGN KEY (user_id) REFERENCES Users(user_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS Review_Likes (
  review_id   INT NOT NULL,
  user_id     INT NOT NULL,
  is_like     BOOLEAN NOT NULL,
  PRIMARY KEY (review_id, user_id),
  FOREIGN KEY (review_id) REFERENCES Reviews(review_id) ON delete CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS events (
  event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_time TIMESTAMP NOT NULL,
  user_id BIGINT NOT NULL,
  event_type_id TINYINT NOT NULL,
  operation_type_id TINYINT NOT NULL,
  entity_id BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES Users(user_id) ON delete CASCADE
);

