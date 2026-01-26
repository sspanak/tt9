package io.github.sspanak.tt9.ime.helpers;

import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;

import io.github.sspanak.tt9.hacks.AppHacks;
import io.github.sspanak.tt9.hacks.InputType;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.ResizableMainView;
import io.github.sspanak.tt9.ui.tray.StatusBar;
import io.github.sspanak.tt9.ui.tray.SuggestionsBar;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.chars.Characters;
import io.github.sspanak.tt9.util.sys.Clipboard;

public class SuggestionOps {
	@NonNull private final Handler delayedAcceptHandler;
	@NonNull private final Consumer<String> onDelayedAccept;

	@Nullable protected SuggestionsBar suggestionBar;
	@Nullable private AppHacks appHacks;
	private boolean isInputLimited;
	@NonNull private TextField textField;
	@Nullable private final SettingsStore settings;
	@Nullable private StatusBar statusBar;


	public SuggestionOps(@Nullable InputMethodService ims, @Nullable SettingsStore settings, @Nullable ResizableMainView mainView, @Nullable AppHacks appHacks, @Nullable InputType inputType, @Nullable TextField textField, @Nullable StatusBar statusBar, @Nullable Consumer<String> onDelayedAccept, @Nullable Runnable onSuggestionClick) {
		delayedAcceptHandler = new Handler(Looper.getMainLooper());
		this.onDelayedAccept = onDelayedAccept != null ? onDelayedAccept : s -> {};

		this.appHacks = appHacks;
		this.isInputLimited = inputType == null || inputType.isLimited();
		this.settings = settings;
		this.statusBar = statusBar;
		this.textField = textField != null ? textField : new TextField(ims, settings, null);

		if (settings != null && mainView != null && onSuggestionClick != null) {
			suggestionBar = new SuggestionsBar(settings, mainView, onSuggestionClick);
		}
	}


	public void setLanguage(@Nullable Language newLanguage) {
		if (suggestionBar != null) {
			suggestionBar.setRTL(LanguageKind.isRTL(newLanguage));
		}
	}


	public void setDependencies(@NonNull AppHacks appHacks, @NonNull InputType inputType, @NonNull TextField textField, @NonNull StatusBar statusBar) {
		this.appHacks = appHacks;
		this.isInputLimited = inputType.isLimited();
		this.textField = textField;
		this.statusBar = statusBar;
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
		if (appHacks == null) {
			textField.setComposingText("");
		} else {
			appHacks.setComposingText("");
		}
		textField.finishComposingText();
	}


	public void set(ArrayList<String> suggestions) {
		set(suggestions, 0, false);
	}


	public void set(ArrayList<String> suggestions, boolean containsGenerated) {
		set(suggestions, 0, containsGenerated);
	}


	public void set(ArrayList<String> suggestions, int selectIndex, boolean containsGenerated) {
		setVisibility(settings, suggestions, false);
		if (suggestionBar != null) {
			suggestionBar.setMany(suggestions, selectIndex, containsGenerated);
		}
	}


	public void setClipboardItems(LinkedList<CharSequence> clips) {
		ArrayList<String> clipStrings = new ArrayList<>(clips.size());
		for (int i = clips.size() - 1; i >= 0; i--) {
			String preview = Clipboard.getPreview(i, SuggestionsBar.CLIPBOARD_SUGGESTION_SUFFIX);
			if (preview != null) {
				clipStrings.add(preview);
			}
		}

		setVisibility(settings, clipStrings, true);
		if (suggestionBar != null) {
			suggestionBar.setMany(clipStrings, 0, false);
		}
	}


	public void scrollTo(int index) {
		if (suggestionBar != null) {
			suggestionBar.scrollToSuggestion(index);
		}
	}


	public String acceptCurrent() {
		final String current = getCurrent();
		if (Characters.PLACEHOLDER.equals(current)) {
			return "";
		}

		if (!current.isEmpty()) {
			commitCurrent(true, true);
		}

		return current;
	}


	public String acceptEdited() {
		final String current = getCurrent();
		if (current.isEmpty() || Characters.PLACEHOLDER.equals(current)) {
			return "";
		}

		String composingText = textField.getComposingText();
		if (composingText.length() > current.length() && !composingText.endsWith(current)) {
			composingText = new StringBuilder(composingText).replace(composingText.length() - current.length(), composingText.length(), current).toString();

			if (appHacks == null) {
				textField.setComposingText(composingText);
			} else {
				appHacks.setComposingText(composingText);
			}
		}

		textField.finishComposingText();

		return current;
	}


	public String acceptIncomplete() {
		final String current = getCurrent();
		if (Characters.PLACEHOLDER.equals(current)) {
			return "";
		}

		commitCurrent(false, true);

		return current;
	}


	public String acceptIncompleteAndKeepList() {
		if (Characters.PLACEHOLDER.equals(this.getCurrent())) {
			return "";
		}

		commitCurrent(false, false);
		return this.getCurrent();
	}


	public void commitCurrent(boolean entireSuggestion, boolean clearList) {
		if (!isEmpty()) {
			if (entireSuggestion) {
				if (appHacks == null) {
					textField.setComposingText(getCurrent());
				} else {
					appHacks.setComposingText(getCurrent());
				}
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


	public void setColorScheme() {
		if (suggestionBar != null) {
			suggestionBar.setColorScheme();
		}
	}


	private void setVisibility(@Nullable SettingsStore settings, @Nullable ArrayList<String> newSuggestions, boolean clipboardSuggestions) {
		final boolean areSuggestionsVisible = isInputLimited || clipboardSuggestions || (settings != null && settings.getShowSuggestions());

		if (suggestionBar != null) {
			suggestionBar.setVisible(areSuggestionsVisible);
		}

		if (statusBar != null) {
			statusBar.setShown(newSuggestions == null || newSuggestions.isEmpty() || !areSuggestionsVisible);
		}
	}
}
