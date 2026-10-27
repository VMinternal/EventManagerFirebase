package com.vuongquangminh.eventmanagerfirebase;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private TextInputEditText etEventName, etLocation, etDate, etTime, etReporter, etDescription, etContactEmail, etContactPhone;
    private TextInputLayout tilDate;
    private MaterialButton btnSaveEvent;

    private DatabaseHelper databaseHelper;
    private boolean isEditMode = false;
    private int eventId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initViews();
        databaseHelper = new DatabaseHelper(this);

        etDate.setOnClickListener(v -> showDatePicker());
        btnSaveEvent.setOnClickListener(v -> processFormSubmission());

        // Kiểm tra xem Activity có được mở ở chế độ Sửa (Edit Mode) hay không
        if (getIntent() != null) {
            isEditMode = getIntent().getBooleanExtra("IS_EDIT", false);
            if (isEditMode) {
                eventId = getIntent().getIntExtra("EVENT_ID", -1);
                if (eventId != -1) {
                    setTitle("Edit Event");
                    loadEventDataForEditing(eventId);
                }
            }
        }
    }

    private void initViews() {
        etEventName = findViewById(R.id.etEventName);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        tilDate = findViewById(R.id.tilDate);
        etTime = findViewById(R.id.etTime);
        etReporter = findViewById(R.id.etReporter);

        etDescription = findViewById(R.id.etDescription);
        etContactEmail = findViewById(R.id.etContactEmail);
        etContactPhone = findViewById(R.id.etContactPhone);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
    }

    // Populate the form with existing data when in Edit mode
    private void loadEventDataForEditing(int id) {
        List<Event> allEvents = databaseHelper.getAllEvents();
        for (Event e : allEvents) {
            if (e.getId() == id) {
                etEventName.setText(e.getName());
                etLocation.setText(e.getLocation());
                etDate.setText(e.getDate());
                if (etTime != null) etTime.setText(e.getTime());
                if (etReporter != null) etReporter.setText(e.getReporter());
                etDescription.setText(e.getDescription());
                etContactEmail.setText(e.getContactEmail());
                etContactPhone.setText(e.getContactPhone());
                break;
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etDate.setText(formattedDate);
                    tilDate.setError(null);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void processFormSubmission() {
        String name = etEventName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = (etTime != null && etTime.getText() != null) ? etTime.getText().toString().trim() : "";
        String reporter = (etReporter != null && etReporter.getText() != null) ? etReporter.getText().toString().trim() : "";
        String description = etDescription.getText().toString().trim();
        String email = etContactEmail.getText().toString().trim();
        String phone = etContactPhone.getText().toString().trim();

        // Data validation
        if (name.isEmpty()) {
            etEventName.setError("Please enter the event name!");
            etEventName.requestFocus();
            return;
        }
        etEventName.setError(null);

        if (location.isEmpty()) {
            etLocation.setError("Please enter the location!");
            etLocation.requestFocus();
            return;
        }
        etLocation.setError(null);

        if (date.isEmpty()) {
            tilDate.setError("Please select the date of the event!");
            etDate.requestFocus();
            return;
        }
        tilDate.setError(null);

        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etContactEmail.setError("Invalid email format!");
            etContactEmail.requestFocus();
            return;
        }
        etContactEmail.setError(null);

        if (!phone.isEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
            etContactPhone.setError("Invalid phone number format!");
            etContactPhone.requestFocus();
            return;
        }
        etContactPhone.setError(null);

        // Check for duplicates only when adding new entries
        if (!isEditMode && databaseHelper.checkEventExists(name, date)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Duplicate Event")
                    .setMessage("An event with the same name and date already exists in the database!")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        Event event;
        if (isEditMode) {
            event = new Event(eventId, name, location, date, time, false, reporter, description, email, phone);
        } else {
            event = new Event(name, location, date, description, email, phone);
        }

        showConfirmationDialog(event);
    }

    private void showConfirmationDialog(Event event) {
        String message = "Please review the event details:\n\n" +
                "• Event Name: " + event.getName() + "\n" +
                "• Location: " + event.getLocation() + "\n" +
                "• Date: " + event.getDate() + "\n" +
                "• Time: " + (event.getTime() == null || event.getTime().isEmpty() ? "N/A" : event.getTime()) + "\n" +
                "• Reporter: " + (event.getReporter() == null || event.getReporter().isEmpty() ? "(None)" : event.getReporter()) + "\n" +
                "• Description: " + (event.getDescription().isEmpty() ? "(None)" : event.getDescription()) + "\n" +
                "• Contact Email: " + (event.getContactEmail().isEmpty() ? "(None)" : event.getContactEmail()) + "\n" +
                "• Contact Phone: " + (event.getContactPhone().isEmpty() ? "(None)" : event.getContactPhone());

        new MaterialAlertDialogBuilder(this)
                .setTitle(isEditMode ? "Update Confirmation" : "Event Confirmation")
                .setMessage(message)
                .setPositiveButton("Confirm & Save", (dialog, which) -> {
                    boolean success;
                    if (isEditMode) {
                        success = databaseHelper.updateEvent(event);
                    } else {
                        success = databaseHelper.addEvent(event);
                    }

                    if (success) {
                        showSuccessDialog();
                    } else {
                        Toast.makeText(this, "Failed to save event to database!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Success")
                .setMessage(isEditMode ? "Event updated successfully!" : "Event saved successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}