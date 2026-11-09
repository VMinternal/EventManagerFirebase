package com.vuongquangminh.eventmanagerfirebase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * SessionAdapter Class
 * Adapts Session list data into RecyclerView items.
 */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private Context context;
    private List<Session> sessionList;
    private OnSessionListener listener;

    // Interface for handling the event when the session-delete button is clicked
    public interface OnSessionListener {
        void onEditClick(Session session);
        void onDeleteClick(Session session);
    }

    public SessionAdapter(Context context, List<Session> sessionList, OnSessionListener listener) {
        this.context = context;
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessionList.get(position);

        holder.tvSessionName.setText(session.getSessionName());
        holder.tvSpeaker.setText("Speaker: " + (session.getSpeaker() != null && !session.getSpeaker().isEmpty() ? session.getSpeaker() : "N/A"));
        holder.tvSessionTime.setText("Time: " + session.getStartTime() + " - " + session.getEndTime());
        holder.tvSessionDescription.setText(session.getDescription() != null ? session.getDescription() : "");

        // Handle the Edit button click event
        holder.btnEditSession.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(session);
            }
        });

        // Handle the delete button click event
        holder.btnDeleteSession.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionName, tvSpeaker, tvSessionTime, tvSessionDescription;
        ImageButton btnDeleteSession;
        ImageView btnEditSession;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionName = itemView.findViewById(R.id.tvSessionName);
            tvSpeaker = itemView.findViewById(R.id.tvSpeaker);
            tvSessionTime = itemView.findViewById(R.id.tvSessionTime);
            tvSessionDescription = itemView.findViewById(R.id.tvSessionDescription);
            btnEditSession = itemView.findViewById(R.id.btnEditSession);
            btnDeleteSession = itemView.findViewById(R.id.btnDeleteSession);
        }
    }
}