package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

public class VoiceInputOps {
	private final boolean isOnDeviceRecognitionAvailable;
	private final boolean isRecognitionAvailable;


	private final Context ims;
	private Language language;
	private SpeechRecognizer speechRecognizer;
	private final VoiceListener listener;

	private final ConsumerCompat<String> onStopListening;
	private final ConsumerCompat<VoiceInputError> onListeningError;


	public VoiceInputOps(
		@NonNull Context ims,
		Runnable onStart,
		ConsumerCompat<String> onStop,
		ConsumerCompat<VoiceInputError> onError
	) {
		isOnDeviceRecognitionAvailable = DeviceInfo.AT_LEAST_ANDROID_12 && SpeechRecognizer.isOnDeviceRecognitionAvailable(ims);
		isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(ims);
		listener = new VoiceListener(ims, onStart, this::onStop, this::onError);

		onStopListening = onStop != null ? onStop : result -> {};
		onListeningError = onError != null ? onError : error -> {};

		this.ims = ims;
	}


	private void createRecognizer() {
		if (DeviceInfo.AT_LEAST_ANDROID_12 && isOnDeviceRecognitionAvailable) {
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
		return listener.isListening() && speechRecognizer != null;
	}


	public void listen(Language language) {
		if (language == null) {
			onListeningError.accept(new VoiceInputError(ims, VoiceInputError.ERROR_INVALID_LANGUAGE));
			return;
		}

		if (!isAvailable()) {
			onListeningError.accept(new VoiceInputError(ims, VoiceInputError.ERROR_NOT_AVAILABLE));
			return;
		}

		if (isListening()) {
			onListeningError.accept(new VoiceInputError(ims, SpeechRecognizer.ERROR_RECOGNIZER_BUSY));
			return;
		}

		createRecognizer();

		this.language = language;
		String locale = language.getLocale().toString().replace("_", "-");

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, toString());

		try {
			speechRecognizer.startListening(intent);
			Logger.d(getClass().getSimpleName(), "SpeechRecognizer started for locale: " + locale);
		} catch (SecurityException e) {
			Logger.e(getClass().getSimpleName(), "SpeechRecognizer start failed due to a SecurityException. " + e.getMessage());
			onError(new VoiceInputError(ims, VoiceInputError.ERROR_CANNOT_BIND_TO_VOICE_SERVICE));
		}
	}


	public void stop() {
		this.language = null;
		if (isAvailable() && isListening()) {
			speechRecognizer.stopListening();
		}
	}


	private void destroy() {
		this.language = null;

		// We try this multiple times, because it can fail due to a bug in the Android SDK
		// https://github.com/sspanak/tt9/issues/593
		for (int i = 0; i < 3 && speechRecognizer != null; i++) {
			try {
				speechRecognizer.destroy();
			} catch (IllegalArgumentException e) {
				if (i < 2) {
					Logger.e(getClass().getSimpleName(), "SpeechRecognizer destroy failed. " + e.getMessage() + ". Retrying...");
					continue;
				} else {
					Logger.e(getClass().getSimpleName(), "SpeechRecognizer destroy failed. " + e.getMessage() + ". Giving up and just nulling the reference.");
				}
			}

			speechRecognizer = null;
			Logger.d(getClass().getSimpleName(), "SpeechRecognizer destroyed");
		}
	}


	private void onStop(ArrayList<String> results) {
		destroy();
		onStopListening.accept(results.isEmpty() ? null : results.get(0));
	}


	private void onError(VoiceInputError error) {
		destroy();
		onListeningError.accept(error);
	}

	@NonNull
	@Override
	public String toString() {
		String languageSuffix = language == null ? "" : " / " + language.getName();
		return ims.getString(R.string.voice_input_listening) + languageSuffix;
	}
}
