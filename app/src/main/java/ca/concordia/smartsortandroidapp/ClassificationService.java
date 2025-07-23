 package ca.concordia.smartsortandroidapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.tensorflow.lite.Interpreter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClassificationService extends Service {

    private static final String CHANNEL_ID = "ClassificationServiceChannel";
    private static final String IMAGE_DOC = "latest";
    private static final String COLLECTION_INPUT = "imageUploads";
    private static final String COLLECTION_OUTPUT = "result";

    private Interpreter interpreter;
    private List<String> labels;
    private final int imageSize = 224;
    private String lastUrl = "";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        createNotificationChannel();
        startForeground(1, getNotification("Classification service started"));
        try {
            AssetManager assetManager = getAssets();
            interpreter = new Interpreter(loadModelFile(assetManager, "model_unquant.tflite"));
            labels = loadLabels(assetManager, "labels.txt");
            listenForImageUpdates();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Notification getNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartSort Classifier")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Classification Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void listenForImageUpdates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_INPUT)
                .document(IMAGE_DOC)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null || !snapshot.exists()) return;

                    String imageUrl = snapshot.getString("url");
                    Timestamp timestamp = snapshot.getTimestamp("timestamp");

                    if (imageUrl != null && !imageUrl.equals(lastUrl)) {
                        lastUrl = imageUrl;
                        Date tsDate = timestamp != null ? timestamp.toDate() : new Date();
                        downloadAndClassifyImage(imageUrl, tsDate);
                    }
                });
    }


    private void downloadAndClassifyImage(String imageUrl, Date timestamp) {
        new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream input = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();

                String prediction = classify(bitmap);
                android.util.Log.d("SmartSort", "📸 Image classified: " + prediction);
                sendBackResult(imageUrl, prediction, timestamp);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void sendBackResult(String imageUrl, String result, Date timestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("prediction", result);
        data.put("timestamp", timestamp);
        data.put("imageUrl", imageUrl);

        String docId = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(timestamp);
        db.collection("predictionHistory")
                .document(docId)
                .set(data);
        Map<String, Object> LatestResult = new HashMap<>();
        LatestResult.put("prediction", result);
        LatestResult.put("timestamp", timestamp);
        LatestResult.put("imageUrl", imageUrl);
        LatestResult.put("status", "done");
        db.collection(COLLECTION_OUTPUT)
                .document("latest")
                .set(LatestResult);
        Intent intent = new Intent("com.smartsort.CLASSIFICATION_COMPLETE");
        sendBroadcast(intent);

    }
    
    public String classify(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true);
        ByteBuffer inputBuffer = convertBitmapToByteBuffer(resized);

        float[][] output = new float[1][labels.size()];
        interpreter.run(inputBuffer, output);

        int maxIdx = 0;
        float maxProb = 0;
        for (int i = 0; i < labels.size(); i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIdx = i;
            }
        }

        if (maxProb < 0.50f) {
            return "2 Others";
        }

        return labels.get(maxIdx);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
        buffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[imageSize * imageSize];
        bitmap.getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize);

        for (int pixel : intValues) {
            buffer.putFloat(((pixel >> 16) & 0xFF) / 255.0f);
            buffer.putFloat(((pixel >> 8) & 0xFF) / 255.0f);
            buffer.putFloat((pixel & 0xFF) / 255.0f);
        }

        return buffer;
    }

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels(AssetManager assetManager, String labelPath) throws IOException {
        List<String> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) result.add(line);
        reader.close();
        return result;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}