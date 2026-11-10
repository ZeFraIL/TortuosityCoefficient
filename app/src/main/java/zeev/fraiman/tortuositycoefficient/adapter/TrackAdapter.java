package zeev.fraiman.tortuositycoefficient.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zeev.fraiman.tortuositycoefficient.R;
import zeev.fraiman.tortuositycoefficient.model.Track;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(Track track);
        void onTrackLongClick(Track track);
    }

    private List<Track> tracks;
    private final OnTrackClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public TrackAdapter(List<Track> tracks, OnTrackClickListener listener) {
        this.tracks = tracks;
        this.listener = listener;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, parent, false);
        return new TrackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = tracks.get(position);
        holder.bind(track);
    }

    @Override
    public int getItemCount() {
        return tracks != null ? tracks.size() : 0;
    }

    class TrackViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDate;
        private final TextView tvK;
        private final TextView tvLength;
        private final TextView tvDirect;
        private final TextView tvPoints;

        public TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTrackTitle);
            tvDate = itemView.findViewById(R.id.tvTrackDate);
            tvK = itemView.findViewById(R.id.tvTrackK);
            tvLength = itemView.findViewById(R.id.tvTrackLength);
            tvDirect = itemView.findViewById(R.id.tvTrackDirect);
            tvPoints = itemView.findViewById(R.id.tvTrackPoints);
        }

        public void bind(Track track) {
            tvTitle.setText(track.getTitle() != null ? track.getTitle() : "Маршрут");
            tvDate.setText(dateFormat.format(new Date(track.getStartTime())));

            double k = track.calculateTortuosityK();
            tvK.setText(String.format(Locale.getDefault(), "K = %.2f", k));

            double pathLen = track.calculatePathLength();
            tvLength.setText(String.format(Locale.getDefault(), "Путь: %.0f м", pathLen));

            double directLen = track.calculateDirectDistance();
            tvDirect.setText(String.format(Locale.getDefault(), "Прямая: %.0f м", directLen));

            int ptsCount = track.getPoints() != null ? track.getPoints().size() : 0;
            tvPoints.setText(String.format(Locale.getDefault(), "Точек: %d", ptsCount));

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTrackClick(track);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTrackLongClick(track);
                }
                return true;
            });
        }
    }
}
