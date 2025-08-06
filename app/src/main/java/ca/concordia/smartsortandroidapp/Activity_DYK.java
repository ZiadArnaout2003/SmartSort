package ca.concordia.smartsortandroidapp;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Random;

public class Activity_DYK extends NavigationBar {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_bar);
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View contentView = getLayoutInflater().inflate(R.layout.activity_didyouknow, contentFrame, false);
        contentFrame.addView(contentView);

        setupDrawer();


        viewPager = contentView.findViewById(R.id.view_pager);
        tabLayout = contentView.findViewById(R.id.tab_layout);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Initialize the first facts on the card viewer
        Random random = new Random();
        int randomPosition = random.nextInt(adapter.getItemCount());
        viewPager.setCurrentItem(randomPosition, false);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText("")
        ).attach();
    }

    static class ViewPagerAdapter extends FragmentStateAdapter {
        private final String[] facts;

        public ViewPagerAdapter(@NonNull Activity_DYK fragmentActivity) {
            super(fragmentActivity);
            String[] loadedFacts = null;
            try {
                AssetManager assetManager = fragmentActivity.getAssets();
                InputStream inputStream = assetManager.open("facts.json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }
                reader.close();

                // Parse JSON using Gson  telechargé depuis internet
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, String[]>>(){}.getType();
                Map<String, String[]> jsonMap = gson.fromJson(jsonString.toString(), type);
                loadedFacts = jsonMap.get("facts");
                if (loadedFacts == null || loadedFacts.length == 0) {
                    Log.e("Activity_DYK", "No facts found in JSON or JSON is empty");
                }
            } catch (IOException e) {
                Log.e("Activity_DYK", "Error loading facts.json: " + e.getMessage());
            } catch (Exception e) {
                Log.e("Activity_DYK", "Error parsing JSON: " + e.getMessage());
            }

            // In case the json file is not loading we can use these instead
            facts = (loadedFacts != null && loadedFacts.length > 0) ? loadedFacts : new String[] {
                    "Did you know? Recycling one glass bottle saves enough energy to light a 100-watt bulb for four hours.",
                    "Did you know? Recycling cardboard only takes 75% of the energy required to make new cardboard.",
                    "Did you know? Each ton of recycled paper can save 17 trees, 380 gallons of oil, and 7,000 gallons of water.",
                    "Did you know? Recycling is a $200 billion industry in the U.S.",
                    "Did you know? Recycling 1 ton of cardboard saves 46 gallons of oil."
            };
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return new FactFragment(facts[position]);
        }

        @Override
        public int getItemCount() {
            return facts.length;
        }
    }

    public static class FactFragment extends Fragment {
        private final String factText;

        public FactFragment(String factText) {
            this.factText = factText;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_fact, container, false);
            TextView textView = view.findViewById(R.id.fact_text);
            textView.setText(factText);
            return view;
        }
    }
}