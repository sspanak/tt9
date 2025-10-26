package io.github.sspanak.tt9.ui.tray;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.Vibration;
import io.github.sspanak.tt9.ui.main.ResizableMainView;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.chars.Characters;

public class SuggestionsBar {
	public static final String SHOW_GROUP_0_SUGGESTION = "(…\u200A)";
	public static final String SHOW_GROUP_1_SUGGESTION = "(…\u200B)";

	private final String SHOW_MORE_SUGGESTION = "(...)";
	private final String STEM_SUFFIX = "… +";
	private final String STEM_VARIATION_PREFIX = "…";
	private final String STEM_PUNCTUATION_VARIATION_PREFIX = "​";
	@NonNull private String stem = "";

	private int defaultBackgroundColor = Color.TRANSPARENT;
	private int backgroundColor;
	private int suggestionSeparatorColor;

	private double lastClickTime = 0;
	private int lastScrollIndex = 0;
	private int selectedIndex = 0;
	@Nullable private List<String> suggestions = new ArrayList<>();
	@NonNull private final List<String> visibleSuggestions = new ArrayList<>();

	private final DefaultItemAnimator animator = new DefaultItemAnimator();
	private final ResizableMainView mainView;
	private final Runnable onItemClick;
	@Nullable private final RecyclerView mView;
	private final SettingsStore settings;
	private SuggestionsAdapter mSuggestionsAdapter;
	private Vibration vibration;

