// AutocompleteSuggestionAdapter.java
package com.mobdeve.s17.mobdeve.animoquest.project.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mobdeve.s17.mobdeve.animoquest.project.R;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteSuggestionAdapter extends RecyclerView.Adapter<AutocompleteSuggestionAdapter.ViewHolder> {

    public interface OnPlaceClickListener {
        void onPlaceClick(PlaceItem placeItem);
    }

    private List<PlaceItem> placeItems = new ArrayList<>();
    private OnPlaceClickListener listener;

    public AutocompleteSuggestionAdapter(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public void setPlaceItems(List<PlaceItem> placeItems) {
        this.placeItems = placeItems;
        notifyDataSetChanged();
    }

    public void clearPlaceItems() {
        this.placeItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public AutocompleteSuggestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_autocomplete_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AutocompleteSuggestionAdapter.ViewHolder holder, int position) {
        PlaceItem placeItem = placeItems.get(position);
        holder.bind(placeItem, listener);
    }

    @Override
    public int getItemCount() {
        return placeItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPrimaryText;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPrimaryText = itemView.findViewById(R.id.tv_primary_text);
        }

        public void bind(final PlaceItem placeItem, final OnPlaceClickListener listener) {
            tvPrimaryText.setText(placeItem.getName());
            itemView.setOnClickListener(v -> listener.onPlaceClick(placeItem));
        }
    }
}
