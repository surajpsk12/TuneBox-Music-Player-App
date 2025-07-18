package com.surajvanshsv.tunebox;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.surajvanshsv.tunebox.adapter.SongAdapter;
import com.surajvanshsv.tunebox.model.Song;
import com.surajvanshsv.tunebox.utils.SongListHolder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestPermissionAndLoadSongs();
    }

    private void requestPermissionAndLoadSongs() {
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_AUDIO};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSongs() {
        songList = getAllAudioFiles();

        if (songList.isEmpty()) {
            Toast.makeText(this, "No audio files found", Toast.LENGTH_SHORT).show();
            return;
        }

        songAdapter = new SongAdapter(this, songList, position -> {
            // Save the full song list globally
            SongListHolder.getInstance().setSongs(songList);

            // Navigate to NowPlayingActivity
            Intent intent = new Intent(MainActivity.this, NowPlayingActivity.class);
            intent.putExtra("songIndex", position);
            startActivity(intent);
        });

        recyclerView.setAdapter(songAdapter);
    }

    private ArrayList<Song> getAllAudioFiles() {
        ArrayList<Song> songs = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media._ID
        };

        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null) {
                int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);

                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleIndex);
                    String artist = cursor.getString(artistIndex);
                    long duration = cursor.getLong(durationIndex);
                    long id = cursor.getLong(idIndex);

                    Uri contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

                    songs.add(new Song(title, artist, duration, contentUri));
                }
            }
        } catch (Exception e) {
            Log.e("SongLoader", "Error loading songs: " + e.getMessage());
        }

        return songs;
    }
}
