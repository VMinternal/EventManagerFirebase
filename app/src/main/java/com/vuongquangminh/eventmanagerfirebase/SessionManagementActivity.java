package com.vuongquangminh.eventmanagerfirebase;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Import SearchView
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SessionManagementActivity extends AppCompatActivity {

    private RecyclerView rvSessions;
    private ExtendedFloatingActionButton fabAddSession;
    private SearchView searchViewSessions;

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
        searchViewSessions = findViewById(R.id.searchViewSessions); // Ánh xạ SearchView

        // Setting up RecyclerView
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        sessionList = new ArrayList<>();


        loadSessionsData("");

        // Searching for digital events on SearchView
        searchViewSessions.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadSessionsData(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadSessionsData(newText); // Automatic real-time filtering as you type.
                return true;
            }
        });

        // Handling the event when the button to add a new session is clicked (pass null for Add mode)
        fabAddSession.setOnClickListener(v -> showSessionDialog(null));
    }

    // Load the list of sessions from SQLite into a RecyclerView (Hỗ trợ lọc theo từ khóa)
    private void loadSessionsData(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            sessionList = databaseHelper.getSessionsForEvent(eventId);
        } else {
            sessionList = databaseHelper.searchSessionsForEvent(eventId, keyword);
        }

        // Update the adapter to support both Edit and Delete events.
        sessionAdapter = new SessionAdapter(this, sessionList, new SessionAdapter.OnSessionListener() {
            @Override
            public void onEditClick(Session session) {
                // Open the dialog in Edit mode and pass in the session object to be edited.
                showSessionDialog(session);
            }

            @Override
            public void onDeleteClick(Session session) {
                showDeleteSessionDialog(session);
            }
        });
        rvSessions.setAdapter(sessionAdapter);
    }

    // Display a shared dialog for both adding and editing a session.
    private void showSessionDialog(Session sessionToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_session, null);
        builder.setView(dialogView);

        final EditText etSessionName = dialogView.findViewById(R.id.etDialogSessionName);
        final EditText etSpeaker = dialogView.findViewById(R.id.etDialogSpeaker);
        final EditText etStartTime = dialogView.findViewById(R.id.etDialogStartTime);
        final EditText etEndTime = dialogView.findViewById(R.id.etDialogEndTime);
        final EditText etDescription = dialogView.findViewById(R.id.etDialogDescription);

        // Safely attaching the event to open a TimePickerDialog
        etStartTime.setOnClickListener(v -> showTimePicker(v, etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(v, etEndTime));

        boolean isEdit = (sessionToEdit != null);

        if (isEdit) {
            builder.setTitle("Edit Session");
            // Safely load legacy data into the cells
            if (sessionToEdit.getSessionName() != null) etSessionName.setText(sessionToEdit.getSessionName());
            if (sessionToEdit.getSpeaker() != null) etSpeaker.setText(sessionToEdit.getSpeaker());
            if (sessionToEdit.getStartTime() != null) etStartTime.setText(sessionToEdit.getStartTime());
            if (sessionToEdit.getEndTime() != null) etEndTime.setText(sessionToEdit.getEndTime());
            if (sessionToEdit.getDescription() != null) etDescription.setText(sessionToEdit.getDescription());
        } else {
            builder.setTitle("Add New Session");
        }

        builder.setPositiveButton(isEdit ? "Update" : "Add", (dialog, which) -> {
            try {
                String name = etSessionName.getText() != null ? etSessionName.getText().toString().trim() : "";
                String speaker = etSpeaker.getText() != null ? etSpeaker.getText().toString().trim() : "";
                String startTime = etStartTime.getText() != null ? etStartTime.getText().toString().trim() : "";
                String endTime = etEndTime.getText() != null ? etEndTime.getText().toString().trim() : "";
                String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

                if (name.isEmpty()) {
                    Toast.makeText(SessionManagementActivity.this, "Session Name is required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Retain current search keywords to maintain the filter state after an Add/Update operation.
                String currentQuery = searchViewSessions.getQuery().toString();

                if (isEdit) {
                    if (sessionToEdit == null || sessionToEdit.getId() <= 0) {
                        Toast.makeText(SessionManagementActivity.this, "Error: Invalid Session ID!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sessionToEdit.setSessionName(name);
                    sessionToEdit.setSpeaker(speaker);
                    sessionToEdit.setStartTime(startTime);
                    sessionToEdit.setEndTime(endTime);
                    sessionToEdit.setDescription(description);

                    boolean isUpdated = databaseHelper.updateSession(sessionToEdit);
                    if (isUpdated) {
                        Toast.makeText(SessionManagementActivity.this, "Session updated successfully!", Toast.LENGTH_SHORT).show();
                        loadSessionsData(currentQuery);
                    } else {
                        Toast.makeText(SessionManagementActivity.this, "Failed to update session.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Session newSession = new Session(eventId, name, speaker, startTime, endTime, description);
                    boolean isInserted = databaseHelper.addSession(newSession);

                    if (isInserted) {
                        Toast.makeText(SessionManagementActivity.this, "Session added successfully!", Toast.LENGTH_SHORT).show();
                        loadSessionsData(currentQuery);
                    } else {
                        Toast.makeText(SessionManagementActivity.this, "Failed to add session.", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(SessionManagementActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // A completely safe function for displaying a TimePickerDialog
    private void showTimePicker(View view, EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                view.getContext(),
                (datePicker, selectedHour, selectedMinute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    targetEditText.setText(formattedTime);
                },
                hour, minute, true
        );
        timePickerDialog.show();
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
                        loadSessionsData(searchViewSessions.getQuery().toString());
                    } else {
                        Toast.makeText(this, "Failed to delete session.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}