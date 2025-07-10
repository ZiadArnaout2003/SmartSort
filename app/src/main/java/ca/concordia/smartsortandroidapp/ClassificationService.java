//package ca.concordia.smartsortandroidapp;
//
//import android.app.Service;
//import android.content.Intent;
//
//public class ClassificationService extends Service {
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        runClassification();  // Run in background thread
//        return START_STICKY;
//    }
//
//    private void runClassification() {
//        // Your ML model or classification logic
//        String result = classifyImage();
//
//        // Send result to Controller (via Broadcast, LiveData, etc.)
//        sendResultToController(result);
//    }
//}
