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

		// @todo: (!!!) The error class is overcomplicated. Also some errors are not available before API < 31.

		// @todo: add a virtual numpad button
		// @todo: hide the microphone buttons when there is no support for voice input
		// @todo: if permissions are already denied, produce the Insufficient Permissions error
		// @todo: translations

		statusBar.setText(R.string.loading);
		voiceInputOps.listen(mLanguage);
	}


	public void stopVoiceInput() {
		voiceInputOps.stop();
		statusBar.setText(mInputMode);
	}


	private void onVoiceInputStarted() {
		statusBar.setText(R.string.voice_input_listening);
	}


	private void onVoiceInputStopped(String text) {
		onText(text, false);
		statusBar.setText(mInputMode);
		mainView.hideCommandPalette();
	}


	private void onVoiceInputError(VoiceInputError error) {
		if (error.isIrrelevantToUser()) {
			Logger.i(LOG_TAG, "Ignoring voice input. " + error);
			statusBar.setText(R.string.commands_select_command);
		} else {
			Logger.e(LOG_TAG, "Failed to listen. " + error);
			statusBar.setText("❌  " + error.toUserString(this));
		}
	}
}
