package ca.concordia.smartsortandroidapp;

import com.google.firebase.Timestamp;

public class PredictionResult {
    private String prediction;
    private final String imageUrl;
    private final Timestamp timestamp;

    public PredictionResult(String prediction, String imageUrl, Timestamp timestamp, String volume) {
        this.prediction = prediction;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;

    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public String getPredictionType() {
        if (prediction != null && prediction.contains(" ")) {
            return prediction.substring(prediction.indexOf(" ") + 1).trim();
        }
        return prediction; // fallback
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}