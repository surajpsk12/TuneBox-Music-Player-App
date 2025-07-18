package com.surajvanshsv.tunebox.utils;

import com.surajvanshsv.tunebox.model.Song;

import java.util.ArrayList;

public class SongListHolder {
    private static final SongListHolder instance = new SongListHolder();
    private ArrayList<Song> songs;

    private SongListHolder() {}

    public static SongListHolder getInstance() {
        return instance;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
}
