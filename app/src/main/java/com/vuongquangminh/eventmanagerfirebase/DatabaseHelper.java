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
 * Manages SQLite database creation, upgrades, and CRUD operations for Events and Sessions.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "event_manager.db";
    private static final int DATABASE_VERSION = 3;

    // Table Events
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

    // Table Sessions
    private static final String TABLE_SESSIONS = "sessions";
    private static final String COLUMN_SESSION_ID = "session_id";
    private static final String COLUMN_SESSION_EVENT_ID = "event_id";
    private static final String COLUMN_SESSION_NAME = "session_name";
    private static final String COLUMN_SESSION_SPEAKER = "speaker";
    private static final String COLUMN_SESSION_START_TIME = "start_time";
    private static final String COLUMN_SESSION_END_TIME = "end_time";
    private static final String COLUMN_SESSION_DESCRIPTION = "session_description";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // foreign key constraints for SQLite
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create an events table
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

        // Create a sessions table (with a foreign key linked to events)
        String CREATE_SESSIONS_TABLE = "CREATE TABLE " + TABLE_SESSIONS + "("
                + COLUMN_SESSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SESSION_EVENT_ID + " INTEGER NOT NULL, "
                + COLUMN_SESSION_NAME + " TEXT NOT NULL, "
                + COLUMN_SESSION_SPEAKER + " TEXT, "
                + COLUMN_SESSION_START_TIME + " TEXT, "
                + COLUMN_SESSION_END_TIME + " TEXT, "
                + COLUMN_SESSION_DESCRIPTION + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_SESSION_EVENT_ID + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_SESSIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
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
        return result != -1;
    }

    public boolean updateEvent(Event event) {
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

        int rowsAffected = db.update(TABLE_EVENTS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(event.getId())});
        db.close();
        return rowsAffected > 0;
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

                Event event = new Event(id, name, location, date, time, risk, reporter, description, email, phone);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return eventList;
    }

    // Advanced search
    public List<Event> searchEventsAdvanced(String keyword, String date, String organiser) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_EVENTS + " WHERE 1=1");
        List<String> selectionArgs = new ArrayList<>();

        // Filter by keyword (Name or Description)
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(" AND (" + COLUMN_NAME + " LIKE ? OR " + COLUMN_DESCRIPTION + " LIKE ?)");
            selectionArgs.add("%" + keyword.trim() + "%");
            selectionArgs.add("%" + keyword.trim() + "%");
        }

        // Filter by Date
        if (date != null && !date.trim().isEmpty()) {
            query.append(" AND " + COLUMN_DATE + " = ?");
            selectionArgs.add(date.trim());
        }

        // Filter by Reporter
        if (organiser != null && !organiser.trim().isEmpty()) {
            query.append(" AND " + COLUMN_REPORTER + " LIKE ?");
            selectionArgs.add("%" + organiser.trim() + "%");
        }

        query.append(" ORDER BY " + COLUMN_ID + " DESC");

        Cursor cursor = db.rawQuery(query.toString(), selectionArgs.toArray(new String[0]));

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION));
                String eventDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME));
                boolean risk = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RISK)) == 1;
                String reporter = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REPORTER));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE));

                Event event = new Event(id, name, location, eventDate, time, risk, reporter, description, email, phone);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return eventList;
    }

    // Delete Event
    public boolean deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EVENTS, COLUMN_ID + " = ?", new String[]{String.valueOf(eventId)});
        db.close();
        return result > 0;
    }

    // Add a new session belonging to an event
    public boolean addSession(Session session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SESSION_EVENT_ID, session.getEventId());
        values.put(COLUMN_SESSION_NAME, session.getSessionName());
        values.put(COLUMN_SESSION_SPEAKER, session.getSpeaker());
        values.put(COLUMN_SESSION_START_TIME, session.getStartTime());
        values.put(COLUMN_SESSION_END_TIME, session.getEndTime());
        values.put(COLUMN_SESSION_DESCRIPTION, session.getDescription());

        long result = db.insert(TABLE_SESSIONS, null, values);
        db.close();
        return result != -1;
    }

    // Retrieve the list of all sessions for a specific event
    public List<Session> getSessionsForEvent(int eventId) {
        List<Session> sessionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SESSIONS,
                null,
                COLUMN_SESSION_EVENT_ID + " = ?",
                new String[]{String.valueOf(eventId)},
                null, null, COLUMN_SESSION_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SESSION_ID));
                int evId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SESSION_EVENT_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_NAME));
                String speaker = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_SPEAKER));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_START_TIME));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_END_TIME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SESSION_DESCRIPTION));

                Session session = new Session(id, evId, name, speaker, startTime, endTime, description);
                sessionList.add(session);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return sessionList;
    }

    // Delete a session by ID
    public boolean deleteSession(int sessionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_SESSIONS, COLUMN_SESSION_ID + " = ?", new String[]{String.valueOf(sessionId)});
        db.close();
        return result > 0;
    }
}