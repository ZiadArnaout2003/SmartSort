package ca.concordia.smartsortandroidapp;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<PredictionResult> historyList;
    private final Context context;

    public HistoryAdapter(Context context, List<PredictionResult> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        PredictionResult result = historyList.get(position);

        holder.textPrediction.setText(result.getPrediction());

        if (result.getTimestamp() != null) {
            String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(result.getTimestamp().toDate());
            holder.textTimestamp.setText(formatted);
        } else {
            holder.textTimestamp.setText("No timestamp");
        }

        Glide.with(context)
                .load(result.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imagePreview);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePreview;
        TextView textPrediction, textTimestamp;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePreview = itemView.findViewById(R.id.image_preview);
            textPrediction = itemView.findViewById(R.id.text_prediction);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
        }
    }
}