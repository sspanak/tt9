package io.github.sspanak.tt9.ime;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.voice.VoiceInputError;
import io.github.sspanak.tt9.ime.voice.VoiceInputOps;
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


	public void startVoiceInput() {
		if (voiceInputOps.isListening()) {
			stopVoiceInput();
			return;
		}

		// @todo: add a virtual numpad button

		statusBar.setText("Loading..."); // @todo: translations
		voiceInputOps.listen(mLanguage);
	}


	public void stopVoiceInput() {
		voiceInputOps.stop();
		statusBar.setText(mInputMode);
	}


	private void onVoiceInputStarted() {
		statusBar.setText("Listening...");
	}


	private void onVoiceInputStopped(String text) {
		onText(text, false);
		statusBar.setText(mInputMode);
		mainView.hideCommandPalette();
	}


	private void onVoiceInputError(VoiceInputError error) {
		if (error.isIrrelevantToUser()) {
			Logger.i(LOG_TAG, "Ignoring voice input. " + error);
			statusBar.setText(getString(R.string.commands_select_command));
		} else {
			Logger.e(LOG_TAG, "Failed to listen. " + error);
			statusBar.setText("❌  " + error);
		}
	}
}
