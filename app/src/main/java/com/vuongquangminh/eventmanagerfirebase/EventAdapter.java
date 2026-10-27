package com.vuongquangminh.eventmanagerfirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private OnItemClickListener itemClickListener;
    private OnEventClickListener sessionListener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }


    public interface OnEventClickListener {
        void onSessionsClick(Event event);
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    // Constructor with full Context and Session Listener updates
    public EventAdapter(Context context, List<Event> eventList, OnEventClickListener sessionListener) {
        this.context = context;
        this.eventList = eventList;
        this.sessionListener = sessionListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvEventName.setText(event.getName());
        holder.tvEventLocation.setText(event.getLocation());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventTime.setText(event.getTime() != null && !event.getTime().isEmpty() ? event.getTime() : "N/A");
        holder.tvEventReporter.setText(event.getReporter());

        holder.btnEdit.setOnClickListener(v -> sessionListener.onEditClick(event));
        holder.btnDelete.setOnClickListener(v -> sessionListener.onDeleteClick(event));

        // Handle the event when the Sessions button is clicked
        holder.btnManageSessions.setOnClickListener(v -> {
            if (sessionListener != null) {
                sessionListener.onSessionsClick(event);
            }
        });

        // Handle the event when the entire card item frame is clicked
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    // Refresh the data list
    public void updateData(List<Event> newEventList) {
        this.eventList = newEventList;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventLocation, tvEventDate, tvEventTime, tvEventReporter;
        Button btnManageSessions;
        ImageButton btnEdit, btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvEventReporter = itemView.findViewById(R.id.tv_event_reporter);
            btnManageSessions = itemView.findViewById(R.id.btnManageSessions);
            btnEdit = itemView.findViewById(R.id.btn_edit_event);
            btnDelete = itemView.findViewById(R.id.btn_delete_event);
        }
    }
}