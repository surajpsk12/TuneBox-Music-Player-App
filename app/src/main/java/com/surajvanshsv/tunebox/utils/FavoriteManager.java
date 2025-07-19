package com.surajvanshsv.tunebox.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.surajvanshsv.tunebox.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoriteManager {
    private static final String PREF_NAME = "favorites_pref";
    private static final String KEY_FAVORITES = "favorite_songs";

    private final SharedPreferences preferences;

    public FavoriteManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addFavorite(String path) {
        Set<String> favorites = new HashSet<>(getFavorites());
        favorites.add(path);
        saveFavorites(favorites);
    }

    public void removeFavorite(String path) {
        Set<String> favorites = new HashSet<>(getFavorites());
        favorites.remove(path);
        saveFavorites(favorites);
    }

    public boolean isFavorite(String path) {
        return getFavorites().contains(path);
    }

    public Set<String> getFavorites() {
        return new HashSet<>(preferences.getStringSet(KEY_FAVORITES, new HashSet<>()));
    }

    private void saveFavorites(Set<String> favorites) {
        preferences.edit().putStringSet(KEY_FAVORITES, favorites).apply();
    }

    // Toggle favorite (used in UI)
    public boolean toggleFavorite(String path) {
        boolean isFav = isFavorite(path);
        if (isFav) {
            removeFavorite(path);
        } else {
            addFavorite(path);
        }
        return !isFav;
    }

    // Get only favorite songs from full list
    public List<Song> getFavoriteSongs(List<Song> allSongs) {
        Set<String> favoritePaths = getFavorites();
        List<Song> favoriteSongs = new ArrayList<>();

        for (Song song : allSongs) {
            if (favoritePaths.contains(song.getFilePath())) {
                favoriteSongs.add(song);
            }
        }
        return favoriteSongs;
    }
}
