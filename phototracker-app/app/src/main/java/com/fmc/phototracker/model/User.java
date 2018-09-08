package com.fmc.phototracker.model;

import java.util.Arrays;

public class User {
    private String username;
    private String password;
    private String email;
    //private int[] tracks;
    //private int[] photo;

    public User() {
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        //this.tracks = tracks;
        //this.photo = photo;
    }

    /*public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }*/

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /*public int[] getTracks() {
        return tracks;
    }

    public void setTracks(int[] tracks) {
        this.tracks = tracks;
    }

    public int[] getPhoto() {
        return photo;
    }

    public void setPhoto(int[] photo) {
        this.photo = photo;
    }*/

    @Override
    public String toString() {
        return "User{" +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
