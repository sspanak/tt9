package io.github.sspanak.tt9;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;

import java.util.List;

public class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {
	private Context context;
	private int layout;
	private int textViewResourceId;
	private LayoutInflater mInflater;
	private List<String> mData;

	public SuggestionsAdapter(Context context, int layout, int textViewResourceId, List<String> data) {
		this.context = context;
		this.layout = layout;
		this.textViewResourceId = textViewResourceId;
		this.mInflater = LayoutInflater.from(context);
		this.mData = data;
	}


	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = mInflater.inflate(layout, parent, false);
		return new ViewHolder(view);
	}


	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		String word = mData.get(position);
		holder.myTextView.setText(word);
	}


	@Override
	public int getItemCount() {
		return mData.size();
	}


	public class ViewHolder extends RecyclerView.ViewHolder {
		TextView myTextView;

		ViewHolder(View itemView) {
			super(itemView);
			myTextView = itemView.findViewById(textViewResourceId);
		}
	}
}
