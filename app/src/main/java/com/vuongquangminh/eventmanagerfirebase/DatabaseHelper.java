package com.vuongquangminh.eventmanagerfirebase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper Class
 * Manages SQLite database creation, upgrades, and CRUD operations for Events.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "event_manager.db";
    private static final int DATABASE_VERSION = 2;

    // Table name and columns
    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_RISK = "requires_risk";
    private static final String COLUMN_REPORTER = "reporter";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_EMAIL = "contact_email";
    private static final String COLUMN_PHONE = "contact_phone";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EVENTS_TABLE = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_LOCATION + " TEXT NOT NULL, "
                + COLUMN_DATE + " TEXT NOT NULL, "
                + COLUMN_TIME + " TEXT, "
                + COLUMN_RISK + " INTEGER, "
                + COLUMN_REPORTER + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_PHONE + " TEXT" + ")";
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    // Insert a new event into the database
    public boolean addEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, event.getName());
        values.put(COLUMN_LOCATION, event.getLocation());
        values.put(COLUMN_DATE, event.getDate());
        values.put(COLUMN_TIME, event.getTime() != null ? event.getTime() : "");
        values.put(COLUMN_RISK, event.isRequiresRiskAssessment() ? 1 : 0);
        values.put(COLUMN_REPORTER, event.getReporter() != null ? event.getReporter() : "");
        values.put(COLUMN_DESCRIPTION, event.getDescription());
        values.put(COLUMN_EMAIL, event.getContactEmail());
        values.put(COLUMN_PHONE, event.getContactPhone());

        long result = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return result != -1; // Returns true if insertion is successful
    }

    // Check if an event with the same name and date already exists
    public boolean checkEventExists(String name, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{COLUMN_ID},
                COLUMN_NAME + " = ? AND " + COLUMN_DATE + " = ?",
                new String[]{name, date},
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    // Retrieve all events from the database
    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EVENTS + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME));
                boolean risk = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RISK)) == 1;
                String reporter = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPORTER));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));

                // Instantiate an Event object that aligns with the new constructor
                Event event = new Event(id, name, location, date, time, risk, reporter, description, email, phone);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return eventList;
    }
}