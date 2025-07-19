package com.surajvanshsv.tunebox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.surajvanshsv.tunebox.adapter.SongAdapter;
import com.surajvanshsv.tunebox.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private static final int REQUEST_PERMISSION = 1;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private List<Song> songList = new ArrayList<>();
    private List<Song> originalList = new ArrayList<>();
    private EditText searchEditText;
    private TextView totalSongs, totalArtists, favouriteSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        recyclerView = findViewById(R.id.recyclerView);
        searchEditText = findViewById(R.id.searchEditText);
        totalSongs = findViewById(R.id.totalSongs);
        totalArtists = findViewById(R.id.totalArtists);
        favouriteSongs = findViewById(R.id.favouriteSongs);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongAdapter(this, songList, this); // pass listener
        recyclerView.setAdapter(adapter);

        // Permissions
        if (checkPermission()) {
            loadAudioFiles();
        } else {
            requestPermission();
        }

        // SONGS button click
        totalSongs.setOnClickListener(v -> {
            songList.clear();
            songList.addAll(originalList);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Showing all songs", Toast.LENGTH_SHORT).show();
        });

        // FAVOURITES button click
        favouriteSongs.setOnClickListener(v -> {
            List<Song> favList = new ArrayList<>();
            for (Song song : originalList) {
                if (song.isFavorite()) favList.add(song);
            }
            songList.clear();
            songList.addAll(favList);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Showing favourites", Toast.LENGTH_SHORT).show();
        });

        // ARTISTS button click
        totalArtists.setOnClickListener(v -> {
            Set<String> artists = new HashSet<>();
            for (Song song : originalList) {
                artists.add(song.getArtist());
            }
            Toast.makeText(this, "Unique Artists: " + artists.size(), Toast.LENGTH_SHORT).show();
            // You can later open a new activity or bottom sheet showing all artist names
        });

        // SEARCH BAR text change (live search)
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase();
                songList.clear();
                for (Song song : originalList) {
                    if (song.getTitle().toLowerCase().contains(query) ||
                            song.getArtist().toLowerCase().contains(query)) {
                        songList.add(song);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
    }

    private void loadAudioFiles() {
        songList.clear();
        originalList.clear();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        };

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                long durationMs = cursor.getLong(2);
                String path = cursor.getString(3);

                String duration = formatDuration(durationMs);
                Song song = new Song(title, artist, duration, path, false);
                songList.add(song);
                originalList.add(song);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        }

        updateStats();
    }

    private void updateStats() {
        totalSongs.setText(String.valueOf(originalList.size()));
        long artistCount = originalList.stream().map(Song::getArtist).distinct().count();
        totalArtists.setText(String.valueOf(artistCount));
        favouriteSongs.setText(String.valueOf(getFavouriteCount()));
    }

    private int getFavouriteCount() {
        int count = 0;
        for (Song song : originalList) {
            if (song.isFavorite()) count++;
        }
        return count;
    }

    private String formatDuration(long millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    @Override
    public void onSongClick(Song song, int position) {
        Intent intent = new Intent(this, NowPlayingActivity.class);
        intent.putExtra("selectedSong", song);
        intent.putExtra("position", position);
        intent.putExtra("songList", new ArrayList<>(originalList)); // full list for skip support
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadAudioFiles();
            } else {
                Toast.makeText(this, "Permission denied to read audio files", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
