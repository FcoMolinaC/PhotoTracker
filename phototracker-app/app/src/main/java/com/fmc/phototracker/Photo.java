package com.fmc.phototracker;

import java.time.LocalDateTime;

public class Photo {
    private LocalDateTime time;
    private String latitude, longitude, description;
    private boolean photo_public;

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPhoto_public() {
        return photo_public;
    }

    public void setPhoto_public(boolean photo_public) {
        this.photo_public = photo_public;
    }
}
