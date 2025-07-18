package com.surajvanshsv.tunebox.model;

import java.io.Serializable;

public class Song implements Serializable {
    private String title;
    private String artist;
    private String duration;
    private String filePath;
    private boolean isFavorite;

    public Song(String title, String artist, String duration, String filePath, boolean isFavorite) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.filePath = filePath;
        this.isFavorite = isFavorite;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getDuration() { return duration; }
    public String getFilePath() { return filePath; }
    public boolean isFavorite() { return isFavorite; }

    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
