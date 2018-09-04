package com.fmc.phototracker.photo;

/**
 * Created by FMC on 29/11/2017.
 */

public class ListEntries {
    private String name;
    private String details;

    public ListEntries (String name, String details) {
        this.name = name;
        this.details = details;
    }

    public String get_name() {
        return name;
    }

    public String get_details() {
        return details;
    }
}
