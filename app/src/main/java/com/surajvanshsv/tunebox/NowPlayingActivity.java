package com.surajvanshsv.tunebox;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.surajvanshsv.tunebox.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {

    private TextView songTitle, songArtist, currentTime, totalTime;
    private ImageButton playPauseBtn, prevBtn, nextBtn, repeatBtn, shuffleBtn;
    private SeekBar seekBar, volumeSeekBar;

    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable updateSeek;

    private ArrayList<Song> songList;
    private int currentIndex = 0;

    private boolean isRepeatEnabled = false;
    private boolean isShuffleEnabled = false;
    private HashSet<Integer> shuffleHistory = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        initViews();
        handler = new Handler();

        songList = (ArrayList<Song>) getIntent().getSerializableExtra("songList");
        currentIndex = getIntent().getIntExtra("position", 0);

        if (songList == null || songList.isEmpty()) {
            finish();
            return;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeatEnabled) {
                playCurrentSong();
            } else {
                playNextSong();
            }
        });

        playCurrentSong();

        updateSeek = () -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                handler.postDelayed(updateSeek, 1000);
            }
        };

        setListeners();
    }

    private void initViews() {
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        prevBtn = findViewById(R.id.prevBtn);
        nextBtn = findViewById(R.id.nextBtn);
        repeatBtn = findViewById(R.id.repeatBtn);
        shuffleBtn = findViewById(R.id.shuffleBtn);
        seekBar = findViewById(R.id.seekBar);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setListeners() {
        playPauseBtn.setOnClickListener(v -> togglePlayPause());

        nextBtn.setOnClickListener(v -> playNextSong());

        prevBtn.setOnClickListener(v -> {
            if (currentIndex > 0) {
                currentIndex--;
            } else {
                currentIndex = songList.size() - 1;
            }
            playCurrentSong();
        });

        repeatBtn.setOnClickListener(v -> {
            isRepeatEnabled = !isRepeatEnabled;
            repeatBtn.setAlpha(isRepeatEnabled ? 1f : 0.5f);
        });

        shuffleBtn.setOnClickListener(v -> {
            isShuffleEnabled = !isShuffleEnabled;
            shuffleBtn.setAlpha(isShuffleEnabled ? 1f : 0.5f);

            if (isShuffleEnabled) {
                Collections.shuffle(songList);
                currentIndex = 0;
                playCurrentSong();
            } else {
                shuffleHistory.clear();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress(70);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                mediaPlayer.setVolume(volume, volume);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playCurrentSong() {
        try {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.reset();

            Song song = songList.get(currentIndex);
            mediaPlayer.setDataSource(song.getFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            songTitle.setText(song.getTitle());
            songArtist.setText(song.getArtist());

            seekBar.setMax(mediaPlayer.getDuration());
            totalTime.setText(formatTime(mediaPlayer.getDuration()));
            playPauseBtn.setImageResource(R.drawable.ic_pause);

            handler.post(updateSeek);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNextSong() {
        if (isShuffleEnabled) {
            int newIndex;
            do {
                newIndex = (int) (Math.random() * songList.size());
            } while (newIndex == currentIndex && songList.size() > 1);
            currentIndex = newIndex;
        } else {
            currentIndex = (currentIndex + 1) % songList.size();
        }
        playCurrentSong();
    }

    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow);
        } else {
            mediaPlayer.start();
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            handler.post(updateSeek);
        }
    }

    private String formatTime(int millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeek);
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
