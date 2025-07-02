package ca.concordia.smartsortandroidapp;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private TextView labelTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        labelTextView = findViewById(R.id.label_text); 

        try {
            ImageClassifier classifier = new ImageClassifier(getAssets());

            StringBuilder results = new StringBuilder();
            AssetManager assetManager = getAssets();
            String[] imageFiles = assetManager.list("test_pictures"); 

            if (imageFiles != null) {
                for (String filename : imageFiles) {
                    try {
                        InputStream is = assetManager.open("test_pictures/" + filename);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        String label = classifier.classify(bitmap);
                        results.append(filename).append(" -> ").append(label).append("\n");
                        is.close();
                    } catch (Exception ignored) {}
                }
            }

            labelTextView.setText(results.toString());

        } catch (Exception e) {
            labelTextView.setText("Error: " + e.getMessage());
        }
    }
}
