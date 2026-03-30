package com.streamapp.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode);
    }

    private final Context context;
    private List<Episode> episodes;
    private final OnEpisodeClickListener listener;
    private int selectedPosition = -1;

    public EpisodeAdapter(Context context, List<Episode> episodes, OnEpisodeClickListener listener) {
        this.context = context;
        this.episodes = episodes;
        this.listener = listener;
    }

    public void updateEpisodes(List<Episode> newEpisodes) {
        this.episodes = newEpisodes;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode ep = episodes.get(position);
        holder.tvNumber.setText("Ep. " + ep.getEpisodeNumber());
        holder.tvTitle.setText(ep.getShortTitle());
        holder.tvDescription.setText(ep.getDescription());

        boolean isSelected = position == selectedPosition;
        holder.cardView.setCardElevation(isSelected ? 12f : 4f);
        holder.cardView.setCardBackgroundColor(
            context.getResources().getColor(
                isSelected ? R.color.card_selected : R.color.card_background,
                null
            )
        );

        // TV focus handling
        holder.cardView.setOnFocusChangeListener((v, hasFocus) -> {
            v.setScaleX(hasFocus ? 1.05f : 1.0f);
            v.setScaleY(hasFocus ? 1.05f : 1.0f);
        });

        holder.cardView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onEpisodeClick(ep);
        });
    }

    @Override
    public int getItemCount() {
        return episodes != null ? episodes.size() : 0;
    }

    static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvNumber, tvTitle, tvDescription;

        EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvNumber = itemView.findViewById(R.id.tvEpisodeNumber);
            tvTitle = itemView.findViewById(R.id.tvEpisodeTitle);
            tvDescription = itemView.findViewById(R.id.tvEpisodeDescription);
        }
    }
}
