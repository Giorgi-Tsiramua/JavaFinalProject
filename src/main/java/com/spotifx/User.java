package com.spotifx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String username;
    private String password;
    private boolean premium;
    private List<String> liked = new ArrayList<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.premium = false;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }
    public List<String> getLiked() { return liked; }
}
