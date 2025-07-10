package ca.concordia.smartsortandroidapp;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String PREF_IMAGE_URL = "lastImageUrl";
    private static final String PREF_LAST_RESULT = "lastResult";
    private static final String IMAGE_FILENAME = "last_image.png";

    private TextView resultText;
    private ImageView imageView;
    private ImageClassifier classifier;
    private Bitmap lastBitmap = null;
    private String lastResult = "";
    private String lastImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        resultText = findViewById(R.id.result_text);
        imageView = findViewById(R.id.image_view);

        try {
            AssetManager assetManager = getAssets();
            classifier = new ImageClassifier(assetManager);
        } catch (Exception e) {
            resultText.setText("Model load failed: " + e.getMessage());
            return;
        }

        loadPrefs();
        loadSavedImageAndResult();
        listenForImageUpdates();
    }

    private void loadPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        lastResult = prefs.getString(PREF_LAST_RESULT, "");
        lastImageUrl = prefs.getString(PREF_IMAGE_URL, "");
    }

    private void savePrefs(String imageUrl, String result) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(PREF_IMAGE_URL, imageUrl)
                .putString(PREF_LAST_RESULT, result)
                .apply();
    }

    private void listenForImageUpdates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Toast.makeText(this, "📡 Listening for image changes...", Toast.LENGTH_SHORT).show();

        db.collection("imageUploads")
                .document("latest")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "❌ Firestore error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot == null || !snapshot.exists()) {
                        Toast.makeText(this, "⚠️ No image found yet", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String imageUrl = snapshot.getString("url");
                    if (imageUrl == null) return;

                    if (!imageUrl.equals(lastImageUrl)) {
                        // New image detected
                        downloadAndClassifyImage(imageUrl);
                    } else {
                        // Same image, already classified and stored
                        loadSavedImageAndResult();
                    }
                });
    }

    private void downloadAndClassifyImage(String imageUrl) {
        Toast.makeText(this, "📥 Downloading image...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                InputStream input = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                lastBitmap = bitmap;
                saveBitmapToInternalStorage(bitmap, IMAGE_FILENAME);

                // Classify and save result
                String result = classifier.classify(bitmap);
                lastResult = result;
                savePrefs(imageUrl, result);
                sendBackResult(result);

                runOnUiThread(() -> displayImage(bitmap, result));

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "❌ Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadSavedImageAndResult() {
        File file = new File(getFilesDir(), IMAGE_FILENAME);
        if (file.exists()) {
            lastBitmap = loadBitmapFromInternalStorage(IMAGE_FILENAME);
            if (lastBitmap != null) {
                displayImage(lastBitmap, lastResult);
            } else {
                Toast.makeText(this, "⚠️ Failed to load saved image. Retrying download...", Toast.LENGTH_SHORT).show();
                downloadAndClassifyImage(lastImageUrl);
            }
        }
    }

    private void displayImage(Bitmap bitmap, String result) {
        imageView.setImageBitmap(bitmap);
        resultText.setText("Prediction: " + result);
        Toast.makeText(this, "🖼️ Image loaded and displayed!", Toast.LENGTH_SHORT).show();
    }

    private void saveBitmapToInternalStorage(Bitmap bitmap, String filename) {
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap loadBitmapFromInternalStorage(String filename) {
        try (FileInputStream fis = openFileInput(filename)) {
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendBackResult(String result) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("prediction", result);

        db.collection("result")
                .document("prediction")
                .set(data)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "✅ Result sent back!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
