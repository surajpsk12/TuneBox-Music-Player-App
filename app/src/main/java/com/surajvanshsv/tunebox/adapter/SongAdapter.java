package com.surajvanshsv.tunebox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.surajvanshsv.tunebox.R;
import com.surajvanshsv.tunebox.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songList;
    private OnSongClickListener listener;

    public SongAdapter(Context context, List<Song> songList, OnSongClickListener listener) {
        this.context = context;
        this.songList = new ArrayList<>(songList);
        this.listener = listener;
    }

    public interface OnSongClickListener {
        void onSongClick(Song song, int position);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.title.setText(song.getTitle());
        holder.artist.setText(song.getArtist());
        holder.duration.setText(song.getDuration());

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(song, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, duration;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitle);
            artist = itemView.findViewById(R.id.songArtist);
            duration = itemView.findViewById(R.id.songDuration);
        }
    }

    // This method replaces the current list with a new one and refreshes the UI
    public void updateList(List<Song> newList) {
        songList.clear();
        songList.addAll(newList);
        notifyDataSetChanged();
    }
}
