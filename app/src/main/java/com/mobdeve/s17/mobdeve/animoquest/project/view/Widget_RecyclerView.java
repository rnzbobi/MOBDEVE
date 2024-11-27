package com.mobdeve.s17.mobdeve.animoquest.project.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.List;

public class Widget_RecyclerView extends RecyclerView.Adapter<Widget_RecyclerView.MyViewHolder> {
    private List<String> titles;
    private int previousSelectedPosition = 0;

    public Widget_RecyclerView(List<String> titles) {
        this.titles = titles;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.widgetbutton);
        }
    }

    @NonNull
    @Override
    public Widget_RecyclerView.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_indoor_floorsbutton, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Widget_RecyclerView.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String title = titles.get(position);

        holder.itemTitle.setText(title);

        if (position == previousSelectedPosition) {
            holder.itemTitle.setTextColor(Color.parseColor("#FFFFFF"));
            holder.itemTitle.setBackgroundResource(R.drawable.selectednavbutton);
        } else {
            holder.itemTitle.setTextColor(Color.parseColor("#000000"));
            holder.itemTitle.setBackgroundResource(R.drawable.navbutton);
        }

        // Set the OnClickListener for the button
        holder.itemTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousSelectedPosition != position) {
                    notifyItemChanged(previousSelectedPosition);
                    previousSelectedPosition = position;
                    notifyItemChanged(previousSelectedPosition);
                }

                // Handle item click
                if (v.getContext() instanceof IndoorNavigationResultActivity) {
                    IndoorNavigationResultActivity activity = (IndoorNavigationResultActivity) v.getContext();
                    activity.updateViews(title);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (titles != null) ? titles.size() : 0;
    }
}

