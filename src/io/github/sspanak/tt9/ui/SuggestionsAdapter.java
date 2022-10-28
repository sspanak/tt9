package io.github.sspanak.tt9.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
	private final int colorHighlight;
	private final int layout;
	private final int textViewResourceId;
	private final LayoutInflater mInflater;
	private final List<String> mSuggestions;

	private int selectedIndex = 0;


	public SuggestionsAdapter(Context context, int layout, int textViewResourceId, int highLightColor, List<String> suggestions) {
		this.colorHighlight = highLightColor;
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
		holder.suggestionItem.setBackgroundColor(selectedIndex == position ? colorHighlight : Color.TRANSPARENT);
	}


	@Override
	public int getItemCount() {
		return mSuggestions.size();
	}


	public void setSelection(int index) {
		selectedIndex = index;
	}


	public class ViewHolder extends RecyclerView.ViewHolder {
		TextView suggestionItem;

		ViewHolder(View itemView) {
			super(itemView);
			suggestionItem = itemView.findViewById(textViewResourceId);
		}
	}
}
