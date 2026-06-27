# PostgreSQL setup

The app keeps users, the premium flag and liked songs in PostgreSQL
(database `javaspotify`) instead of the old `users.dat` file.

## 1. Install PostgreSQL
Grab it from https://www.postgresql.org/download/windows/, set a password for
the `postgres` user and keep the default port `5432`.

## 2. Create the database
```
psql -U postgres -f database/setup.sql
```
The app creates the tables itself the first time it runs.

## 3. Tell the app the password
Defaults are host `localhost`, port `5432`, db `javaspotify`, user `postgres`.
The password is read from an env var:
```
setx SPOTIFX_DB_PASSWORD "postgres"
```
(or `PGPASSWORD`). If nothing is set it falls back to `postgres`.

You can override `SPOTIFX_DB_HOST`, `SPOTIFX_DB_PORT`, `SPOTIFX_DB_NAME` and
`SPOTIFX_DB_USER` the same way if your setup is different.

## 4. Run
```
mvn javafx:run
```
