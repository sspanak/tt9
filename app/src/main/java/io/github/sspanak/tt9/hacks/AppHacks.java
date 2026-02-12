package io.github.sspanak.tt9.hacks;

import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.commands.CmdMoveCursor;
import io.github.sspanak.tt9.ime.helpers.CursorOps;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.ime.helpers.SuggestionOps;
import io.github.sspanak.tt9.ime.helpers.TextField;
import io.github.sspanak.tt9.ime.helpers.TextSelection;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.HighlightedText;
import io.github.sspanak.tt9.util.Text;
import io.github.sspanak.tt9.util.Timer;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class AppHacks {
	private final static String TYPING_SESSION_TIMER = "TYPING_SESSION_TIMER";
	private boolean previousWasMessengerChat = false;


	private final static String COMPOSING_TEXT_TO_RESTART_TIMER = "COMPOSING_RESTART_TIMER";
	private long composingTextToRestartTime = Integer.MAX_VALUE;

	@Nullable private InputType inputType;
	@Nullable private TextField textField;
	@Nullable private TextSelection textSelection;


	public void setDependencies(@Nullable InputType inputType, @Nullable TextField textField, @Nullable TextSelection textSelection) {
		this.inputType = inputType;
		this.textField = textField;
		this.textSelection = textSelection;
	}


	/**
	 * Perform hacks to prepare the state for our onStart().
	 */
	public void onBeforeStart(@NonNull InputMethodService ims, @NonNull SettingsStore settings, @Nullable Language language, @Nullable EditorInfo field, @NonNull InputMode inputMode, @NonNull SuggestionOps suggestionOps, boolean restarting) {
		acceptComposingTextOnCursorReset(inputMode, suggestionOps, new TextField(ims, settings, field));
		mitigateRestartOnKeypressComposingTextCorruption(ims, settings, language, field, restarting);
		resetMessengerPadding(ims, settings, field);
	}


	/**
	 * Allows absolutely all brutal methods to show the keyboard, ignoring the framework flags, and
	 * incorrect app behavior (e.g. not requesting the keyboard when focusing a text field).
	 */
	public boolean isBrutalForceShowNeeded() {
		return
			DeviceInfo.AT_LEAST_ANDROID_16
			&& inputType != null
			&& (inputType.isFirefoxText() || inputType.isGmailComposeMail());
	}


	/**
	 * setComposingText
	 * Performs extra operations when setting composing text for apps that do not do it properly themselves.
	 */
	public void setComposingText(@NonNull String word) {
		if (inputType == null || textField == null) {
			return;
		}

		if (inputType.isWhatsApp() && Text.isGraphic(word)) {
			textField.setComposingText("");
		}

		textField.setComposingText(word);
	}



	public void setComposingTextWithHighlightedStem(@NonNull String word, @Nullable String stem, boolean isStemFilterFuzzy) {
		final HighlightedText highText =
			new HighlightedText(word, true, false)
			.setRegion(0, stem != null ? stem.length() : 0, true, isStemFilterFuzzy, false);

		setComposingText(highText);
	}


	public void setComposingTextPartsWithHighlightedJoining(@NonNull String word, @NonNull String suffix) {
		final HighlightedText highText = new HighlightedText(word + suffix, false, false)
			.setRegion(word.length() - 1, word.length(), true, false, true);

		setComposingText(highText);
	}


	/**
	 * A compatibility function for text fields that do not properly support composing text.
	 */
	private void setComposingText(@NonNull HighlightedText word) {
		if (inputType == null || textField == null) {
			return;
		}

		// use composing text but do not highlight it with SpannableString
		if (inputType.isKindleInvertedTextField()) {
			textField.setComposingText(word.toString());
			return;
		}

		// if the composing text starts with an emoji, reset to empty before settings new composing text
		if (inputType.isWhatsApp() && Text.isGraphic(word.toString())) {
			textField.setComposingText("");
		}

		// disable composing text for stupid search fields in eBay or Deezer, which restart the connection
		// on every key press, causing duplication of the composing text.
		if (isComposingCausingRestarts()) {
			textField.disableComposing();
		}
		Timer.start(COMPOSING_TEXT_TO_RESTART_TIMER);

		// set the composing text in the app
		textField.setComposingText(word.highlight());
	}


	/**
	 * onBackspace
	 * Performs extra Backspace operations and returns "false", or completely replaces Backspace and returns "true". When "true" is
	 * returned, you must not attempt to delete text. This function has already done everything necessary.
	 */
	public boolean onBackspace(@NonNull SettingsStore settings, @NonNull InputMode inputMode) {
		if (inputType == null || textField == null || textSelection == null) {
			return false;
		}

		if (inputType.isKindleInvertedTextField()) {
			inputMode.clearWordStem();
		} else if (inputType.isTermux()) {
			return false;
		}

		// When Backspace function is assigned to a different key (not hardware Backspace), we must
		// allow the key to function normally if there is nothing to delete (e.g. "Back" navigates back).
		return
			Key.exists(settings.getKeyBackspace())
			&& !Key.isHardwareBackspace(settings.getKeyBackspace())
			&& inputMode.noSuggestions()
			&& textSelection.isEmpty()
			&& textField.getTextBeforeCursor(null, 1).isEmpty();
	}


	/**
	 * onAction
	 * Runs non-standard actions for certain apps and fields. Use instead of inputConnection.performEditorAction(action).
	 * Returns "true" if the action was handled, "false" otherwise.
	 */
	public boolean onAction(int action) {
		if (inputType != null && textField != null && inputType.isSonimSearchField(action)) {
			return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
		}

		return false;
	}


	/**
	 * Handles applications that always report no text around the cursor, preventing the cursor from
	 * moving the usual way.
	 */
	public boolean onMoveCursor(int direction) {
		if (inputType == null || textField == null) {
			return false;
		}

		final int keyCode = switch (direction) {
			case CmdMoveCursor.CURSOR_MOVE_UP -> KeyEvent.KEYCODE_DPAD_UP;
			case CmdMoveCursor.CURSOR_MOVE_DOWN -> KeyEvent.KEYCODE_DPAD_DOWN;
			case CmdMoveCursor.CURSOR_MOVE_LEFT -> KeyEvent.KEYCODE_DPAD_LEFT;
			case CmdMoveCursor.CURSOR_MOVE_RIGHT -> KeyEvent.KEYCODE_DPAD_RIGHT;
			default -> KeyEvent.KEYCODE_UNKNOWN;
		};

		if (inputType.isRustDesk() || inputType.isTermux()) {
			return textField.sendDownUpKeyEvents(keyCode);
		}

		return false;
	}


	/**
	 * Performs extra operations when the cursor moves and returns "true" if the selection was handled,
	 * "false" otherwise.
	 */
	public boolean onUpdateSelection(
		@NonNull InputMode inputMode,
		@NonNull SuggestionOps suggestionOps,
		int oldSelStart,
		int oldSelEnd,
		int newSelStart,
		int newSelEnd,
		int candidatesStart,
		int candidatesEnd
	) {
		return
			CursorOps.isInputReset(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
			&& acceptComposingTextOnCursorReset(inputMode, suggestionOps, textField);
	}


	/**
	 * onEnter
	 * Tries to guess and send the correct confirmation key code or sequence of key codes,
	 * depending on the connected application and input field. On invalid connection or field,
	 * it does nothing and return "false", signaling the system we have ignored the key press.
	 */
	public boolean onEnter() {
		if (inputType == null || textField == null) {
			return false;
		}

		if (inputType.isTermux() || inputType.isMultilineTextInNonSystemApp()) {
			// Termux supports only ENTER, so we convert DPAD_CENTER for it.
			// Any extra installed apps are likely not designed for hardware keypads, so again,
			// we don't want to send DPAD_CENTER to them.
			return textField.sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
		}

		// The rest of the cases are probably system apps or numeric fields, which should
		// now how to handle the incoming OK key code, be it ENTER or DPAD_CENTER.
		// As per the docs, we must return "false", to indicate that we have not "seen" the key press.
		return false;
	}


	private boolean isComposingCausingRestarts() {
		return composingTextToRestartTime <= SettingsStore.COMPOSING_TEXT_RESTART_THRESHOLD;
	}

	/**
	 * When sending messages in Signal, Viber or Google SMS, using their own send button, these apps
	 * clear the text field, but without notifying the keyboard. This results in the InputMode still
	 * holding the old composing text, after the text field has been cleared. Attempting to type a new
	 * word then causes the previous word to pop back up. We use this hack to detect such situations
	 * and reset the InputMode upon sending a message.
	 */
	private boolean acceptComposingTextOnCursorReset(@NonNull InputMode inputMode, @NonNull SuggestionOps suggestionOps, @Nullable TextField textField) {
		if (!isComposingCausingRestarts() && textField != null && textField.isEmpty() && !(inputMode.getSuggestions().isEmpty() && suggestionOps.isEmpty())) {
			inputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
			inputMode.reset();
			return true;
		}

		return false;
	}


	/**
	 * For Deezer, eBay and other apps' search fields, where the input connection gets restarted on
	 * every key press. The restart causes initialization of a new TextField object, losing the previous
	 * composing text for us, but remaining in the app's field. When we attempt to set new text, on
	 * the next key press, the text will get duplicated. For this reason, when we detect such behavior,
	 * we disable composing, and delete the first loose character.
	 */
	private void mitigateRestartOnKeypressComposingTextCorruption(@NonNull InputMethodService ims, @NonNull SettingsStore settings, @Nullable Language language, EditorInfo field, boolean restarting) {
		if (!settings.getAutoDisableComposing()) {
			return;
		}

		if (!restarting) {
			composingTextToRestartTime = Integer.MAX_VALUE;
			return;
		}

		final long time = Timer.stop(COMPOSING_TEXT_TO_RESTART_TIMER);
		if (time > 0 && !isComposingCausingRestarts() && time <= SettingsStore.COMPOSING_TEXT_RESTART_THRESHOLD) {
			composingTextToRestartTime = time;

			// delete the corrupted character that went out of control
			// the surrounding if ensures we do it once
			TextField corruptedField = textField == null ? new TextField(ims, settings, field) : textField;
			corruptedField.deleteChars(language, 1);
		}
	}


	private void resetMessengerPadding(@NonNull InputMethodService ims, @NonNull SettingsStore settings, @Nullable EditorInfo field) {
		// below we adjust the padding of MainSmall, so save some resources by not doing anything if
		// another layout is used.
		if (!settings.isMainLayoutSmall()) {
			settings.setMessengerReplyExtraPadding(false);
			return;
		}

		final InputType newInputType = new InputType(ims, field);
		if (newInputType.notMessenger()) {
			settings.setMessengerReplyExtraPadding(false);
			return;
		}

		final long previousSessionTime = Timer.stop(TYPING_SESSION_TIMER);
		final boolean currentIsMessengerNonText = newInputType.isMessengerNonText();

		if (previousSessionTime < 1000 && previousWasMessengerChat && currentIsMessengerNonText) {
			settings.setMessengerReplyExtraPadding(true);
		} else if (previousSessionTime > 1000 && previousWasMessengerChat) {
			settings.setMessengerReplyExtraPadding(false);
		}

		Timer.start(TYPING_SESSION_TIMER);
		previousWasMessengerChat = newInputType.isMessengerChat();
	}
}
