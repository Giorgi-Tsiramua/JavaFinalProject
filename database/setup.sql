-- JavaSpotify PostgreSQL setup
-- Run ONCE as the postgres superuser to create the database:
--
--   psql -U postgres -f db/setup.sql
--
-- CREATE DATABASE cannot run inside a transaction block, so keep it on its own line.

CREATE DATABASE javaspotify;

-- After the database exists, the app creates these tables on first launch.
-- Shown here only for reference:
--
-- \c javaspotify
--
-- CREATE TABLE IF NOT EXISTS users (
--     username TEXT PRIMARY KEY,
--     password TEXT NOT NULL,
--     premium  BOOLEAN DEFAULT FALSE
-- );
--
-- CREATE TABLE IF NOT EXISTS liked_songs (
--     username TEXT NOT NULL REFERENCES users(username),
--     song     TEXT NOT NULL,
--     PRIMARY KEY (username, song)
-- );
