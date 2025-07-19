package com.surajvanshsv.tunebox;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.surajvanshsv.tunebox.adapter.SongAdapter;
import com.surajvanshsv.tunebox.model.Song;
import com.surajvanshsv.tunebox.utils.FavoriteManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 123;
    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private FavoriteManager favoriteManager;

    private TextView totalSongsText, totalArtistsText, favouriteSongsText;
    private EditText searchEditText;

    // Mini player views
    private CardView miniPlayer;
    private ProgressBar miniPlayerProgress;
    private TextView miniSongTitle, miniSongArtist;
    private ImageView miniPlayPauseButton, miniNextButton, miniPrevButton;

    private int currentSongIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoriteManager = new FavoriteManager(this);

        songAdapter = new SongAdapter(this, songList, (song, position) -> {
            currentSongIndex = position;
            startNowPlayingActivity();
        });

        recyclerView.setAdapter(songAdapter);

        if (hasPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }
        });

        setupMiniPlayer();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        totalSongsText = findViewById(R.id.totalSongs);
        totalArtistsText = findViewById(R.id.totalArtists);
        favouriteSongsText = findViewById(R.id.favouriteSongs);
        searchEditText = findViewById(R.id.searchEditText);

        // Mini player
        miniPlayer = findViewById(R.id.miniPlayer);
        miniPlayerProgress = findViewById(R.id.miniPlayerProgress);
        miniSongTitle = findViewById(R.id.miniSongTitle);
        miniSongArtist = findViewById(R.id.miniSongArtist);
        miniPlayPauseButton = findViewById(R.id.miniPlayPauseButton);
        miniNextButton = findViewById(R.id.miniNextButton);
        miniPrevButton = findViewById(R.id.miniPrevButton);
    }

    private void setupMiniPlayer() {
        miniPlayer.setOnClickListener(v -> {
            if (currentSongIndex >= 0) {
                startNowPlayingActivity();
            }
        });

        miniPlayPauseButton.setOnClickListener(v -> {
            Toast.makeText(this, "Play/Pause clicked", Toast.LENGTH_SHORT).show();
            // Add playback toggle logic here
        });

        miniNextButton.setOnClickListener(v -> {
            if (currentSongIndex < songList.size() - 1) {
                currentSongIndex++;
                updateMiniPlayer(songList.get(currentSongIndex));
            }
        });

        miniPrevButton.setOnClickListener(v -> {
            if (currentSongIndex > 0) {
                currentSongIndex--;
                updateMiniPlayer(songList.get(currentSongIndex));
            }
        });
    }

    private void updateMiniPlayer(Song song) {
        miniPlayer.setVisibility(View.VISIBLE);
        miniSongTitle.setText(song.getTitle());
        miniSongArtist.setText(song.getArtist());
        miniPlayerProgress.setProgress(0); // Reset or update based on playback position
        // You can add logic to update album art if available
    }

    private void startNowPlayingActivity() {
        Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
        intent.putExtra("songList", new ArrayList<>(songList));
        intent.putExtra("position", currentSongIndex);
        startActivity(intent);
    }

    private void filterSongs(String query) {
        List<Song> filtered = new ArrayList<>();
        for (Song song : songList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(song);
            }
        }
        songAdapter.updateList(filtered);
    }

    private void loadSongs() {
        songList.clear();
        Set<String> artistSet = new HashSet<>();

        Uri collection = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        try (Cursor cursor = getContentResolver().query(collection, projection, selection, null, null)) {
            if (cursor != null) {
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String path = cursor.getString(dataColumn);
                    long durationMs = cursor.getLong(durationColumn);
                    String duration = formatDuration(durationMs);

                    Song song = new Song(title, artist, duration, path, favoriteManager.isFavorite(path));
                    songList.add(song);
                    artistSet.add(artist);
                }
            }
        }

        songAdapter.updateList(songList);

        totalSongsText.setText(String.valueOf(songList.size()));
        totalArtistsText.setText(String.valueOf(artistSet.size()));
        favouriteSongsText.setText(String.valueOf(favoriteManager.getFavoriteSongs(songList).size()));
    }

    private String formatDuration(long durationMs) {
        int minutes = (int) (durationMs / 1000) / 60;
        int seconds = (int) (durationMs / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            Toast.makeText(this, "Permission denied. Cannot display songs.", Toast.LENGTH_SHORT).show();
        }
    }
}
