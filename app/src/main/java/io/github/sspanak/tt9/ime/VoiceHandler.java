package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.voice.VoiceInputError;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
import io.github.sspanak.tt9.ui.dialogs.RequestPermissionDialog;
import io.github.sspanak.tt9.util.Logger;

abstract class VoiceHandler extends TypingHandler {
	private final static String LOG_TAG = VoiceHandler.class.getSimpleName();
	private VoiceInputOps voiceInputOps;


	@Override
	protected void onInit() {
		super.onInit();

		voiceInputOps = new VoiceInputOps(
			this,
			this::onVoiceInputStarted,
			this::onVoiceInputStopped,
			this::onVoiceInputError
		);
	}


	public void toggleVoiceInput() {
		if (voiceInputOps.isListening() || !voiceInputOps.isAvailable()) {
			stopVoiceInput();
			return;
		}

		// @todo: update the readme and add info about the "com.google.android.googlequicksearchbox" package
		// @todo: translations

		statusBar.setText(R.string.loading);
		voiceInputOps.listen(mLanguage);
	}


	protected void stopVoiceInput() {
		voiceInputOps.stop();
		resetStatus();
	}


	private void onVoiceInputStarted() {
		statusBar.setText(voiceInputOps);
	}


	private void onVoiceInputStopped(String text) {
		onText(text, false);
		mainView.hideCommandPalette();
		resetStatus();
	}


	private void onVoiceInputError(VoiceInputError error) {
		if (error.isIrrelevantToUser()) {
			Logger.i(LOG_TAG, "Ignoring voice input. " + error.debugMessage);
			resetStatus();
		} else {
			Logger.e(LOG_TAG, "Failed to listen. " + error.debugMessage);
			statusBar.setError(error.toString());
			if (error.isNoPermission()) {
				RequestPermissionDialog.show(this);
			}
		}
	}
}
