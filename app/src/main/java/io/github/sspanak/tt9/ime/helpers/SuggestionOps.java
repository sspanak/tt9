package io.github.sspanak.tt9.ime.helpers;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;

public class SuggestionOps {
	@NonNull private final Handler delayedAcceptHandler;
	@NonNull private final ConsumerCompat<String> onDelayedAccept;

	@Nullable protected SuggestionsBar suggestionBar;
	@NonNull private TextField textField;


	public SuggestionOps(@Nullable InputMethodService ims, @Nullable SettingsStore settings, @Nullable ResizableMainView mainView, @Nullable TextField textField, @Nullable ConsumerCompat<String> onDelayedAccept, @Nullable Runnable onSuggestionClick) {
		delayedAcceptHandler = new Handler(Looper.getMainLooper());
		this.onDelayedAccept = onDelayedAccept != null ? onDelayedAccept : s -> {};

		this.textField = textField != null ? textField : new TextField(ims, null, null);

		if (settings != null && mainView != null && onSuggestionClick != null) {
			suggestionBar = new SuggestionsBar(settings, mainView, onSuggestionClick);
		}
	}

	public void setLanguage(@Nullable Language newLanguage) {
		if (suggestionBar != null) {
			suggestionBar.setRTL(LanguageKind.isRTL(newLanguage));
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
		return suggestionBar != null ? suggestionBar.get(index) : "";
	}


	public void clear() {
		set(null);
		textField.setComposingText("");
		textField.finishComposingText();
	}

	public void set(ArrayList<String> suggestions) {
		if (suggestionBar != null) {
			suggestionBar.setMany(suggestions, 0, false);
		}
	}

	public void set(ArrayList<String> suggestions, boolean containsGenerated) {
		if (suggestionBar != null) {
			suggestionBar.setMany(suggestions, 0, containsGenerated);
		}
	}


	public void set(ArrayList<String> suggestions, int selectIndex, boolean containsGenerated) {
		if (suggestionBar != null) {
			suggestionBar.setMany(suggestions, selectIndex, containsGenerated);
		}
	}


	public void scrollTo(int index) {
		if (suggestionBar != null) {
			suggestionBar.scrollToSuggestion(index);
		}
	}


	public String acceptCurrent() {
		String word = getCurrent();
		if (Characters.PLACEHOLDER.equals(word)) {
			return "";
		}

		if (!word.isEmpty()) {
			commitCurrent(true, true);
		}

		return word;
	}


	public String acceptIncomplete() {
		String currentWord = this.getCurrent();
		if (Characters.PLACEHOLDER.equals(currentWord)) {
			return "";
		}

		commitCurrent(false, true);

		return currentWord;
	}


	public String acceptIncompleteAndKeepList() {
		if (Characters.PLACEHOLDER.equals(this.getCurrent())) {
			return "";
		}

		commitCurrent(false, false);
		return this.getCurrent();
	}


	public String acceptPrevious(Language language, int sequenceLength) {
		if (sequenceLength <= 0) {
			set(null);
		}

		String lastComposingText = getCurrent(language, sequenceLength - 1);
		if (Characters.PLACEHOLDER.equals(lastComposingText)) {
			return "";
		}
		commitCurrent(false, true);
		return lastComposingText;
	}


	public void commitCurrent(boolean entireSuggestion, boolean clearList) {
		if (!isEmpty()) {
			if (entireSuggestion) {
				textField.setComposingText(getCurrent());
			}
			textField.finishComposingText();
		}


		if (clearList) {
			set(null);
		}
	}


	public int getCurrentIndex() {
		return suggestionBar != null ? suggestionBar.getCurrentIndex() : 0;
	}


	public String getCurrent() {
		return get(getCurrentIndex());
	}


	public String getCurrentRaw() {
		return suggestionBar != null ? suggestionBar.getRaw(getCurrentIndex()) : "";
	}


	public String getCurrent(Language language, int maxLength) {
		if (maxLength == 0 || isEmpty()) {
			return "";
		}

		Text text = new Text(language, getCurrent());
		if (maxLength > 0 && !text.isEmpty() && text.codePointLength() > maxLength) {
			return text.substringCodePoints(0, maxLength);
		}

		return text.toString();
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
