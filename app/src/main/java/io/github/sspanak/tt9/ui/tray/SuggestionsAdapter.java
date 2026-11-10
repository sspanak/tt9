package io.github.sspanak.tt9.ui.tray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
	/** Optional item styling supplied by a feature; ordinary row binding stays unchanged. */
	public interface Presentation {
		void bind(View item, TextView text, int position);
		CharSequence text(String candidate, int position);
		int viewType();
	}
	private final Consumer<Integer> onItemClick;
	private final Consumer<Integer> onItemLongClick;
	private final int layout;
	private final int textViewResourceId;
	private final LayoutInflater mInflater;
	private final List<String> mSuggestions;
	private float textSize;

	private int colorDefault;
	private int colorHighlight;
	private int backgroundHighlight;
	private int selectedIndex = 0;
	private Presentation presentation;


	public SuggestionsAdapter(Context context, @NonNull Consumer<Integer> onItemClick, @NonNull Consumer<Integer> onItemLongClick, int layout, int textViewResourceId, List<String> suggestions) {
		this.onItemClick = onItemClick;
		this.onItemLongClick = onItemLongClick;
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
		if (presentation != null) presentation.bind(holder.itemView, holder.suggestionItem, position);
		CharSequence text = presentation != null ? presentation.text(mSuggestions.get(position), position) : mSuggestions.get(position);
		SpannableString scaledText = new SpannableString(text);
		scaledText.setSpan(new RelativeSizeSpan(textSize), 0, scaledText.length(), 0);
		holder.suggestionItem.setText(scaledText);

		holder.suggestionItem.setTag(position);
		holder.suggestionItem.setTextColor(selectedIndex == position ? colorHighlight : colorDefault);
		holder.suggestionItem.setBackgroundColor(selectedIndex == position ? backgroundHighlight : Color.TRANSPARENT);
		holder.suggestionItem.setOnClickListener(v -> onItemClick.accept((int) v.getTag()));
		holder.suggestionItem.setOnLongClickListener(v -> {
			onItemLongClick.accept((int) v.getTag());
			return true;
		});
	}


	@Override
	public int getItemCount() {
		return mSuggestions.size();
	}

	@Override
	public int getItemViewType(int position) {
		return presentation != null ? presentation.viewType() : 0;
	}

	public void setPresentation(Presentation presentation) { this.presentation = presentation; }


	public void setSelection(int newIndex) {
		notifyItemChanged(selectedIndex);
		notifyItemChanged(selectedIndex = newIndex);
	}


	@SuppressLint("NotifyDataSetChanged")
	public void resetItems(int newIndex) {
		selectedIndex = newIndex;
		notifyDataSetChanged();
	}


	public void setTextSize(float size) {
		textSize = size;
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
