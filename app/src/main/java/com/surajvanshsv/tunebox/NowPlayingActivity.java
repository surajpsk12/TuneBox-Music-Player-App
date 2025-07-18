package com.surajvanshsv.tunebox;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.surajvanshsv.tunebox.model.Song;
import com.surajvanshsv.tunebox.utils.SongListHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {

    private TextView songTitle, songArtist, currentTime, totalTime;
    private SeekBar seekBar;
    private ImageButton playPauseBtn, nextBtn, prevBtn;

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songList;
    private int currentPosition;

    private Handler handler = new Handler();

    private final Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentTime.setText(formatDuration(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        currentPosition = getIntent().getIntExtra("position", 0);
        songList = SongListHolder.getInstance().getSongs();

        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        nextBtn = findViewById(R.id.nextBtn);
        prevBtn = findViewById(R.id.prevBtn);

        playSong(currentPosition);

        playPauseBtn.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseBtn.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                playPauseBtn.setImageResource(R.drawable.ic_pause);
                handler.post(updateSeekBar);
            }
        });

        nextBtn.setOnClickListener(v -> {
            if (currentPosition < songList.size() - 1) {
                currentPosition++;
                playSong(currentPosition);
            }
        });

        prevBtn.setOnClickListener(v -> {
            if (currentPosition > 0) {
                currentPosition--;
                playSong(currentPosition);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(formatDuration(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playSong(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        Song song = songList.get(position);
        songTitle.setText(song.getTitle());
        songArtist.setText(song.getArtist());

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(this, song.getUri()); // ✅ Correct method to play from Uri
            mediaPlayer.prepare();
            mediaPlayer.start();

            playPauseBtn.setImageResource(R.drawable.ic_pause);
            seekBar.setMax(mediaPlayer.getDuration());
            totalTime.setText(formatDuration(mediaPlayer.getDuration()));
            handler.post(updateSeekBar);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            if (currentPosition < songList.size() - 1) {
                currentPosition++;
                playSong(currentPosition);
            }
        });
    }

    private String formatDuration(long millis) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
