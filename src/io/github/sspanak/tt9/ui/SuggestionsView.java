package io.github.sspanak.tt9.ui;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import android.graphics.drawable.ColorDrawable;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.SuggestionsAdapter;

import java.util.List;

public class SuggestionsView {
	private List<String> suggestions = new ArrayList<>();
	protected int selectedIndex;

	protected LinearLayoutManager mLayoutManager;
	protected DividerItemDecoration mDividerItemDecoration;
	protected SuggestionsAdapter mSuggestionsAdapter;
	protected RecyclerView mRecyclerView;

	public SuggestionsView(View mainView) {
		super();

		mRecyclerView = mainView.findViewById(R.id.main_suggestions_list);

		mLayoutManager = new LinearLayoutManager(mainView.getContext(),
			LinearLayoutManager.HORIZONTAL,false); // reverseLayout=false

		// Adds a vertical divider and sets the color, no extra xml needed
		mDividerItemDecoration = new DividerItemDecoration(
			mRecyclerView.getContext(), mLayoutManager.getOrientation());
		mDividerItemDecoration.setDrawable(new ColorDrawable(R.color.candidate_separator));

		mSuggestionsAdapter = new SuggestionsAdapter(mainView.getContext(),
			R.layout.suggestion_list_view, R.id.suggestion_list_item, suggestions);

		mRecyclerView.setLayoutManager(mLayoutManager);
		mRecyclerView.addItemDecoration(mDividerItemDecoration);
		mRecyclerView.setAdapter(mSuggestionsAdapter);
	}


	public boolean isShown() {
		return suggestions != null && suggestions.size() > 0;
	}


	public int getCurrentIndex() {
		return selectedIndex;
	}


	public String getCurrentSuggestion() {
		return getSuggestion(selectedIndex);
	}


	public String getSuggestion(int id) {
		return suggestions != null && id >= 0 && id < suggestions.size() ? suggestions.get(id) : "";
	}


	public void setSuggestions(List<String> newSuggestions, int initialSel) {
		suggestions.clear();

		if (newSuggestions != null) {
			suggestions.addAll(newSuggestions);
			selectedIndex = Math.max(initialSel, 0);

			mSuggestionsAdapter.notifyDataSetChanged();
		}

		render();
	}


	public void scrollToSuggestion(int increment) {
		if (suggestions == null || suggestions.size() <= 1) {
			return;
		}

		selectedIndex = selectedIndex + increment;
		if (selectedIndex == suggestions.size()) {
			selectedIndex = 0;
		} else if (selectedIndex < 0) {
			selectedIndex = suggestions.size() - 1;
		}

		render();
	}

	private void render() {
		if (mRecyclerView == null || mSuggestionsAdapter == null) {
			Logger.w("SuggestionsView", "Cannot render without a view.");
			return;
		}
		if (suggestions.size() <= 4) {
			selectedIndex = 0;
		}
		mRecyclerView.scrollToPosition(selectedIndex);
		mSuggestionsAdapter.notifyDataSetChanged();

	}
}
