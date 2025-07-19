package com.surajvanshsv.tunebox;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.surajvanshsv.tunebox.model.Song;
import com.surajvanshsv.tunebox.utils.FavoriteManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity {

    private TextView songTitle, songArtist, currentTime, totalTime;
    private ImageButton playPauseBtn, prevBtn, nextBtn, repeatBtn, shuffleBtn, queueBtn;
    private ImageView backButton, favoriteButton, volumeIcon, albumArt;
    private SeekBar seekBar, volumeSeekBar;

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Handler handler = new Handler();

    private ArrayList<Song> songList;
    private int currentIndex = 0;
    private boolean isRepeatEnabled = false;
    private boolean isShuffleEnabled = false;

    private FavoriteManager favoriteManager;

    private Runnable updateSeekRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPos = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(currentPos);
                currentTime.setText(formatTime(currentPos));
                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        initViews();

        favoriteManager = new FavoriteManager(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

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

        setupListeners();
        setupVolumeControl();
        playCurrentSong();
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
        queueBtn = findViewById(R.id.queueBtn);

        backButton = findViewById(R.id.backButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        volumeIcon = findViewById(R.id.volumeIcon);
        albumArt = findViewById(R.id.albumArt);

        seekBar = findViewById(R.id.seekBar);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
    }

    private void setupListeners() {
        playPauseBtn.setOnClickListener(v -> togglePlayPause());

        prevBtn.setOnClickListener(v -> {
            currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
            playCurrentSong();
        });

        nextBtn.setOnClickListener(v -> playNextSong());

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
            }
        });

        favoriteButton.setOnClickListener(v -> {
            Song currentSong = songList.get(currentIndex);
            String path = currentSong.getFilePath();
            boolean isFav = favoriteManager.isFavorite(path);

            if (isFav) {
                favoriteManager.removeFavorite(path);
                favoriteButton.setImageResource(R.drawable.ic_favorite_border);
            } else {
                favoriteManager.addFavorite(path);
                favoriteButton.setImageResource(R.drawable.ic_favorite_filled);
            }
        });

        backButton.setOnClickListener(v -> finish());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(formatTime(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupVolumeControl() {
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeSeekBar.setMax(max);
        volumeSeekBar.setProgress(current);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void playCurrentSong() {
        try {
            mediaPlayer.reset();

            Song currentSong = songList.get(currentIndex);
            songTitle.setText(currentSong.getTitle());
            songArtist.setText(currentSong.getArtist());

            mediaPlayer.setDataSource(currentSong.getFilePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            seekBar.setMax(mediaPlayer.getDuration());
            totalTime.setText(formatTime(mediaPlayer.getDuration()));
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            handler.post(updateSeekRunnable);

            // Show favorite icon correctly
            favoriteButton.setImageResource(
                    favoriteManager.isFavorite(currentSong.getFilePath())
                            ? R.drawable.ic_favorite_filled
                            : R.drawable.ic_favorite_border
            );

            loadAlbumArt(currentSong.getFilePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playNextSong() {
        if (isShuffleEnabled) {
            currentIndex = (int) (Math.random() * songList.size());
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
            handler.post(updateSeekRunnable);
        }
    }

    private void loadAlbumArt(String filePath) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(art, 0, art.length);
                albumArt.setImageBitmap(bitmap);
            } else {
                albumArt.setImageResource(R.drawable.default_album_art);
            }
        } catch (Exception e) {
            albumArt.setImageResource(R.drawable.default_album_art);
        } finally {
            retriever.release();
        }
    }

    private String formatTime(int millis) {
        return String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(updateSeekRunnable);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
