package com.vuongquangminh.eventmanagerfirebase;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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

        // Initialize the adapter and listen for click events (Sessions, Edit, Delete)
        eventAdapter = new EventAdapter(this, eventList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onSessionsClick(Event event) {
                Intent intent = new Intent(MainActivity.this, SessionManagementActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Event event) {
                // Open the AddEventActivity screen in Edit mode
                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                intent.putExtra("IS_EDIT", true);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Event event) {
                // Confirmation dialog before deleting an event
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete event '" + event.getName() + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            boolean success = databaseHelper.deleteEvent(event.getId());
                            if (success) {
                                Toast.makeText(MainActivity.this, "Event deleted successfully!", Toast.LENGTH_SHORT).show();
                                loadEvents(); // Tải lại danh sách sau khi xóa
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

    // Display the search icon on the title bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Handle the event when the search icon in the menu is clicked
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            showAdvancedSearchDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Function to open the dialog box for entering advanced search criteria
    private void showAdvancedSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_advanced_search, null);
        builder.setView(view);

        EditText edtKeyword = view.findViewById(R.id.edt_search_keyword);
        EditText edtDate = view.findViewById(R.id.edt_search_date);
        EditText edtOrganiser = view.findViewById(R.id.edt_search_organiser);
        Button btnSearch = view.findViewById(R.id.btn_execute_search);

        AlertDialog dialog = builder.create();

        btnSearch.setOnClickListener(v -> {
            String keyword = edtKeyword.getText().toString();
            String date = edtDate.getText().toString();
            String organiser = edtOrganiser.getText().toString();

            // Call the search function from DatabaseHelper
            List<Event> searchResult = databaseHelper.searchEventsAdvanced(keyword, date, organiser);

            // Update results in the RecyclerView
            if (eventAdapter != null) {
                eventAdapter.updateData(searchResult);
            }

            if (searchResult.isEmpty()) {
                Toast.makeText(this, "No matching events found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Find " + searchResult.size() + " event.", Toast.LENGTH_SHORT).show();
            }

            dialog.dismiss();
        });

        dialog.show();
    }
}