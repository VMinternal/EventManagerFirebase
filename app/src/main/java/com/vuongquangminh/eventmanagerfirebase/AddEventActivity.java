package com.vuongquangminh.eventmanagerfirebase;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    private TextInputEditText etEventName, etLocation, etDate, etReporter, etDescription;
    private TextInputLayout tilDate;
    private RadioGroup rgRiskAssessment;
    private RadioButton rbRiskYes;
    private MaterialButton btnSaveEvent;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initViews();

        databaseHelper = new DatabaseHelper(this);

        // Open DatePickerDialog when clicking the date field
        etDate.setOnClickListener(v -> showDatePicker());

        // Handle create event button click
        btnSaveEvent.setOnClickListener(v -> processFormSubmission());
    }

    private void initViews() {
        etEventName = findViewById(R.id.etEventName);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        tilDate = findViewById(R.id.tilDate);
        etReporter = findViewById(R.id.etReporter);
        etDescription = findViewById(R.id.etDescription);
        rgRiskAssessment = findViewById(R.id.rgRiskAssessment);
        rbRiskYes = findViewById(R.id.rbRiskYes);
        btnSaveEvent = findViewById(R.id.btnSaveEvent);
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
        String reporter = etReporter.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean requiresRisk = rbRiskYes.isChecked();

        // Validation: Check mandatory fields
        if (name.isEmpty()) {
            etEventName.setError("The event name cannot be left blank!");
            etEventName.requestFocus();
            return;
        } else {
            etEventName.setError(null);
        }

        if (location.isEmpty()) {
            etLocation.setError("The location field cannot be left blank!");
            etLocation.requestFocus();
            return;
        } else {
            etLocation.setError(null);
        }

        if (date.isEmpty()) {
            tilDate.setError("Please select the date of the event!");
            etDate.requestFocus();
            return;
        } else {
            tilDate.setError(null);
        }

        if (reporter.isEmpty()) {
            etReporter.setError("The reporter's name cannot be left blank!");
            etReporter.requestFocus();
            return;
        } else {
            etReporter.setError(null);
        }

        // Initialize Event object
        Event event = new Event(name, location, date, "", requiresRisk, reporter, description);

        // Display confirmation dialog
        showConfirmationDialog(event);
    }

    private void showConfirmationDialog(Event event) {
        String message = "Please confirm the event details:\n\n" +
                "• Event name: " + event.getName() + "\n" +
                "• Location: " + event.getLocation() + "\n" +
                "• Date of the event: " + event.getDate() + "\n" +
                "• Reporter: " + event.getReporter() + "\n" +
                "• Risk assessment required: " + (event.isRequiresRiskAssessment() ? "Yes" : "No") + "\n" +
                "• Description: " + (event.getDescription().isEmpty() ? "(None)" : event.getDescription());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Event Confirmation")
                .setMessage(message)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    boolean isInserted = databaseHelper.addEvent(event);
                    if (isInserted) {
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
                .setMessage("Event saved to SQLite database successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}