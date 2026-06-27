package com.spotifx;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class ITunesApi {

    public static class Song {
        public String title;
        public String artist;
        public String previewUrl;
        public Song(String title, String artist, String previewUrl) {
            this.title = title;
            this.artist = artist;
            this.previewUrl = previewUrl;
        }
        public String toString() { return title + " - " + artist; }
    }

    // itunes-is api-dan vigeb simgerebs
    public static List<Song> search(String query) {
        List<Song> result = new ArrayList<>();
        try {
            String q = URLEncoder.encode(query, "UTF-8");
            URL url = new URL("https://itunes.apple.com/search?term=" + q
                    + "&entity=song&limit=25");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JsonObject root = JsonParser.parseString(sb.toString()).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("results");
            for (int i = 0; i < data.size(); i++) {
                JsonObject t = data.get(i).getAsJsonObject();
                if (!t.has("previewUrl")) continue;
                String preview = t.get("previewUrl").getAsString();
                if (preview == null || preview.isEmpty()) continue;
                String title = t.has("trackName") ? t.get("trackName").getAsString() : "Unknown";
                String artist = t.has("artistName") ? t.get("artistName").getAsString() : "Unknown";
                result.add(new Song(title, artist, preview));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
