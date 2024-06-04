package io.github.sspanak.tt9.ime.voice;

import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.ui.dialogs.RequestPermissionDialog;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class VoiceInputOps {
	private final boolean isOnDeviceRecognitionAvailable;
	private final boolean isRecognitionAvailable;
	private final InputMethodService ims;
	private SpeechRecognizer speechRecognizer;
	private final VoiceListener listener;

	private final ConsumerCompat<String> onStopListening;
	private final ConsumerCompat<VoiceInputError> onListeningError;


	public VoiceInputOps(
		@NonNull InputMethodService ims,
		@NonNull Runnable onStart,
		@NonNull ConsumerCompat<String> onStop,
		@NonNull ConsumerCompat<VoiceInputError> onError
	) {
		isOnDeviceRecognitionAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(ims);
		isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(ims);
		listener = new VoiceListener(onStart, this::onStop, this::onError);

		onStopListening = onStop;
		onListeningError = onError;

		this.ims = ims;
	}


	private void createRecognizer() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isOnDeviceRecognitionAvailable) {
			speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(ims);
		} else if (isRecognitionAvailable) {
			speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ims);
		} else {
			return;
		}

		speechRecognizer.setRecognitionListener(listener);
	}


	public boolean isAvailable() {
		return isRecognitionAvailable || isOnDeviceRecognitionAvailable;
	}


	public boolean isListening() {
		return listener.isListening();
	}


	public void listen(Language language) {
		if (language == null) {
			onListeningError.accept(new VoiceInputError(VoiceInputError.ERROR_INVALID_LANGUAGE));
			return;
		}

		if (!isAvailable()) {
			onListeningError.accept(new VoiceInputError(VoiceInputError.ERROR_NOT_AVAILABLE));
			return;
		}

		if (isListening()) {
			onListeningError.accept(new VoiceInputError(SpeechRecognizer.ERROR_RECOGNIZER_BUSY));
			return;
		}

		// @todo: if permissions are already requested, return an error

		createRecognizer();

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.getLocale().toString());
		speechRecognizer.startListening(intent);
		Logger.d(getClass().getSimpleName(), "SpeechRecognizer started");
	}


	public void stop() {
		if (isAvailable() && listener.isListening()) {
			speechRecognizer.stopListening();
		}
	}


	private void destroy() {
		if (speechRecognizer != null) {
			speechRecognizer.destroy();
			speechRecognizer = null;
			Logger.d(getClass().getSimpleName(), "SpeechRecognizer destroyed");
		}
	}


	private void onStop(ArrayList<String> results) {
		destroy();
		onStopListening.accept(results.isEmpty() ? null : results.get(0));
	}


	private void onError(VoiceInputError error) {
		if (error.isNoPermission()) {
			RequestPermissionDialog.show(ims);
			return;
		}

		destroy();
		onListeningError.accept(error);
	}
}
