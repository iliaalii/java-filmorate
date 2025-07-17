create TABLE IF NOT EXISTS rating (
  rating_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(20) NOT NULL
);

create TABLE IF NOT EXISTS genres (
  genre_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

create TABLE IF NOT EXISTS directors (
  director_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);


create TABLE IF NOT EXISTS films (
  film_id INTEGER AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  description VARCHAR(200),
  release_date DATE,
  duration INTEGER,
  rating_id INTEGER,
  director_id INTEGER,
  CONSTRAINT films_duration_chk CHECK(duration > 0),
  CONSTRAINT films_release_date_chk CHECK(release_date >= DATE '1895-12-28'),
  FOREIGN KEY (rating_id) REFERENCES rating(rating_id),
  FOREIGN KEY (director_id) REFERENCES directors(director_id)
);

create TABLE IF NOT EXISTS film_directors (
  film_id INTEGER NOT NULL,
  director_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, director_id),
  FOREIGN KEY (film_id) REFERENCES films(film_id) ON delete CASCADE,
  FOREIGN KEY (director_id) REFERENCES directors(director_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS films_genres (
  film_id INTEGER NOT NULL,
  genre_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, genre_id),
  FOREIGN KEY (film_id) REFERENCES films(film_id) ON delete CASCADE,
  FOREIGN KEY (genre_id) REFERENCES genres(genre_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS users (
  user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  login VARCHAR(32) NOT NULL,
  name VARCHAR(100),
  email VARCHAR(256) NOT NULL,
  birthday DATE NOT NULL
);

create TABLE IF NOT EXISTS friends (
  user_id INTEGER NOT NULL,
  friend_id INTEGER NOT NULL,
  PRIMARY KEY (user_id, friend_id),
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE,
  FOREIGN KEY (friend_id) REFERENCES users(user_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS likes (
  film_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  PRIMARY KEY (film_id, user_id),

  FOREIGN KEY (film_id) REFERENCES films(film_id) ON delete CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE
);



create TABLE IF NOT EXISTS reviews (
  review_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  film_id INTEGER NOT NULL,
  user_id INTEGER NOT NULL,
  content VARCHAR(2000) NOT NULL,
  is_positive BOOLEAN NOT NULL,
  FOREIGN KEY (film_id) REFERENCES films(film_id) ON delete CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS review_likes (
  review_id   INT NOT NULL,
  user_id     INT NOT NULL,
  is_like     BOOLEAN NOT NULL,
  PRIMARY KEY (review_id, user_id),
  FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON delete CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS events (
  event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  event_time TIMESTAMP NOT NULL,
  user_id BIGINT NOT NULL,
  event_type_id TINYINT NOT NULL,
  operation_type_id TINYINT NOT NULL,
  entity_id BIGINT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE
);

