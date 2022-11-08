package io.github.sspanak.tt9.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class SuggestionsView {
	private final List<String> suggestions = new ArrayList<>();
	protected int selectedIndex = 0;

	private final RecyclerView mView;
	private SuggestionsAdapter mSuggestionsAdapter;


	public SuggestionsView(View mainView) {
		super();

		mView = mainView.findViewById(R.id.main_suggestions_list);
		mView.setLayoutManager(new LinearLayoutManager(mainView.getContext(), RecyclerView.HORIZONTAL,false));

		initDataAdapter(mainView.getContext());
		initSeparator(mainView.getContext());
		configureAnimation();
	}


	private void configureAnimation() {
		DefaultItemAnimator animator = new DefaultItemAnimator();

		int translateDuration = SettingsStore.getInstance().getSuggestionTranslateAnimationDuration();
		int selectDuration = SettingsStore.getInstance().getSuggestionSelectAnimationDuration();

		animator.setMoveDuration(selectDuration);
		animator.setChangeDuration(translateDuration);
		animator.setAddDuration(translateDuration);
		animator.setRemoveDuration(translateDuration);

		mView.setItemAnimator(animator);
	}


	private void initDataAdapter(Context context) {
		mSuggestionsAdapter = new SuggestionsAdapter(
			context,
			R.layout.suggestion_list_view,
			R.id.suggestion_list_item,
			suggestions
		);
		mView.setAdapter(mSuggestionsAdapter);

		setDarkTheme(true); // just use some default colors
	}


	private void initSeparator(Context context) {
		// Extra XML is required instead of a ColorDrawable object, because setting the highlight color
		// erases the borders defined using the ColorDrawable.
		Drawable separatorDrawable = ContextCompat.getDrawable(context, R.drawable.suggestion_separator);
		if (separatorDrawable == null) {
			return;
		}

		DividerItemDecoration separator = new DividerItemDecoration(mView.getContext(), RecyclerView.HORIZONTAL);
		separator.setDrawable(separatorDrawable);
		mView.addItemDecoration(separator);
	}


	public boolean isShown() {
		return suggestions.size() > 0;
	}


	public int getCurrentIndex() {
		return selectedIndex;
	}


	public String getCurrentSuggestion() {
		return getSuggestion(selectedIndex);
	}


	public String getSuggestion(int id) {
		return id >= 0 && id < suggestions.size() ? suggestions.get(id) : "";
	}


	@SuppressLint("NotifyDataSetChanged")
	public void setSuggestions(List<String> newSuggestions, int initialSel) {
		suggestions.clear();
		selectedIndex = 0;

		if (newSuggestions != null) {
			suggestions.addAll(newSuggestions);
			selectedIndex = Math.max(initialSel, 0);
		}

		mSuggestionsAdapter.setSelection(selectedIndex);
		mSuggestionsAdapter.notifyDataSetChanged();
		mView.scrollToPosition(selectedIndex);
	}


	public void scrollToSuggestion(int increment) {
		if (suggestions.size() <= 1) {
			return;
		}

		int oldIndex = selectedIndex;

		selectedIndex = selectedIndex + increment;
		if (selectedIndex == suggestions.size()) {
			selectedIndex = 0;
		} else if (selectedIndex < 0) {
			selectedIndex = suggestions.size() - 1;
		}

		mSuggestionsAdapter.setSelection(selectedIndex);
		mSuggestionsAdapter.notifyItemChanged(oldIndex);
		mSuggestionsAdapter.notifyItemChanged(selectedIndex);

		mView.scrollToPosition(selectedIndex);
	}


	/**
	 * setDarkTheme
	 * Changes the suggestion colors according to the theme.
	 *
	 * We need to do this manually, instead of relying on the Context to resolve the appropriate colors,
	 * because this View is part of the main service View. And service Views are always locked to the
	 * system context and theme.
	 *
	 * More info:
	 * https://stackoverflow.com/questions/72382886/system-applies-night-mode-to-views-added-in-service-type-application-overlay
	 */
	public void setDarkTheme(boolean darkEnabled) {
		Context context = mView.getContext();

		int backgroundColor = darkEnabled ? R.color.dark_candidate_background : R.color.candidate_background;
		int defaultColor = darkEnabled ? R.color.dark_candidate_color : R.color.candidate_color;
		int highlightColor = darkEnabled ? R.color.dark_candidate_selected : R.color.candidate_selected;

		mView.setBackgroundColor(ContextCompat.getColor(context, backgroundColor));
		mSuggestionsAdapter.setColorDefault(ContextCompat.getColor(context, defaultColor));
		mSuggestionsAdapter.setColorHighlight(ContextCompat.getColor(context, highlightColor));
	}
}
