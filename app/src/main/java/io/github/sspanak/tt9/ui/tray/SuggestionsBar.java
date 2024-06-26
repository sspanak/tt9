package io.github.sspanak.tt9.ui.tray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;

public class SuggestionsBar {
	private double lastClickTime = 0;
	private final List<String> suggestions = new ArrayList<>();
	protected int selectedIndex = 0;
	private boolean isDarkThemeEnabled = false;

	private final ResizableMainView mainView;
	private final Runnable onItemClick;
	private final RecyclerView mView;
	private final SettingsStore settings;
	private SuggestionsAdapter mSuggestionsAdapter;

	private final Handler alternativeScrollingHandler = new Handler();


	public SuggestionsBar(@NonNull SettingsStore settings, @NonNull ResizableMainView mainView, @NonNull Runnable onItemClick) {
		this.onItemClick = onItemClick;
		this.settings = settings;

		this.mainView = mainView;
		mView = mainView.getView() != null ? mainView.getView().findViewById(R.id.suggestions_bar) : null;
		if (mView != null) {
			Context context = mainView.getView().getContext();

			mView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
			mView.setOnTouchListener(this::onTouch);

			initDataAdapter(context);
			initSeparator(context);
			configureAnimation();


		}
	}


	private void configureAnimation() {
		DefaultItemAnimator animator = new DefaultItemAnimator();

		animator.setMoveDuration(SettingsStore.SUGGESTIONS_SELECT_ANIMATION_DURATION);
		animator.setChangeDuration(SettingsStore.SUGGESTIONS_TRANSLATE_ANIMATION_DURATION);
		animator.setAddDuration(SettingsStore.SUGGESTIONS_TRANSLATE_ANIMATION_DURATION);
		animator.setRemoveDuration(SettingsStore.SUGGESTIONS_TRANSLATE_ANIMATION_DURATION);

		mView.setItemAnimator(animator);
	}


	private void initDataAdapter(Context context) {
		mSuggestionsAdapter = new SuggestionsAdapter(
			context,
			this::handleItemClick,
			settings.isMainLayoutNumpad() ? R.layout.suggestion_list_numpad : R.layout.suggestion_list,
			R.id.suggestion_list_item,
			suggestions
		);
		mView.setAdapter(mSuggestionsAdapter);

		setDarkTheme(settings.getDarkTheme());
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


	public boolean isEmpty() {
		return suggestions.isEmpty();
	}


	public int getCurrentIndex() {
		return selectedIndex;
	}


	@NonNull
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

		setSuggestionsOnScreen();
	}


	private void setSuggestionsOnScreen() {
		if (mView != null) {
			mSuggestionsAdapter.setSelection(selectedIndex);
			mSuggestionsAdapter.notifyDataSetChanged();
			mView.scrollToPosition(selectedIndex);
		}
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

		scrollToSuggestionOnScreen(oldIndex);
	}


	private void scrollToSuggestionOnScreen(int oldIndex) {
		if (mView == null) {
			return;
		}

		mSuggestionsAdapter.setSelection(selectedIndex);
		mSuggestionsAdapter.notifyItemChanged(oldIndex);
		mSuggestionsAdapter.notifyItemChanged(selectedIndex);

		if (settings.getSuggestionScrollingDelay() > 0) {
			alternativeScrollingHandler.postDelayed(() -> mView.scrollToPosition(selectedIndex), settings.getSuggestionScrollingDelay());
		} else {
			mView.scrollToPosition(selectedIndex);
		}
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
	 * <a href="https://stackoverflow.com/questions/72382886/system-applies-night-mode-to-views-added-in-service-type-application-overlay">...</a>
	 */
	public void setDarkTheme(boolean darkEnabled) {
		if (mView == null) {
			return;
		}

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
		if (mView == null) {
			return;
		}

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
			(newSuggestionsSize == 0 && suggestions.isEmpty())
			|| (newSuggestionsSize > 0 && !suggestions.isEmpty())
		) {
			return;
		}

		setBackground(newSuggestions);
	}


	/**
	 * handleItemClick
	 * Passes through suggestion selected using the touchscreen.
	 */
	private void handleItemClick(int position) {
		selectedIndex = position;
		onItemClick.run();
	}


	private boolean onTouch(View view, MotionEvent event) {
		if (!isEmpty()) {
			return false;
		}

		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mainView.onResizeStart(event.getRawY());
				return true;
			case MotionEvent.ACTION_MOVE:
				mainView.onResizeThrottled(event.getRawY());
				return true;
			case MotionEvent.ACTION_UP:
				long now = System.currentTimeMillis();
				if (now - lastClickTime < SettingsStore.SOFT_KEY_DOUBLE_CLICK_DELAY) {
					mainView.onSnap();
				} else {
					mainView.onResize(event.getRawY());
				}

				lastClickTime = now;

				return true;
		}

		return false;
	}
}
