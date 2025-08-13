package ca.concordia.smartsortandroidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener; // For Firebase Realtime DB listeners
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ca.concordia.smartsortandroidapp.ResultController;

public class BinStatus extends NavigationBar {

    private TextView bin1StatusView, bin2StatusView;
    private TextView bin1LastFullView, bin2LastFullView;

    private TextView bin1CanCountView, bin1BottleCountView;
    private TextView bin2OthersCountView, bin1TotalCountView;

    private DatabaseReference databaseRef;

    private String lastBin1Status = "";
    private String lastBin2Status = "";

    private long lastNotFullTimestampBin1 = 0;
    private long lastNotFullTimestampBin2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.navigation_bar);


        FrameLayout contentFrame = findViewById(R.id.content_frame);
        View contentView = getLayoutInflater().inflate(R.layout.bin_status, contentFrame, false);
        contentFrame.addView(contentView);


        setupDrawer();


        bin1StatusView = contentView.findViewById(R.id.text_bin1_status);
        bin2StatusView = contentView.findViewById(R.id.text_bin2_status);
        bin1LastFullView = contentView.findViewById(R.id.text_bin1_last_full);
        bin2LastFullView = contentView.findViewById(R.id.text_bin2_last_full);

        bin1CanCountView = contentView.findViewById(R.id.text_bin1_can_count);
        bin1BottleCountView = contentView.findViewById(R.id.text_bin1_bottle_count);
        bin2OthersCountView = contentView.findViewById(R.id.text_bin2_others_count);
        bin1TotalCountView = contentView.findViewById(R.id.text_bin1_total_count);

        databaseRef = FirebaseDatabase.getInstance().getReference();

        loadStatus("bin1", bin1StatusView, bin1LastFullView, "Can_bottles Bin");
        loadStatus("bin2", bin2StatusView, bin2LastFullView, "Others Bin");
    }

    private void loadStatus(String binName, TextView statusView, TextView lastFullView, String RealName) {
        DatabaseReference statusRef = databaseRef.child(binName).child("status");


        statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String initialStatus = snapshot.getValue(String.class);
                if (binName.equals("bin1")) {
                    lastBin1Status = initialStatus != null ? initialStatus : "";
                } else {
                    lastBin2Status = initialStatus != null ? initialStatus : "";
                }

                attachStatusListener(binName, statusView, lastFullView, RealName);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                statusView.setText(RealName + " Status: [Error loading initial status]");
            }
        });



        String notFullPath = "BinStatus/" + binName + "/notfull";
        databaseRef.child(notFullPath).orderByKey().limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        long lastNotFullTimestamp = 0;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            try {
                                lastNotFullTimestamp = Long.parseLong(child.getKey());
                                String formattedTime = formatTimestamp(lastNotFullTimestamp);
                                lastFullView.setText("Last emptied: " + formattedTime);

                                if (binName.equals("bin1")) {
                                    lastNotFullTimestampBin1 = lastNotFullTimestamp;
                                } else {
                                    lastNotFullTimestampBin2 = lastNotFullTimestamp;
                                }

                                updateCountsSince(binName, lastNotFullTimestamp);
                            } catch (NumberFormatException ignored) {
                                lastFullView.setText("Last emptied: [Invalid timestamp]");
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        lastFullView.setText("Last emptied: [Error]");
                    }
                });
    }

    private void attachStatusListener(String binName, TextView statusView, TextView lastFullView, String RealName) {
        DatabaseReference statusRef = databaseRef.child(binName).child("status");

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String currentStatus = snapshot.getValue(String.class);
                statusView.setText(RealName + " Status: " + currentStatus);

                if (currentStatus == null) return;

                String lastStatus = binName.equals("bin1") ? lastBin1Status : lastBin2Status;

                if (!currentStatus.equals(lastStatus)) {
                    String statusPath = "BinStatus/" + binName + "/" + (currentStatus.equals("full") ? "full" : "notfull");
                    DatabaseReference statusLogRef = databaseRef.child(statusPath);

                    statusLogRef.orderByKey().limitToLast(1)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    boolean alreadyLogged = false;
                                    for (DataSnapshot entry : dataSnapshot.getChildren()) {
                                        long lastLoggedTime = Long.parseLong(entry.getKey());
                                        if (System.currentTimeMillis() - lastLoggedTime < 10_000) {
                                            alreadyLogged = true;
                                        }
                                    }

                                    if (!alreadyLogged) {
                                        long timestamp = System.currentTimeMillis();
                                        databaseRef.child(statusPath).child(String.valueOf(timestamp)).setValue(true);

                                        if (currentStatus.equals("not full")) {
                                            String formattedTime = formatTimestamp(timestamp);
                                            lastFullView.setText("Last emptied: " + formattedTime);

                                            if (binName.equals("bin1")) {
                                                lastNotFullTimestampBin1 = timestamp;
                                                updateCountsSince(binName, lastNotFullTimestampBin1);
                                            } else {
                                                lastNotFullTimestampBin2 = timestamp;
                                                updateCountsSince(binName, lastNotFullTimestampBin2);
                                            }
                                        }
                                    }

                                    if (binName.equals("bin1")) {
                                        lastBin1Status = currentStatus;
                                    } else {
                                        lastBin2Status = currentStatus;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {}
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                statusView.setText(RealName + " Status: [Error]");
            }
        });
    }

    private void updateCountsSince(String binName, long sinceTimestamp) {
        ResultController resultController = new ResultController();

        resultController.listenToPredictionResults(new ResultController.ValueEventListener() {
            @Override
            public void onDataChanged(List<PredictionResult> results) {
                final int[] cans = {0};
                final int[] bottles = {0};
                final int[] others = {0};

                for (PredictionResult result : results) {
                    Timestamp ts = result.getTimestamp();
                    if (ts == null || ts.toDate().getTime() < sinceTimestamp) continue;

                    String prediction = result.getPrediction();
                    if (prediction == null) continue;

                    String lowerPrediction = prediction.toLowerCase();

                    if (binName.equals("bin1")) {
                        if (lowerPrediction.contains("can")) {
                            cans[0]++;
                        } else if (lowerPrediction.contains("bottle")) {
                            bottles[0]++;
                        }
                    } else if (binName.equals("bin2")) {
                        if (!lowerPrediction.contains("can") && !lowerPrediction.contains("bottle")) {
                            others[0]++;
                        }
                    }
                }

                int total = cans[0] + bottles[0];

                runOnUiThread(() -> {
                    if (binName.equals("bin1")) {
                        bin1CanCountView.setText("Cans: " + cans[0]);
                        bin1BottleCountView.setText("Bottles: " + bottles[0]);
                        bin1TotalCountView.setText("Total: " + total);
                    } else {
                        bin2OthersCountView.setText("Others: " + others[0]);
                    }
                });
            }
        });
    }





    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(timestamp);
    }
}
