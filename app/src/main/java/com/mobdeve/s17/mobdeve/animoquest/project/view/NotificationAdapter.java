package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobdeve.s17.mobdeve.animoquest.project.R;
import com.mobdeve.s17.mobdeve.animoquest.project.model.NotificationHolder;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationHolder> notificationList;

    public NotificationAdapter(List<NotificationHolder> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationHolder notification = notificationList.get(position);

        holder.title.setText(notification.getTitle());
        holder.subject.setText(notification.getSubject());
        holder.timestamp.setText(notification.getTimestamp());

        if (notification.isRead()) {
            holder.title.setAlpha(0.6f); // Dim the title for read notifications
            holder.statusDot.setVisibility(View.INVISIBLE); // Hide the blue dot for read notifications
        } else {
            holder.title.setAlpha(1.0f); // Normal brightness for unread notifications
            holder.statusDot.setVisibility(View.VISIBLE); // Show the blue dot for unread notifications
        }

        holder.itemView.setOnClickListener(v -> {

            // Show the BottomSheetDialog when clicked
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(holder.itemView.getContext());
            View bottomSheetView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.bottom_sheet_notification, null);

            // Set the title, message, inside the bottom sheet
            TextView title = bottomSheetView.findViewById(R.id.bottom_sheet_title);
            TextView message = bottomSheetView.findViewById(R.id.bottom_sheet_message);
            ImageView imageView = bottomSheetView.findViewById(R.id.bottom_sheet_image);
            Button closeButton = bottomSheetView.findViewById(R.id.btn_close_bottom_sheet);
            NestedScrollView scrollView = bottomSheetView.findViewById(R.id.nestedScrollView);

            // Populate the bottom sheet content
            title.setText(notification.getTitle());
            message.setText(notification.getMessage());
            imageView.setImageResource(R.drawable.logo_dlsu);

            // Mark notification as read
            notification.setRead(true);
            notifyItemChanged(position);

            // Update isRead status in the database
            updateNotificationReadStatus(notification.getId());

            // Close button action
            closeButton.setOnClickListener(v1 -> bottomSheetDialog.dismiss());

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateData(List<NotificationHolder> newNotifications) {
        this.notificationList.clear();
        this.notificationList.addAll(newNotifications);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage, statusDot;
        TextView subject, timestamp, title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            profileImage = itemView.findViewById(R.id.profile_image);
            subject = itemView.findViewById(R.id.subject);
            timestamp = itemView.findViewById(R.id.timestamp);
            statusDot = itemView.findViewById(R.id.status_dot);
        }
    }

    private void updateNotificationReadStatus(String notificationId) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userNotificationsRef = FirebaseDatabase.getInstance()
                .getReference("userNotifications")
                .child(currentUserId)
                .child(notificationId);

        userNotificationsRef.child("isRead").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated
                })
                .addOnFailureListener(e -> {
                    // Log or show error
                    Log.e("NotificationAdapter", "Failed to update read status: " + e.getMessage());
                });
    }

}
