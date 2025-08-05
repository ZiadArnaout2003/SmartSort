package ca.concordia.smartsortandroidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Arrays;
import java.util.List;

public class DidYouKnowActivity extends AppCompatActivity {

    private TextView factText;
    private LinearLayout dotsContainer;
    private List<String> facts;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_did_you_know);

        factText = findViewById(R.id.dyk_fact_text);
        dotsContainer = findViewById(R.id.dots_container);
        Button backBtn = findViewById(R.id.btn_back_dashboard);

        // Sample facts (replace with Firebase + Gemini API later)
        facts = Arrays.asList(
                "Recycling one plastic bottle can save enough energy to power a 60‑watt light bulb for up to 6 hours.",
                "Glass can be recycled endlessly without loss of quality.",
                "Recycling aluminum saves 95% of the energy required to make new aluminum."
        );

        updateFact();
        setupDots();

        backBtn.setOnClickListener(v -> finish()); // Go back to dashboard
    }

    private void updateFact() {
        factText.setText(facts.get(currentIndex));
        updateDotsUI();
    }

    private void setupDots() {
        dotsContainer.removeAllViews();
        for (int i = 0; i < facts.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 6);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == currentIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
    }

    private void updateDotsUI() {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            dot.setBackgroundResource(i == currentIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
}
