package io.github.sspanak.tt9.ui.tray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.sspanak.tt9.util.ConsumerCompat;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
	private final ConsumerCompat<Integer> onItemClick;
	private final int layout;
	private final int textViewResourceId;
	private final LayoutInflater mInflater;
	private final List<String> mSuggestions;

	private int colorDefault;
	private int colorHighlight;
	private int backgroundHighlight;
	private int selectedIndex = 0;


	public SuggestionsAdapter(Context context, ConsumerCompat<Integer> onItemClick, int layout, int textViewResourceId, List<String> suggestions) {
		this.onItemClick = onItemClick;
		this.layout = layout;
		this.textViewResourceId = textViewResourceId;
		this.mInflater = LayoutInflater.from(context);
		this.mSuggestions = suggestions;
	}


	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(mInflater.inflate(layout, parent, false));
	}


	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.suggestionItem.setText(mSuggestions.get(position));
		holder.suggestionItem.setTag(position);
		holder.suggestionItem.setTextColor(selectedIndex == position ? colorHighlight : colorDefault);
		holder.suggestionItem.setBackgroundColor(selectedIndex == position ? backgroundHighlight : Color.TRANSPARENT);
		holder.suggestionItem.setOnClickListener(v -> onItemClick.accept((int) v.getTag()));
	}


	@Override
	public int getItemCount() {
		return mSuggestions.size();
	}


	public void setSelection(int newIndex) {
		notifyItemChanged(selectedIndex);
		notifyItemChanged(selectedIndex = newIndex);
	}


	@SuppressLint("NotifyDataSetChanged")
	public void resetItems(int newIndex) {
		selectedIndex = newIndex;
		notifyDataSetChanged();
	}


	public void setColorDefault(int colorDefault) {
		this.colorDefault = colorDefault;
	}


	public void setColorHighlight(int colorHighlight) {
		this.colorHighlight = colorHighlight;
	}


	public void setBackgroundHighlight(int backgroundHighlight) {
		this.backgroundHighlight = backgroundHighlight;
	}


	public class ViewHolder extends RecyclerView.ViewHolder {
		final TextView suggestionItem;

		ViewHolder(View itemView) {
			super(itemView);
			suggestionItem = itemView.findViewById(textViewResourceId);
		}
	}
}
