package com.vuongquangminh.eventmanagerfirebase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnItemClickListener listener;

    // Interface for handling click events on individual items.
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventAdapter(List<Event> eventList, OnItemClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
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

        // Handle the click event on the item card.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    //Refresh the data list (used when searching or adding/deleting)
    public void updateData(List<Event> newEventList) {
        this.eventList = newEventList;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventLocation, tvEventDate, tvEventTime, tvEventReporter;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvEventReporter = itemView.findViewById(R.id.tv_event_reporter);
        }
    }
}