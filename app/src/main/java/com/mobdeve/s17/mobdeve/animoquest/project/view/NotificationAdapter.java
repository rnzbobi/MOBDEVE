package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
        holder.profileImage.setImageResource(notification.getProfileImageResId());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the BottomSheetDialog when clicked
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(holder.itemView.getContext());
                View bottomSheetView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.bottom_sheet_notification, null);

                // Set the title, message, and image inside the bottom sheet
                TextView title = bottomSheetView.findViewById(R.id.bottom_sheet_title);
                TextView message = bottomSheetView.findViewById(R.id.bottom_sheet_message);
                ImageView imageView = bottomSheetView.findViewById(R.id.bottom_sheet_image);
                ImageView posterView = bottomSheetView.findViewById(R.id.bottom_sheet_poster);
                Button closeButton = bottomSheetView.findViewById(R.id.btn_close_bottom_sheet);

                // Set the notification details
                title.setText(notification.getTitle());
                message.setText(notification.getMessage());

                // Set the image for the notification (use notification's profile image or placeholder)
                imageView.setImageResource(notification.getProfileImageResId());

                // Set the poster image (if available)
                if (notification.getPosterImageResId() != 0) {
                    posterView.setVisibility(View.VISIBLE);
                    posterView.setImageResource(notification.getPosterImageResId());
                } else {
                    posterView.setVisibility(View.GONE);  // Hide the poster if not available
                }

                // Close button action
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView subject, timestamp, title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            profileImage = itemView.findViewById(R.id.profile_image);
            subject = itemView.findViewById(R.id.subject);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }
}

