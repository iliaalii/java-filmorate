-- Exported from QuickDBD: https://www.quickdatabasediagrams.com/
-- Link to schema: https://app.quickdatabasediagrams.com/#/d/i89NpA
-- NOTE! If you have used non-SQL datatypes in your design, you will have to change these here.


CREATE TABLE `Films` (
    `film_id` int  NOT NULL ,
    `name` text  NOT NULL ,
    `description` varchar(200)  NOT NULL ,
    `releaseDate` date  NOT NULL ,
    `duration` int  NOT NULL ,
    `rating_id` int  NOT NULL ,
    PRIMARY KEY (
        `film_id`
    )
);

CREATE TABLE `Users` (
    `user_id` int  NOT NULL ,
    `login` text  NOT NULL ,
    `name` text  NOT NULL ,
    `email` varchar(256)  NOT NULL ,
    `birthday` date  NOT NULL ,
    PRIMARY KEY (
        `user_id`
    )
);

CREATE TABLE `Friends` (
    `user_id` int  NOT NULL ,
    `friend_id` int  NOT NULL ,
    `confirmed` boolean  NOT NULL ,
    PRIMARY KEY (
        `user_id`,`friend_id`
    )
);

CREATE TABLE `Likes` (
    `film_id` int  NOT NULL ,
    `user_id` int  NOT NULL ,
    PRIMARY KEY (
        `film_id`,`user_id`
    )
);

CREATE TABLE `Rating` (
    `rating_id` int  NOT NULL ,
    `name` text  NOT NULL ,
    PRIMARY KEY (
        `rating_id`
    )
);

CREATE TABLE `Film_Genre` (
    `film_id` int  NOT NULL ,
    `genre_id` int  NOT NULL ,
    PRIMARY KEY (
        `film_id`
    )
);

CREATE TABLE `Genre` (
    `genre_id` int  NOT NULL ,
    `name` text  NOT NULL ,
    PRIMARY KEY (
        `genre_id`
    )
);

ALTER TABLE `Films` ADD CONSTRAINT `fk_Films_rating_id` FOREIGN KEY(`rating_id`)
REFERENCES `Rating` (`rating_id`);

ALTER TABLE `Friends` ADD CONSTRAINT `fk_Friends_user_id` FOREIGN KEY(`user_id`)
REFERENCES `Users` (`user_id`);

ALTER TABLE `Friends` ADD CONSTRAINT `fk_Friends_friend_id` FOREIGN KEY(`friend_id`)
REFERENCES `Users` (`user_id`);

ALTER TABLE `Likes` ADD CONSTRAINT `fk_Likes_film_id` FOREIGN KEY(`film_id`)
REFERENCES `Films` (`film_id`);

ALTER TABLE `Likes` ADD CONSTRAINT `fk_Likes_user_id` FOREIGN KEY(`user_id`)
REFERENCES `Users` (`user_id`);

ALTER TABLE `Film_Genre` ADD CONSTRAINT `fk_Film_Genre_film_id` FOREIGN KEY(`film_id`)
REFERENCES `Films` (`film_id`);

ALTER TABLE `Film_Genre` ADD CONSTRAINT `fk_Film_Genre_genre_id` FOREIGN KEY(`genre_id`)
REFERENCES `Genre` (`genre_id`);

