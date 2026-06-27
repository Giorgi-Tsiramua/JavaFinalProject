package com.spotifx;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UserStore {

    private Connection connection;
    private final Map<String, User> users = new HashMap<>();

    public UserStore() {
        try {
            String host = env("SPOTIFX_DB_HOST", "localhost");
            String port = env("SPOTIFX_DB_PORT", "5432");
            String db   = env("SPOTIFX_DB_NAME", "javaspotify");
            String user = env("SPOTIFX_DB_USER", "postgres");
            String pass = firstNonNull(
                    System.getenv("SPOTIFX_DB_PASSWORD"),
                    System.getenv("PGPASSWORD"),
                    "postgres");

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;
            connection = DriverManager.getConnection(url, user, pass);
            createTables();
            loadAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement st = connection.createStatement();
        st.execute("CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY," +
                "password TEXT NOT NULL," +
                "premium BOOLEAN DEFAULT FALSE)");
        st.execute("CREATE TABLE IF NOT EXISTS liked_songs (" +
                "username TEXT NOT NULL REFERENCES users(username)," +
                "song TEXT NOT NULL," +
                "PRIMARY KEY (username, song))");
        st.close();
    }

    private void loadAll() throws SQLException {
        // bazidan yvela momxmarebeli chavtvirtot mexsierebashi
        users.clear();
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT username, password, premium FROM users");
        while (rs.next()) {
            User u = new User(rs.getString("username"), rs.getString("password"));
            u.setPremium(rs.getBoolean("premium"));
            users.put(u.getUsername(), u);
        }
        rs.close();

        for (User u : users.values()) {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT song FROM liked_songs WHERE username=?");
            ps.setString(1, u.getUsername());
            ResultSet lrs = ps.executeQuery();
            while (lrs.next()) u.getLiked().add(lrs.getString("song"));
            lrs.close();
            ps.close();
        }
        st.close();
    }

    public void save() {
        try {
            for (User u : users.values()) {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO users (username, password, premium) VALUES (?,?,?) " +
                        "ON CONFLICT (username) DO UPDATE SET password=EXCLUDED.password, " +
                        "premium=EXCLUDED.premium");
                ps.setString(1, u.getUsername());
                ps.setString(2, u.getPassword());
                ps.setBoolean(3, u.isPremium());
                ps.executeUpdate();
                ps.close();

                PreparedStatement del = connection.prepareStatement(
                        "DELETE FROM liked_songs WHERE username=?");
                del.setString(1, u.getUsername());
                del.executeUpdate();
                del.close();

                PreparedStatement ins = connection.prepareStatement(
                        "INSERT INTO liked_songs (username, song) VALUES (?,?) " +
                        "ON CONFLICT DO NOTHING");
                for (String song : u.getLiked()) {
                    ins.setString(1, u.getUsername());
                    ins.setString(2, song);
                    ins.addBatch();
                }
                ins.executeBatch();
                ins.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User register(String username, String password) {
        if (users.containsKey(username)) return null;
        User u = new User(username, password);
        users.put(username, u);
        save();
        return u;
    }

    public User login(String username, String password) {
        User u = users.get(username);
        if (u != null && u.getPassword().equals(password)) return u;
        return null;
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    private static String firstNonNull(String... values) {
        for (String v : values) if (v != null && !v.isEmpty()) return v;
        return "";
    }
}
