package com.spotifx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private UserStore store = new UserStore();
    private User current;
    private MediaPlayer player;

    private List<ITunesApi.Song> songs = new ArrayList<>();
    private int currentIndex = -1;

    // ramden skip gaaketa free momxmarebelma
    private int skipsUsed = 0;
    private static final int FREE_SKIP_LIMIT = 4;

    private Stage stage;
    private Label status;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("SpotiFX");
        showLogin();
        stage.setOnCloseRequest(e -> {
            if (player != null) player.dispose();
        });
        stage.show();
    }

    // shesvla da registracia
    private void showLogin() {
        Label title = new Label("SpotiFX");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPromptText("მომხმარებელი");
        PasswordField passField = new PasswordField();
        passField.setPromptText("პაროლი");

        Button loginBtn = new Button("შესვლა");
        Button registerBtn = new Button("რეგისტრაცია");
        Label msg = new Label();

        loginBtn.setOnAction(e -> {
            User u = store.login(userField.getText().trim(), passField.getText());
            if (u != null) { current = u; showMain(); }
            else msg.setText("არასწორი მონაცემები");
        });

        registerBtn.setOnAction(e -> {
            String un = userField.getText().trim();
            if (un.isEmpty() || passField.getText().isEmpty()) {
                msg.setText("შეავსეთ ყველა ველი");
                return;
            }
            User u = store.register(un, passField.getText());
            if (u != null) { current = u; showMain(); }
            else msg.setText("მომხმარებელი უკვე არსებობს");
        });

        VBox box = new VBox(10, title, userField, passField,
                new HBox(10, loginBtn, registerBtn), msg);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));
        ((HBox) box.getChildren().get(3)).setAlignment(Pos.CENTER);

        stage.setScene(new Scene(box, 360, 320));
    }

    // mtavari fanjari
    private void showMain() {
        Label hello = new Label("გამარჯობა, " + current.getUsername()
                + (current.isPremium() ? " (Premium)" : " (Free)"));
        hello.setStyle("-fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText("ძებნა (მაგ: Imagine Dragons)");
        Button searchBtn = new Button("ძებნა");

        ObservableList<ITunesApi.Song> items = FXCollections.observableArrayList();
        ListView<ITunesApi.Song> list = new ListView<>(items);
        list.setPrefHeight(220);

        status = new Label("მოძებნე სიმღერა");

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            status.setText("იტვირთება...");
            // ar gavaketo UI thread-shi rom ar gaiyinos
            new Thread(() -> {
                List<ITunesApi.Song> found = ITunesApi.search(q);
                Platform.runLater(() -> {
                    songs.clear();
                    songs.addAll(found);
                    items.setAll(found);
                    status.setText(found.isEmpty() ? "ვერ მოიძებნა" : "ნაპოვნია: " + found.size());
                });
            }).start();
        });

        Button playBtn = new Button("▶ ჩართვა");
        Button pauseBtn = new Button("⏸ პაუზა");
        Button stopBtn = new Button("⏹ გაჩერება");
        Button skipBtn = new Button("⏭ შემდეგი");
        Button likeBtn = new Button("♥ მოწონება");

        playBtn.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx < 0) { status.setText("აირჩიე სიმღერა"); return; }
            currentIndex = idx;
            playCurrent();
        });

        pauseBtn.setOnAction(e -> { if (player != null) player.pause(); });
        stopBtn.setOnAction(e -> { if (player != null) player.stop(); });

        skipBtn.setOnAction(e -> {
            if (songs.isEmpty()) return;
            if (!current.isPremium() && skipsUsed >= FREE_SKIP_LIMIT) {
                status.setText("Free ვერსიაში მხოლოდ " + FREE_SKIP_LIMIT
                        + " skip. იყიდე Premium!");
                return;
            }
            if (!current.isPremium()) skipsUsed++;
            currentIndex = (currentIndex + 1) % songs.size();
            list.getSelectionModel().select(currentIndex);
            playCurrent();
        });

        likeBtn.setOnAction(e -> {
            int idx = list.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;
            String name = songs.get(idx).toString();
            if (!current.getLiked().contains(name)) {
                current.getLiked().add(name);
                store.save();
                status.setText("მოწონებულია: " + name);
            } else {
                status.setText("უკვე მოწონებულია");
            }
        });

        Button premiumBtn = new Button(current.isPremium() ? "Premium ✓" : "Premium-ის შეძენა");
        premiumBtn.setDisable(current.isPremium());
        premiumBtn.setOnAction(e -> {
            current.setPremium(true);
            store.save();
            premiumBtn.setText("Premium ✓");
            premiumBtn.setDisable(true);
            hello.setText("გამარჯობა, " + current.getUsername() + " (Premium)");
            status.setText("გილოცავ! ახლა შეუზღუდავი skip გაქვს.");
        });

        Button logoutBtn = new Button("გასვლა");
        logoutBtn.setOnAction(e -> {
            if (player != null) { player.dispose(); player = null; }
            current = null;
            songs.clear();
            skipsUsed = 0;
            currentIndex = -1;
            showLogin();
        });

        HBox controls = new HBox(8, playBtn, pauseBtn, stopBtn, skipBtn, likeBtn);
        controls.setAlignment(Pos.CENTER);

        HBox top = new HBox(10, hello, premiumBtn, logoutBtn);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(10, top, new HBox(8, searchField, searchBtn),
                list, controls, status);
        box.setPadding(new Insets(15));
        HBox.setHgrow(searchField, Priority.ALWAYS);

        stage.setScene(new Scene(box, 520, 480));
    }

    private void playCurrent() {
        if (currentIndex < 0 || currentIndex >= songs.size()) return;
        ITunesApi.Song s = songs.get(currentIndex);
        try {
            if (player != null) player.dispose();
            Media media = new Media(s.previewUrl);
            player = new MediaPlayer(media);
            player.play();
            status.setText("უკრავს: " + s.toString());
        } catch (Exception ex) {
            status.setText("დაკვრის შეცდომა");
        }
    }
}
