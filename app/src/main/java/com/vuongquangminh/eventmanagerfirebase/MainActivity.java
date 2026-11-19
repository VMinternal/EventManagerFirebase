package com.vuongquangminh.eventmanagerfirebase;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String REALTIME_DB_URL =
            "https://dbtest-7a484-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private RecyclerView recyclerViewEvents;
    private FloatingActionButton fabAddEvent;
    private EventAdapter eventAdapter;
    private DatabaseHelper databaseHelper;
    private List<Event> eventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(this);

        recyclerViewEvents = findViewById(R.id.recycler_view_events);
        fabAddEvent = findViewById(R.id.fab_add_event);

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the Adapter and listen for interaction events (Session, Edit, Delete)
        eventAdapter = new EventAdapter(this, eventList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onSessionsClick(Event event) {
                Intent intent = new Intent(MainActivity.this, SessionManagementActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Event event) {
                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                intent.putExtra("IS_EDIT", true);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Event event) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete event '" + event.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean success = databaseHelper.deleteEvent(event.getId());
                            if (success) {
                                Toast.makeText(MainActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                                loadEvents();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to delete event!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerViewEvents.setAdapter(eventAdapter);

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        eventList = databaseHelper.getAllEvents();
        eventAdapter.updateData(eventList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            showAdvancedSearchDialog();
            return true;
        } else if (id == R.id.action_upload) {
            uploadLocalEventsToCloud();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- CLOUD SYNCHRONIZATION FEATURES (FIREBASE REALTIME DATABASE) ---

    // Check network connection status
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        }
        return false;
    }

    // Upload all data from the local SQLite database to Firebase Realtime Database (Events + Sessions)
    private void uploadLocalEventsToCloud() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection! Please connect to the network before uploading.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Event> localEvents = databaseHelper.getAllEvents();
        if (localEvents.isEmpty()) {
            Toast.makeText(this, "No local events found to upload!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance(REALTIME_DB_URL).getReference();
        DatabaseReference eventsRef = rootRef.child("events");
        DatabaseReference sessionsRef = rootRef.child("sessions");

        final int totalEvents = localEvents.size();
        final int[] completedEvents = {0};
        final boolean[] hasError = {false};

        Toast.makeText(this, "Pushing events and sessions to Firebase...", Toast.LENGTH_SHORT).show();

        for (Event event : localEvents) {
            String eventKey = String.valueOf(event.getId());

            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("name", event.getName());
            eventMap.put("location", event.getLocation());
            eventMap.put("date", event.getDate());
            eventMap.put("time", event.getTime());
            eventMap.put("requiresRiskAssessment", event.isRequiresRiskAssessment());
            eventMap.put("reporter", event.getReporter());
            eventMap.put("description", event.getDescription());
            eventMap.put("contactEmail", event.getContactEmail());
            eventMap.put("contactPhone", event.getContactPhone());

            eventsRef.child(eventKey).setValue(eventMap)
                    .addOnSuccessListener(unused -> {
                        android.util.Log.d("FIREBASE_RTDB", "Event synced ID: " + eventKey);

                        List<Session> sessions = databaseHelper.getSessionsForEvent(event.getId());
                        DatabaseReference currentEventSessionsRef = sessionsRef.child(eventKey);

                        currentEventSessionsRef.removeValue().addOnCompleteListener(removeTask -> {
                            if (sessions != null && !sessions.isEmpty()) {
                                for (Session session : sessions) {
                                    String sessionKey = String.valueOf(session.getId());
                                    Map<String, Object> sessionMap = new HashMap<>();
                                    sessionMap.put("id", session.getId());
                                    sessionMap.put("eventId", event.getId());
                                    sessionMap.put("sessionName", session.getSessionName());
                                    sessionMap.put("speaker", session.getSpeaker());
                                    sessionMap.put("startTime", session.getStartTime());
                                    sessionMap.put("endTime", session.getEndTime());
                                    sessionMap.put("description", session.getDescription());

                                    currentEventSessionsRef.child(sessionKey).setValue(sessionMap)
                                            .addOnFailureListener(e -> {
                                                hasError[0] = true;
                                                android.util.Log.e("FIREBASE_RTDB", "Session sync error: " + e.getMessage());
                                            });
                                }
                            }

                            completedEvents[0]++;
                            if (completedEvents[0] == totalEvents && !hasError[0]) {
                                Toast.makeText(MainActivity.this,
                                        "All events and sessions successfully uploaded to Firebase!",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        hasError[0] = true;
                        completedEvents[0]++;
                        android.util.Log.e("FIREBASE_RTDB", "Event upload error: " + e.getMessage(), e);
                        Toast.makeText(MainActivity.this, "Firebase Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    // --- ADVANCED SEARCH DIALOG BOX ---
    private void showAdvancedSearchDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_advanced_search, null);

        EditText edtKeyword = view.findViewById(R.id.edt_search_keyword);
        EditText edtDate = view.findViewById(R.id.edt_search_date);
        EditText edtOrganiser = view.findViewById(R.id.edt_search_organiser);
        Button btnSearch = view.findViewById(R.id.btn_execute_search);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("Advanced Event Search")
                .setView(view)
                .setNeutralButton("Show All", (d, which) -> {
                    loadEvents();
                    d.dismiss();
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        btnSearch.setOnClickListener(v -> {
            String keyword = edtKeyword.getText().toString().trim();
            String date = edtDate.getText().toString().trim();
            String organiser = edtOrganiser.getText().toString().trim();

            List<Event> searchResult = databaseHelper.searchEventsAdvanced(keyword, date, organiser);

            if (eventAdapter != null) {
                eventAdapter.updateData(searchResult);
            }

            if (searchResult.isEmpty()) {
                Toast.makeText(this, "No matching events found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Found " + searchResult.size() + " event(s).", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}