	private final Handler delayedDisplayHandler = new Handler();


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
			vibration = new Vibration(settings, mView);
		}
	}


	private void configureAnimation() {
		if (mView == null) {
			return;
		}

		animator.setMoveDuration(SettingsStore.SUGGESTIONS_SELECT_ANIMATION_DURATION);
		animator.setChangeDuration(SettingsStore.SUGGESTIONS_TRANSLATE_ANIMATION_DURATION);
		animator.setAddDuration(SettingsStore.SUGGESTIONS_TRANSLATE_ANIMATION_DURATION);
		animator.setRemoveDuration(SettingsStore.SUGGESTIONS_TRANSLATE_ANIMATION_DURATION);
	}


	private void initDataAdapter(Context context) {
		if (mView == null) {
			return;
		}

		mSuggestionsAdapter = new SuggestionsAdapter(
			context,
			this::handleItemClick,
			settings.isMainLayoutNumpad() ? R.layout.suggestion_list_numpad : R.layout.suggestion_list,
			R.id.suggestion_list_item,
			visibleSuggestions
		);

		mView.setAdapter(mSuggestionsAdapter);
		mView.setHasFixedSize(true); // Optimizes performance

		setColorScheme();
	}


	private void initSeparator(Context context) {
		if (mView == null) {
			return;
		}

		suggestionSeparatorColor = settings.getSuggestionSeparatorColor();
		// Extra XML is required instead of a ColorDrawable object, because setting the highlight color
		// erases the borders defined using the ColorDrawable.
		Drawable separatorDrawable = ContextCompat.getDrawable(context, R.drawable.suggestion_separator);
		if (separatorDrawable == null) {
			return;
		}

		separatorDrawable.setColorFilter(suggestionSeparatorColor, PorterDuff.Mode.SRC_ATOP);

		DividerItemDecoration separator = new DividerItemDecoration(mView.getContext(), RecyclerView.HORIZONTAL);
		separator.setDrawable(separatorDrawable);
		mView.addItemDecoration(separator);
	}


	public boolean isEmpty() {
		return visibleSuggestions.isEmpty();
	}


	public boolean containsStem() {
		return !stem.isEmpty();
	}


	public int getCurrentIndex() {
		return selectedIndex;
	}


	@NonNull
	public String get(int id) {
		String suggestion = getRaw(id);

		// show more...
		if (suggestion.equals(SHOW_MORE_SUGGESTION) || suggestion.equalsIgnoreCase(SHOW_GROUP_1_SUGGESTION) || suggestion.equalsIgnoreCase(SHOW_GROUP_0_SUGGESTION)) {
			return Characters.PLACEHOLDER;
		}

		// single char
		if (suggestion.equals(Characters.NEW_LINE)) return "\n";
		if (suggestion.equals(Characters.TAB)) return "\t";

		suggestion = suggestion.replace(Characters.ZWNJ_GRAPHIC, Characters.ZWNJ);
		suggestion = suggestion.replace(Characters.ZWJ_GRAPHIC, Characters.ZWJ);
		if (suggestion.length() == 1) return suggestion;


		// combined with "... +"
		int endIndex = suggestion.indexOf(STEM_SUFFIX);
		endIndex = endIndex == -1 ? suggestion.length() : endIndex;

		// "..." prefix
		int startIndex = 0;
		String[] prefixes = {STEM_VARIATION_PREFIX, STEM_PUNCTUATION_VARIATION_PREFIX, Characters.COMBINING_BASE};
		for (String prefix : prefixes) {
			int prefixIndex = suggestion.indexOf(prefix) + 1;
			if (prefixIndex < endIndex) { // do not match the prefix chars when they are part of STEM_SUFFIX
				startIndex = Math.max(startIndex, prefixIndex);
			}
    }

		if (startIndex == 0 && endIndex == suggestion.length()) {
			return suggestion;
		}

		return stem + suggestion.substring(startIndex, endIndex);
	}


	@NonNull
	public String getRaw(int id) {
		final int index = containsStem() ? id - 1 : id;
		if (index < 0 || suggestions == null || index >= suggestions.size()) {
			return "";
		}

		return suggestions.get(index);
	}


	public void setRTL(boolean yes) {
		if (mView != null) {
			mView.setLayoutDirection(yes ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
		}
	}


	public void setMany(@Nullable List<String> newSuggestions, int initialSel, boolean containsGenerated) {
		if ((suggestions == null || suggestions.isEmpty()) && (newSuggestions == null || newSuggestions.isEmpty())) {
			return;
		}

		suggestions = newSuggestions;
		selectedIndex = newSuggestions == null || newSuggestions.isEmpty() ? 0 : Math.max(initialSel, 0);

		visibleSuggestions.clear();
		setStem(newSuggestions, containsGenerated);

		boolean onlySpecialChars = newSuggestions != null && !newSuggestions.isEmpty() && !(new Text(newSuggestions.get(0)).isAlphabetic());
		addMany(newSuggestions, mView == null || onlySpecialChars ? Integer.MAX_VALUE : SettingsStore.SUGGESTIONS_MAX);

		selectedIndex = Math.max(Math.min(selectedIndex, visibleSuggestions.size() - 1), 0);

		render();
	}


	private void setStem(List<String> newSuggestions, boolean containsGenerated) {
		if (newSuggestions == null || newSuggestions.size() < 2) {
			stem = "";
			return;
		}

		stem = containsGenerated && newSuggestions.get(0).length() > 1 ? newSuggestions.get(0).substring(0, newSuggestions.get(0).length() - 1) : "";

		// Do not modify single letter + punctuation, such as "j'" or "l'". They look better as they are.
		stem = (stem.length() == 1 && newSuggestions.get(0).length() == 2 && !Character.isAlphabetic(newSuggestions.get(0).charAt(1))) ? "" : stem;

		// If no other suggestion contains the stem, it makes no sense to separate them and display:
		// "STEM" + "one-suggestion". It is only useful when there are multiple suggestions with the
		// same stem.
		boolean onlyOneContainsStem = true;
		for (int i = 1; i < newSuggestions.size(); i++) {
			if (newSuggestions.get(i).contains(stem)) {
				onlyOneContainsStem = false;
				break;
			}
		}
		stem = onlyOneContainsStem ? "" : stem;

		if (!stem.isEmpty() && !newSuggestions.contains(stem)) {
			visibleSuggestions.add(stem + STEM_SUFFIX);
			selectedIndex++;
		}
	}


	/**
	 * Adds suggestions to the list displayed on the screen. By default, they should be limited
	 * for performance reasons, hence the "limit" parameter. When they are too many, the SHOW_MORE_SUGGESTION,
	 * will be displayed at the end.
	 */
	private void addMany(List<String> newSuggestions, int limit) {
		if (newSuggestions == null) {
			return;
		}

		for (int i = 0, end = Math.min(limit, newSuggestions.size()); i < end; i++) {
			add(newSuggestions.get(i));
		}

		if (newSuggestions.size() > limit) {
			visibleSuggestions.add(SHOW_MORE_SUGGESTION);
		}
	}


	private void add(@NonNull String suggestion) {
		// shorten the stem variations
		if (!stem.isEmpty() && suggestion.length() == stem.length() + 1 && suggestion.toLowerCase().startsWith(stem.toLowerCase())) {
			String trimmedSuggestion = suggestion.substring(stem.length());
			char firstChar = trimmedSuggestion.charAt(0);

			String prefix = Character.isAlphabetic(firstChar) && !Characters.isCombiningPunctuation(firstChar) ? STEM_VARIATION_PREFIX : STEM_PUNCTUATION_VARIATION_PREFIX;
			prefix = Characters.isFathatan(firstChar) ? " " : prefix; // Fix incorrect display of Fathatan without a base character. It is a combining character, but since it is a letter, we must include a base character not to break it, with a "..." prefix
			visibleSuggestions.add(prefix + formatUnreadableSuggestion(trimmedSuggestion));
			return;
		}

		visibleSuggestions.add(formatUnreadableSuggestion(suggestion));
	}


	private void render() {
		if (mView == null) {
			return;
		}

		setBackground(false);
		mSuggestionsAdapter.setTextSize(settings.getSuggestionFontScale());

		boolean smooth = settings.getSuggestionSmoothScroll() && visibleSuggestions.size() <= SettingsStore.SUGGESTIONS_MAX + 1;
		mView.setItemAnimator(smooth ? animator : null);

		mSuggestionsAdapter.resetItems(selectedIndex);
		if (selectedIndex > 0) {
			mView.scrollToPosition(selectedIndex);
		}
	}


	/**
	 * If addMany() constrained the visible suggestions, the end of the list will contain
	 * the SHOW_MORE_SUGGESTION. This method will remove the SHOW_MORE_SUGGESTION, prepare
	 * all hidden suggestions for displaying, and will scroll correctly to the new visible suggestion.
	 * After that, you must call render(), to visualize the changes.
	 */
	private boolean appendHiddenSuggestionsIfNeeded(boolean scrollBack) {
		if (mView == null || selectedIndex < 0 || selectedIndex >= visibleSuggestions.size() || !visibleSuggestions.get(selectedIndex).equals(SHOW_MORE_SUGGESTION)) {
			return false;
		}

		visibleSuggestions.clear();
		addMany(suggestions, Integer.MAX_VALUE);
		selectedIndex = scrollBack || selectedIndex >= visibleSuggestions.size() ? visibleSuggestions.size() - 1 : selectedIndex;
		selectedIndex = Math.max(selectedIndex, 0);

		return true;
	}


	private String formatUnreadableSuggestion(String suggestion) {
		if (TextTools.isCombining(suggestion)) {
			return Characters.COMBINING_BASE + suggestion;
		}

		return switch (suggestion) {
			case "\n" -> Characters.NEW_LINE;
			case "\t" -> Characters.TAB;
			case Characters.ZWJ -> Characters.ZWJ_GRAPHIC;
			case Characters.ZWNJ -> Characters.ZWNJ_GRAPHIC;
			default -> suggestion;
		};
	}


	public void scrollToSuggestion(int increment) {
		if (visibleSuggestions.size() <= 1) {
			return;
		}

		calculateScrollIndex(increment);
		if (appendHiddenSuggestionsIfNeeded(increment < 0)) {
			render();
		}
		scrollToSelected();
	}


	private void calculateScrollIndex(int increment) {
		if (visibleSuggestions.isEmpty()) {
			selectedIndex = 0;
			return;
		}

		selectedIndex = selectedIndex + increment;
		if (selectedIndex == visibleSuggestions.size()) {
			selectedIndex = containsStem() ? 1 : 0;
		} else if (selectedIndex < 0) {
			selectedIndex = visibleSuggestions.size() - 1;
		} else if (selectedIndex == 0 && containsStem()) {
			selectedIndex = visibleSuggestions.size() - 1;
		}
	}


	private void scrollToSelected() {
		if (mView == null) {
			return;
		}

		mSuggestionsAdapter.setSelection(selectedIndex);

		if (settings.getSuggestionScrollingDelay() > 0) {
			delayedDisplayHandler.removeCallbacksAndMessages(null);
			delayedDisplayHandler.postDelayed(this::renderScroll, settings.getSuggestionScrollingDelay());
		} else {
			renderScroll();
		}
	}


	/**
	 * Tells the adapter to scroll. Always call scrollToSelected() first,
	 * to set the selected index in the adapter.
	 */
	private void renderScroll() {
		if (mView == null) {
			return;
		}

		boolean smooth = settings.getSuggestionSmoothScroll() && Math.abs(selectedIndex - lastScrollIndex) < SettingsStore.SUGGESTIONS_MAX;
		mView.setItemAnimator(smooth ? animator : null);
		mView.scrollToPosition(containsStem() && selectedIndex == 1 ? 0 : selectedIndex);
		lastScrollIndex = selectedIndex;
	}


	/**
	 * setColorScheme
	 * Changes the suggestion colors according to the current color scheme.
	 */
	public void setColorScheme() {
		if (mView == null) {
			return;
		}

		defaultBackgroundColor = settings.getKeyboardBackground();
		mSuggestionsAdapter.setColorDefault(settings.getKeyboardTextColor());
		mSuggestionsAdapter.setColorHighlight(settings.getSuggestionSelectedColor());
		mSuggestionsAdapter.setBackgroundHighlight(settings.getSuggestionSelectedBackground());
		suggestionSeparatorColor = settings.getSuggestionSeparatorColor();

		setBackground(true);
	}


	/**
	 * setBackground
	 * Makes the background transparent, when there are no suggestions and theme-colored,
	 * when there are suggestions.
	 */
	private void setBackground(boolean force) {
		if (mView == null) {
			return;
		}

		boolean mustChange = (
			(backgroundColor == Color.TRANSPARENT && !visibleSuggestions.isEmpty()) ||
			(backgroundColor == defaultBackgroundColor && visibleSuggestions.isEmpty())
		);

		if (force || mustChange) {
			backgroundColor = visibleSuggestions.isEmpty() ? Color.TRANSPARENT : defaultBackgroundColor;
			mView.setBackgroundColor(backgroundColor);
		}
	}


	/**
	 * handleItemClick
	 * Passes through suggestion selected using the touchscreen.
	 */
	private void handleItemClick(int position) {
		vibration.vibrate();
		selectedIndex = position;
		if (appendHiddenSuggestionsIfNeeded(false)) {
			render();
		} else {
			onItemClick.run();
		}
	}


	private boolean onTouch(View v, MotionEvent event) {
		if (!isEmpty()) {
			return false;
		}

		int action = event.getAction();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mainView.onResizeStart(event.getRawY());
				return true;
			case MotionEvent.ACTION_MOVE:
				if (settings.getDragResize()) {
					mainView.onResizeThrottled(event.getRawY());
				}
				return true;
			case MotionEvent.ACTION_UP:
				long now = System.currentTimeMillis();
				if (now - lastClickTime < SettingsStore.SOFT_KEY_DOUBLE_CLICK_DELAY) {
					mainView.onSnap();
				} else if (settings.getDragResize()){
					mainView.onResize(event.getRawY());
				}

				lastClickTime = now;

				return true;
		}

		return false;
	}
}
