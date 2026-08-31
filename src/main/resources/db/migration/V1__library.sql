CREATE TABLE author (
    id BIGSERIAL PRIMARY KEY,
    alias VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE genre (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE book (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    pages INTEGER NOT NULL,
    published_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE book_author (
    book_id BIGINT NOT NULL REFERENCES book(id),
    author_id BIGINT NOT NULL REFERENCES author(id),
    PRIMARY KEY (book_id, author_id)
);

CREATE TABLE book_genre (
    book_id BIGINT NOT NULL REFERENCES book(id),
    genre_id BIGINT NOT NULL REFERENCES genre(id),
    PRIMARY KEY (book_id, genre_id)
);