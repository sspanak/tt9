package io.github.sspanak.tt9.ui;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;

public class SuggestionsView {
	private List<String> suggestions = new ArrayList<>();
	protected int selectedIndex;

	private final ListView view;
	private final ArrayAdapter<String> listAdapter;


	public SuggestionsView(View mainView) {
		super();
		listAdapter = new ArrayAdapter<>(mainView.getContext(), R.layout.suggestion_list_view, R.id.suggestion_list_item, suggestions);
		view = mainView.findViewById(R.id.main_suggestions_list);
		view.setAdapter(listAdapter);
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


	public void setSuggestions(List<String> suggestions, int initialSel) {
		clear();

		if (suggestions != null) {
			this.suggestions = suggestions;
			selectedIndex = Math.max(initialSel, 0);

			listAdapter.addAll(suggestions);
			listAdapter.notifyDataSetChanged();
		}

		render();
	}


	protected void clear() {
		suggestions.clear();
		selectedIndex = -1;

		listAdapter.clear();
		listAdapter.notifyDataSetChanged();
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
		if (view == null || listAdapter == null) {
			Logger.w("SuggestionsView", "Cannot render without a view.");
			return;
		}

		view.setSelection(selectedIndex);

		if (suggestions.size() <= 4) {
			view.scrollTo(0,0);
		}
	}
}
