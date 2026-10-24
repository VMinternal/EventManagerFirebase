package com.vuongquangminh.eventmanagerfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

        databaseHelper = new DatabaseHelper(this);

        recyclerViewEvents = findViewById(R.id.recycler_view_events);
        fabAddEvent = findViewById(R.id.fab_add_event);

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this, eventList, new EventAdapter.OnEventClickListener() {
            @Override
            public void onSessionsClick(Event event) {
                Intent intent = new Intent(MainActivity.this, SessionManagementActivity.class);
                intent.putExtra("EVENT_ID", event.getId());
                startActivity(intent);
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
}