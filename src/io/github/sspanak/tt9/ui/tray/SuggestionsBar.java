package io.github.sspanak.tt9.ui.tray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
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
import io.github.sspanak.tt9.ime.TraditionalT9;

public class SuggestionsBar {
	private final List<String> suggestions = new ArrayList<>();
	protected int selectedIndex = 0;
	private boolean isDarkThemeEnabled = false;

	private final RecyclerView mView;
	private final TraditionalT9 tt9;
	private SuggestionsAdapter mSuggestionsAdapter;


	public SuggestionsBar(TraditionalT9 tt9, View mainView) {
		super();

		this.tt9 = tt9;

		mView = mainView.findViewById(R.id.suggestions_bar);
		mView.setLayoutManager(new LinearLayoutManager(mainView.getContext(), RecyclerView.HORIZONTAL,false));

		initDataAdapter(mainView.getContext());
		initSeparator(mainView.getContext());
		configureAnimation();
	}


	private void configureAnimation() {
		DefaultItemAnimator animator = new DefaultItemAnimator();

		int translateDuration = tt9.getSettings().getSuggestionTranslateAnimationDuration();
		int selectDuration = tt9.getSettings().getSuggestionSelectAnimationDuration();

		animator.setMoveDuration(selectDuration);
		animator.setChangeDuration(translateDuration);
		animator.setAddDuration(translateDuration);
		animator.setRemoveDuration(translateDuration);

		mView.setItemAnimator(animator);
	}


	private void initDataAdapter(Context context) {
		mSuggestionsAdapter = new SuggestionsAdapter(
			context,
			this,
			tt9.getSettings().getShowSoftNumpad() ? R.layout.suggestion_list_numpad : R.layout.suggestion_list,
			R.id.suggestion_list_item,
			suggestions
		);
		mView.setAdapter(mSuggestionsAdapter);

		setDarkTheme(tt9.getSettings().getDarkTheme());
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


	public boolean hasElements() {
		return suggestions.size() > 0;
	}


	public int getCurrentIndex() {
		return selectedIndex;
	}


	public String getCurrentSuggestion() {
		return getSuggestion(selectedIndex);
	}


	public String getSuggestion(int id) {
		if (id < 0 || id >= suggestions.size()) {
			return "";
		}

		return suggestions.get(id).equals("⏎") ? "\n" : suggestions.get(id);
	}


	@SuppressLint("NotifyDataSetChanged")
	public void setSuggestions(List<String> newSuggestions, int initialSel) {
		ecoSetBackground(newSuggestions);

		suggestions.clear();
		selectedIndex = 0;

		if (newSuggestions != null) {
			for (String suggestion : newSuggestions) {
				// make the new line better readable
				suggestions.add(suggestion.equals("\n") ? "⏎" : suggestion);
			}
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
		isDarkThemeEnabled = darkEnabled;
		Context context = mView.getContext();

		int defaultColor = darkEnabled ? R.color.dark_candidate_color : R.color.candidate_color;
		int highlightColor = darkEnabled ? R.color.dark_candidate_selected : R.color.candidate_selected;

		mSuggestionsAdapter.setColorDefault(ContextCompat.getColor(context, defaultColor));
		mSuggestionsAdapter.setColorHighlight(ContextCompat.getColor(context, highlightColor));

		setBackground(suggestions);
	}


	/**
	 * setBackground
	 * Makes the background transparent, when there are no suggestions and theme-colored,
	 * when there are suggestions.
	 */
	private void setBackground(List<String> newSuggestions) {
		int newSuggestionsSize = newSuggestions != null ? newSuggestions.size() : 0;
		if (newSuggestionsSize == 0) {
			mView.setBackgroundColor(Color.TRANSPARENT);
			return;
		}

		int color = ContextCompat.getColor(
			mView.getContext(),
			isDarkThemeEnabled ? R.color.dark_candidate_background : R.color.candidate_background
		);

		mView.setBackgroundColor(color);
	}


	/**
	 * ecoSetBackground
	 * A performance-optimized version of "setBackground().
	 * Determines if the suggestions have changed and only then it changes the background.
	 */
	private void ecoSetBackground(List<String> newSuggestions) {
		int newSuggestionsSize = newSuggestions != null ? newSuggestions.size() : 0;
		if (
			(newSuggestionsSize == 0 && suggestions.size() == 0)
			|| (newSuggestionsSize > 0 && suggestions.size() > 0)
		) {
			return;
		}

		setBackground(newSuggestions);
	}


	/**
	 * onItemClick
	 * Passes through suggestion selected using the touchscreen.
	 */
	public void onItemClick(int position) {
		selectedIndex = position;
		tt9.onOK();
	}
}
