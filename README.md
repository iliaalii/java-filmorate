# java-filmorate (12 Sprint)

## Диограммв для p2p проверки
![изображение](src/ER-diagram/QuickDBD-12_sprint.png)

* [Ссылка](https://app.quickdatabasediagrams.com/#/d/i89NpA) на диограмму.
* [Скрипт](src/ER-diagram/QuickDBD-12_sprint.sql) базы данных в MySQL формате.

## Примеры запросов к контроллерам с выгрузкой данных
<sup>С учетом пройденого материала</sup>

### Film
  * вывод всех фильмов
    ```SQL
    SELECT f.film_id,
           f.`name`,
           f.description,
           f.releaseDate,
           f.duration,
           r.`name` AS rating
    FROM Films f
    LEFT JOIN Rating AS r ON f.rating_id = r.rating_id
    ```
  
  * вывод конкретного фильма `id`
    ```SQL
    SELECT f.film_id,
           f.`name`,
           f.description,
           f.releaseDate,
           f.duration,
           r.`name` AS rating
    FROM Films f
    LEFT JOIN Rating AS r ON f.rating_id = r.rating_id
    WHERE f.film_id = /*id*/;
    ```
  * получение фильмов с их жанром
    ```SQL
    SELECT f.film_id,
           f.`name`,
           g.`name` AS genre
    FROM Films f
    LEFT JOIN Film_Genre AS fg ON f.film_id = fg.film_id
    LEFT JOIN Genre AS g ON fg.genre_id = g.genre_id
    GROUP BY f.film_id,
             f.`name`,
             g.`name`;
    ```
    
  * получение `count` популярных фильмов
    ```SQL
    SELECT f.film_id,
           f.`name`,
           f.description,
           f.releaseDate,
           f.duration,
           r.`name` AS rating,
           COUNT(l.user_id) AS likes_count
    FROM Films f
    LEFT JOIN Rating AS r ON f.rating_id = r.rating_id
    LEFT JOIN Likes AS l ON f.film_id = l.film_id
    GROUP BY f.film_id, f.name, f.description, f.releaseDate, f.duration, r.name
    ORDER BY likes_count DESC
    LIMIT /*count*/;
    ```

### User
* вывод всех пользователей
  ```SQL
  SELECT u.user_id,
         u.login,
         u.`name`,
         u.email,
         u.birthday
  FROM Users u;
  ```

* поиск подтвержденных друзей `id`
  ```SQL
  SELECT u.user_id,
         u.login,
         u.`name`,
         u.email,
         u.birthday
  FROM Friends f
  LEFT JOIN Users AS u ON f.friend_id = u.user_id
  WHERE f.user_id = /*id*/
  AND f.confirmed = true;
  ```
  
* поиск общих друзей `id` `friendId`
  ```SQL
  SELECT u.user_id,
         u.login,
         u.`name`,
         u.email,
         u.birthday
  FROM Friends f1
  INNER JOIN Friends f2 ON f1.friend_id = f2.friend_id
  INNER JOIN Users u ON f1.friend_id = u.user_id
  WHERE f1.user_id = /*id*/
  AND f2.user_id = /*friendId*/
  AND f1.confirmed = true
  AND f2.confirmed = true;
  ```

* проверка подтверждения дружбы `id` `friendId`
  ```SQL
  SELECT f.confirmed
  FROM Friends f
  WHERE f.user_id = /*id*/
  AND f.friend_id = /*friendId*/;
  ```
