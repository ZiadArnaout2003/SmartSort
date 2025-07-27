package ca.concordia.smartsortandroidapp;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ResultController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void loadAllResults(MainActivity activity, ArrayList<String> resultList, android.widget.ArrayAdapter<String> adapter) {
        db.collection("predictionHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) return;

                    resultList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String prediction = doc.getString("prediction");
                        String imageUrl = doc.getString("imageUrl");
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        String timeStr = timestamp != null
                                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(timestamp.toDate())
                                : "Unknown Time";

                        resultList.add(timeStr + " - " + prediction);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(activity).load(imageUrl).preload();
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }


    public interface OnResultsFetchedListener {
        void onResults(List<PredictionResult> results);
    }

    public void fetchAllPredictionResults(OnResultsFetchedListener listener) {
        db.collection("predictionHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<PredictionResult> resultList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String prediction = doc.getString("prediction");
                        String imageUrl = doc.getString("imageUrl");
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        resultList.add(new PredictionResult(prediction, imageUrl, timestamp));
                    }
                    listener.onResults(resultList);
                });
    }

    public void listenToPredictionResults(ValueEventListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("predictionHistory")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    List<PredictionResult> resultList = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        String prediction = doc.getString("prediction");
                        String imageUrl = doc.getString("imageUrl");
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (prediction != null && imageUrl != null && timestamp != null) {
                            resultList.add(new PredictionResult(prediction, imageUrl, timestamp));
                        }
                    }
                    listener.onDataChanged(resultList);
                });
    }

    public interface ValueEventListener {
        void onDataChanged(List<PredictionResult> results);
    }


}
