package io.github.sspanak.tt9.ime;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;
import io.github.sspanak.tt9.util.ConsumerCompat;

public class SuggestionOps {
	@NonNull private final Handler delayedAcceptHandler;
	@NonNull private final ConsumerCompat<String> onDelayedAccept;
	@NonNull protected final SuggestionsBar suggestionBar;
	@NonNull private TextField textField;


	SuggestionOps(@NonNull SettingsStore settings, View mainView, @NonNull ConsumerCompat<String> onDelayedAccept, @NonNull Runnable onSuggestionClick) {
		delayedAcceptHandler = new Handler(Looper.getMainLooper());
		this.onDelayedAccept = onDelayedAccept;

		suggestionBar = new SuggestionsBar(settings, mainView, onSuggestionClick);
		textField = new TextField(null, null);
	}


	void setTextField(@NonNull TextField textField) {
		this.textField = textField;
	}


	boolean isEmpty() {
		return suggestionBar.isEmpty();
	}


	String get(int index) {
		return suggestionBar.getSuggestion(index);
	}


	void clear() {
		set(null);
		textField.setComposingText("");
		textField.finishComposingText();
	}


	void set(ArrayList<String> suggestions) {
		suggestionBar.setSuggestions(suggestions, 0);
	}


	void set(ArrayList<String> suggestions, int selectIndex) {
		suggestionBar.setSuggestions(suggestions, selectIndex);
	}


	void scrollTo(int index) {
		suggestionBar.scrollToSuggestion(index);
	}


	String acceptCurrent() {
		String word = getCurrent();
		if (!word.isEmpty()) {
			commitCurrent(true);
		}

		return word;
	}


	String acceptIncomplete() {
		String currentWord = this.getCurrent();
		commitCurrent(false);

		return currentWord;
	}


	String acceptPrevious(int sequenceLength) {
		String lastComposingText = getCurrent(sequenceLength - 1);
		commitCurrent(false);
		return lastComposingText;
	}


	void commitCurrent(boolean entireSuggestion) {
		if (!suggestionBar.isEmpty()) {
			if (entireSuggestion) {
				textField.setComposingText(getCurrent());
			}
			textField.finishComposingText();
		}

		set(null);
	}


	int getCurrentIndex() {
		return suggestionBar.getCurrentIndex();
	}


	String getCurrent() {
		return get(suggestionBar.getCurrentIndex());
	}


	protected String getCurrent(int maxLength) {
		if (maxLength == 0 || suggestionBar.isEmpty()) {
			return "";
		}

		String text = getCurrent();
		if (maxLength > 0 && !text.isEmpty() && text.length() > maxLength) {
			text = text.substring(0, maxLength);
		}

		return text;
	}


	boolean scheduleDelayedAccept(int delay) {
		cancelDelayedAccept();

		if (suggestionBar.isEmpty()) {
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


	void cancelDelayedAccept() {
		delayedAcceptHandler.removeCallbacksAndMessages(null);
	}


	void setDarkTheme(boolean yes) {
		suggestionBar.setDarkTheme(yes);
	}
}
