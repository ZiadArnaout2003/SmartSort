package ca.concordia.smartsortandroidapp;

import com.google.firebase.Timestamp;

public class PredictionResult {
    private final String prediction;
    private final String imageUrl;
    private final Timestamp timestamp;

    public PredictionResult(String prediction, String imageUrl, Timestamp timestamp) {
        this.prediction = prediction;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
    }

    public String getPrediction() {
        return prediction;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
