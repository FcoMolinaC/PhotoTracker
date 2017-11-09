package com.fmc.phototracker;

import java.time.LocalDateTime;

public class Track {
    private LocalDateTime time;
    private String name, description;
    private boolean track_public;

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isTrack_public() {
        return track_public;
    }

    public void setTrack_public(boolean track_public) {
        this.track_public = track_public;
    }
}
