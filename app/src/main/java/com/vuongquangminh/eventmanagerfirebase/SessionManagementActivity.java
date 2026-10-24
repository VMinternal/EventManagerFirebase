package com.vuongquangminh.eventmanagerfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class SessionManagementActivity extends AppCompatActivity {

    private RecyclerView rvSessions;
    private ExtendedFloatingActionButton fabAddSession;

    private DatabaseHelper databaseHelper;
    private SessionAdapter sessionAdapter;
    private List<Session> sessionList;

    private int eventId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_management);

        // Retrieve the eventId passed from the previous screen's Intent
        eventId = getIntent().getIntExtra("EVENT_ID", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Error: Invalid Event ID!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);

        // View Mapping
        rvSessions = findViewById(R.id.rvSessions);
        fabAddSession = findViewById(R.id.fabAddSession);

        // Setting up RecyclerView
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        sessionList = new ArrayList<>();

        loadSessionsData();

        // Handling the event when the button to add a new session is clicked
        fabAddSession.setOnClickListener(v -> showAddSessionDialog());
    }

    // Load the list of sessions from SQLite into a RecyclerView
    private void loadSessionsData() {
        sessionList = databaseHelper.getSessionsForEvent(eventId);
        sessionAdapter = new SessionAdapter(this, sessionList, session -> {
            // Handling the session deletion action
            showDeleteSessionDialog(session);
        });
        rvSessions.setAdapter(sessionAdapter);
    }

    // Display an input dialog to add a new session
    private void showAddSessionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_session, null);
        builder.setView(dialogView);

        final EditText etSessionName = dialogView.findViewById(R.id.etDialogSessionName);
        final EditText etSpeaker = dialogView.findViewById(R.id.etDialogSpeaker);
        final EditText etStartTime = dialogView.findViewById(R.id.etDialogStartTime);
        final EditText etEndTime = dialogView.findViewById(R.id.etDialogEndTime);
        final EditText etDescription = dialogView.findViewById(R.id.etDialogDescription);

        builder.setTitle("Add New Session")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etSessionName.getText().toString().trim();
                    String speaker = etSpeaker.getText().toString().trim();
                    String startTime = etStartTime.getText().toString().trim();
                    String endTime = etEndTime.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(SessionManagementActivity.this, "Session Name is required!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create a new Session object and save it to SQLite
                    Session newSession = new Session(eventId, name, speaker, startTime, endTime, description);
                    boolean isInserted = databaseHelper.addSession(newSession);

                    if (isInserted) {
                        Toast.makeText(SessionManagementActivity.this, "Session added successfully!", Toast.LENGTH_SHORT).show();
                        loadSessionsData();
                    } else {
                        Toast.makeText(SessionManagementActivity.this, "Failed to add session.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    // Show confirmation before deleting a session
    private void showDeleteSessionDialog(Session session) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Session")
                .setMessage("Are you sure you want to delete session: " + session.getSessionName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean isDeleted = databaseHelper.deleteSession(session.getId());
                    if (isDeleted) {
                        Toast.makeText(this, "Session deleted!", Toast.LENGTH_SHORT).show();
                        loadSessionsData();
                    } else {
                        Toast.makeText(this, "Failed to delete session.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}