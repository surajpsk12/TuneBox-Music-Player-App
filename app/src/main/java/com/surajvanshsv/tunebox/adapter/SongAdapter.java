package com.surajvanshsv.tunebox.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.surajvanshsv.tunebox.NowPlayingActivity;
import com.surajvanshsv.tunebox.R;
import com.surajvanshsv.tunebox.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private Context context;
    private int currentPlayingPosition = -1;

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    public void setCurrentPlayingPosition(int position) {
        int oldPosition = currentPlayingPosition;
        currentPlayingPosition = position;
        notifyItemChanged(oldPosition);
        notifyItemChanged(currentPlayingPosition);
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = songList.get(position);

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());
        holder.songDuration.setText(song.getDuration());

        holder.playButton.setVisibility(position == currentPlayingPosition ? View.VISIBLE : View.GONE);

        holder.favoriteIcon.setImageResource(
                song.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
        );

        holder.favoriteIcon.setOnClickListener(v -> {
            song.setFavorite(!song.isFavorite());
            notifyItemChanged(position);
        });

        holder.moreOptions.setOnClickListener(v -> {
            // TODO: Add popup menu options (e.g., share, add to playlist)
        });

        // Highlight the selected song (optional)
        if (position == currentPlayingPosition) {
            holder.itemView.setBackgroundResource(R.color.selected_song_bg);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
        }

        // Launch NowPlayingActivity on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NowPlayingActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("songList", new ArrayList<>(songList));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle, songArtist, songDuration;
        ImageView playButton, favoriteIcon, moreOptions;

        public SongViewHolder(View itemView) {
            super(itemView);
            songTitle = itemView.findViewById(R.id.songTitle);
            songArtist = itemView.findViewById(R.id.songArtist);
            songDuration = itemView.findViewById(R.id.songDuration);
            playButton = itemView.findViewById(R.id.playButton);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            moreOptions = itemView.findViewById(R.id.moreOptions);
        }
    }

    public void updateList(List<Song> newList) {
        this.songList = newList;
        notifyDataSetChanged();
    }
}
