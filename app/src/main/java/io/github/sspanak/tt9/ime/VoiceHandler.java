package io.github.sspanak.tt9.ime;

import android.Manifest;
import android.view.KeyEvent;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.voice.VoiceInputError;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.ui.dialogs.RequestPermissionDialog;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.Ternary;
import io.github.sspanak.tt9.util.TextTools;

abstract class VoiceHandler extends TypingHandler {
	private final static String LOG_TAG = VoiceHandler.class.getSimpleName();
	protected VoiceInputOps voiceInputOps;
	private boolean shouldCapitalizeVoiceInput = false;


	@Override
	protected void onInit() {
		super.onInit();

		voiceInputOps = new VoiceInputOps(
			this,
			this::onVoiceInputStarted,
			this::onVoiceInputStopped,
			this::onVoiceInputPartial,
			this::onVoiceInputError
		);
	}

	@Override
	protected Ternary onBack() {
		stopVoiceInput();
		return Ternary.FALSE; // we don't want to abort other operations, we just silently stop voice input
	}

	@Override
	protected boolean onNumber(int key, boolean hold, int repeat) {
		stopVoiceInput();
		return super.onNumber(key, hold, repeat);
	}


	/**
	 * Prevents Pound and Star keys from working as hotkeys when some function or panel is active.
	 * For example, it is confusing to change the language, open the settings or trigger some other function
	 * on, when the command palette is open or voice input is active. For this reason, we disable the Pound key
	 * and make the Star key stop voice input instead of navigating back.
	 */
	@Override
	public boolean onHotkey(int keyCode, boolean repeat, boolean validateOnly) {
		return switch (keyCode) {
			case KeyEvent.KEYCODE_STAR -> validateOnly || navigateBack();
			case KeyEvent.KEYCODE_POUND -> isFnPanelVisible(); // ignore the pound key when a function is active
			default -> false;
		};
	}


	protected boolean navigateBack() {
		if (!voiceInputOps.isListening()) {
			return false;
		}

		stopVoiceInput();
		return true;
	}


	public void toggleVoiceInput() {
		if (voiceInputOps.isListening() || !voiceInputOps.isAvailable()) {
			stopVoiceInput();
			return;
		}

		statusBar.setText(R.string.loading);
		suggestionOps.cancelDelayedAccept();
		mInputMode.onAcceptSuggestion(suggestionOps.acceptIncomplete());
		voiceInputOps.listen(mLanguage);
	}


	protected void stopVoiceInput() {
		if (voiceInputOps.isListening()) {
			statusBar.setText(R.string.voice_input_stopping);
			voiceInputOps.stop();
		}
		shouldCapitalizeVoiceInput = false;
	}


	private void onVoiceInputStarted() {
		// Determine capitalization once at the start based on current context
		shouldCapitalizeVoiceInput = false;
		if (settings.getAutoTextCase()) {
			String before = textField.getStringBeforeCursor();
			shouldCapitalizeVoiceInput = before.isEmpty()
				|| (settings.getAutoCapitalsAfterNewline() && before.endsWith("\n"))
				|| TextTools.isStartOfSentence(before);
		}

		if (!mainView.isCommandPaletteShown()) {
			mainView.render(); // disable the function keys
		}
		statusBar.setText(voiceInputOps);
	}


	private String applyAutoCapitalization(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		// Use the capitalization decision made at the start of voice input
		if (shouldCapitalizeVoiceInput) {
			return Character.toUpperCase(text.charAt(0)) + text.substring(1);
		}

		return text;
	}


	private void onVoiceInputStopped(String text) {
		Logger.d(LOG_TAG, "Voice input stopped: " + text);
		onText(applyAutoCapitalization(text), false);
		shouldCapitalizeVoiceInput = false;
		resetStatus();
		 if (!mainView.isCommandPaletteShown()) {
			 mainView.render(); // re-enable the function keys
		 }
	}


	private void onVoiceInputPartial(String text) {
		Logger.d(LOG_TAG, "Voice input partial: " + text);

		// Skip empty partial results
		if (text == null || text.isEmpty()) {
			return;
		}

		// Use composing text for partial results - this allows replacement without duplication
		textField.setComposingText(applyAutoCapitalization(text), 1);
	}


	private void onVoiceInputError(VoiceInputError error) {
		shouldCapitalizeVoiceInput = false;

		if (error.isLanguageMissing() && voiceInputOps.enableOfflineMode(mLanguage, false)) {
			Logger.i(LOG_TAG, "Voice input package for language '" + mLanguage.getName() + "' is missing. Enforcing online mode for the current session.");
			voiceInputOps.listen(mLanguage);
		} else if (error.isIrrelevantToUser()) {
			Logger.i(LOG_TAG, "Ignoring voice input. " + error.debugMessage);
			resetStatus(); // re-enable the function keys
		} else {
			Logger.e(LOG_TAG, "Failed to listen. " + error.debugMessage);
			statusBar.setError(error.toString());
			if (error.isNoPermission()) {
				RequestPermissionDialog.show(this, Manifest.permission.RECORD_AUDIO);
			}
		}

		 if (!mainView.isCommandPaletteShown()) {
			 mainView.render(); // re-enable the function keys
		 }
	}
}
