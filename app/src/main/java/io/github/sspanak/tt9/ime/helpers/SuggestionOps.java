package io.github.sspanak.tt9.ime.helpers;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;
import io.github.sspanak.tt9.util.ConsumerCompat;

public class SuggestionOps {
	@NonNull private final Handler delayedAcceptHandler;
	@NonNull private final ConsumerCompat<String> onDelayedAccept;
	@Nullable protected SuggestionsBar suggestionBar;
	@NonNull private TextField textField;


	public SuggestionOps(@Nullable SettingsStore settings, @Nullable ResizableMainView mainView, @Nullable TextField textField, @Nullable ConsumerCompat<String> onDelayedAccept, @Nullable Runnable onSuggestionClick) {
		delayedAcceptHandler = new Handler(Looper.getMainLooper());
		this.onDelayedAccept = onDelayedAccept != null ? onDelayedAccept : s -> {};

		this.textField = textField != null ? textField : new TextField(null, null, null);
		if (settings != null && mainView != null && onSuggestionClick != null) {
			suggestionBar = new SuggestionsBar(settings, mainView, onSuggestionClick);
		}
	}


	public void setRTL(boolean yes) {
		if (suggestionBar != null) {
			suggestionBar.setRTL(yes);
		}
	}


	public void setTextField(@NonNull TextField textField) {
		this.textField = textField;
	}


	public boolean isEmpty() {
		return suggestionBar == null || suggestionBar.isEmpty();
	}


	public boolean containsStem() {
		return suggestionBar != null && suggestionBar.containsStem();
	}


	@NonNull
	public String get(int index) {
		return suggestionBar != null ? suggestionBar.getSuggestion(index) : "";
	}


	public void clear() {
		set(null);
		textField.setComposingText("");
		textField.finishComposingText();
	}

	public void set(ArrayList<String> suggestions) {
		if (suggestionBar != null) {
			suggestionBar.setSuggestions(suggestions, 0, false);
		}
	}

	public void set(ArrayList<String> suggestions, boolean containsGenerated) {
		if (suggestionBar != null) {
			suggestionBar.setSuggestions(suggestions, 0, containsGenerated);
		}
	}


	public void set(ArrayList<String> suggestions, int selectIndex, boolean containsGenerated) {
		if (suggestionBar != null) {
			suggestionBar.setSuggestions(suggestions, selectIndex, containsGenerated);
		}
	}


	public void scrollTo(int index) {
		if (suggestionBar != null) {
			suggestionBar.scrollToSuggestion(index);
		}
	}


	public String acceptCurrent() {
		String word = getCurrent();
		if (!word.isEmpty()) {
			commitCurrent(true);
		}

		return word;
	}


	public String acceptIncomplete() {
		String currentWord = this.getCurrent();
		commitCurrent(false);

		return currentWord;
	}


	public String acceptPrevious(int sequenceLength) {
		if (sequenceLength <= 0) {
			set(null);
		}

		String lastComposingText = getCurrent(sequenceLength - 1);
		commitCurrent(false);
		return lastComposingText;
	}


	public void commitCurrent(boolean entireSuggestion) {
		if (!isEmpty()) {
			if (entireSuggestion) {
				textField.setComposingText(getCurrent());
			}
			textField.finishComposingText();
		}

		set(null);
	}


	public int getCurrentIndex() {
		return suggestionBar != null ? suggestionBar.getCurrentIndex() : 0;
	}


	public String getCurrent() {
		return get(getCurrentIndex());
	}


	public String getCurrent(int maxLength) {
		if (maxLength == 0 || isEmpty()) {
			return "";
		}

		String text = getCurrent();
		if (maxLength > 0 && !text.isEmpty() && text.length() > maxLength) {
			text = text.substring(0, maxLength);
		}

		return text;
	}


	public boolean scheduleDelayedAccept(int delay) {
		cancelDelayedAccept();

		if (isEmpty()) {
			return false;
		}

		if (delay == 0) {
			onDelayedAccept.accept(acceptCurrent());
			return true;
		} else if (delay > 0) {
			delayedAcceptHandler.postDelayed(() -> onDelayedAccept.accept(acceptCurrent()), delay);
		}

		return false;
	}


	public void cancelDelayedAccept() {
		delayedAcceptHandler.removeCallbacksAndMessages(null);
	}


	public void setDarkTheme() {
		if (suggestionBar != null) {
			suggestionBar.setDarkTheme();
		}
	}
}
