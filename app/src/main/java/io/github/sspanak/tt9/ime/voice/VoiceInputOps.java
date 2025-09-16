package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class VoiceInputOps {
	private final static String LOG_TAG = VoiceInputOps.class.getSimpleName();

	private final Context ims;
	private final HashMap<Integer, Boolean> isOfflineModeDisabled;
	private Language language;
	private final VoiceListener listener;
	private final SpeechRecognizerSupportLegacy recognizerSupport;
	private SpeechRecognizer speechRecognizer;

	private final ConsumerCompat<String> onStopListening;
	private final ConsumerCompat<VoiceInputError> onListeningError;


	public VoiceInputOps(
		@NonNull Context ims,
		Runnable onStart,
		ConsumerCompat<String> onStop,
		ConsumerCompat<VoiceInputError> onError
	) {
		isOfflineModeDisabled = new HashMap<>();
		listener = new VoiceListener(ims, onStart, this::onStop, this::onError);
		recognizerSupport = DeviceInfo.AT_LEAST_ANDROID_13 ? new SpeechRecognizerSupportModern(ims) : new SpeechRecognizerSupportLegacy(ims);

		onStopListening = onStop != null ? onStop : result -> {};
		onListeningError = onError != null ? onError : error -> {};

		this.ims = ims;
	}


	static String getLocale(@NonNull Language lang) {
		return lang.getLocale().toString().replace("_", "-");
	}


	static Intent createIntent(@NonNull String locale) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		return intent;
	}


	private void createRecognizer(@Nullable Language language) {
		boolean isLanguageAllowedOffline = language != null && !Boolean.TRUE.equals(isOfflineModeDisabled.get(language.getId()));

		if (isLanguageAllowedOffline && DeviceInfo.AT_LEAST_ANDROID_13 && recognizerSupport.isLanguageSupportedOffline(language)) {
			Logger.d(LOG_TAG, "Creating offline SpeechRecognizer...");
			speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(ims);
		} else if (recognizerSupport.isRecognitionAvailable) {
			Logger.d(LOG_TAG, "Creating online SpeechRecognizer...");
			speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ims);
		} else {
			Logger.d(LOG_TAG, "Cannot create SpeechRecognizer, recognition not available.");
			return;
		}

		speechRecognizer.setRecognitionListener(listener);
	}


	public boolean isAvailable() {
		return recognizerSupport.isRecognitionAvailable || recognizerSupport.isOnDeviceRecognitionAvailable;
	}


	public boolean isListening() {
		return listener.isListening() && speechRecognizer != null;
	}


	public void listen(@Nullable Language language) {
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

		this.language = language;
		recognizerSupport.setLanguage(language).checkOfflineSupport(this::listenAsync);
	}


	private void listenAsync() {
		Handler mainHandler = new Handler(Looper.getMainLooper());
		mainHandler.post(this::listen);
	}


	private void listen() {
		if (!isAvailable()) {
			onListeningError.accept(new VoiceInputError(ims, VoiceInputError.ERROR_NOT_AVAILABLE));
			return;
		}

		if (isListening()) {
			onListeningError.accept(new VoiceInputError(ims, SpeechRecognizer.ERROR_RECOGNIZER_BUSY));
			return;
		}

		createRecognizer(language);

		String locale = getLocale(language);

		try {
			speechRecognizer.startListening(createIntent(locale));
			Logger.d(LOG_TAG, "SpeechRecognizer started for locale: " + locale);
		} catch (SecurityException e) {
			Logger.e(LOG_TAG, "SpeechRecognizer start failed due to a SecurityException. " + e.getMessage());
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
					Logger.e(LOG_TAG, "SpeechRecognizer destroy failed. " + e.getMessage() + ". Retrying...");
					continue;
				} else {
					Logger.e(LOG_TAG, "SpeechRecognizer destroy failed. " + e.getMessage() + ". Giving up and just nulling the reference.");
				}
			}

			speechRecognizer = null;
			Logger.d(LOG_TAG, "SpeechRecognizer destroyed");
		}
	}


	public boolean enableOfflineMode(@NonNull Language language, boolean yes) {
		boolean isCurrentlyAllowed = !Boolean.TRUE.equals(isOfflineModeDisabled.get(language.getId()));

		if (yes != isCurrentlyAllowed) {
			isOfflineModeDisabled.put(language.getId(), !yes);
		}

		return isCurrentlyAllowed != yes;
	}


	public void enableOfflineMode() {
		for (Integer langId : isOfflineModeDisabled.keySet()) {
			isOfflineModeDisabled.put(langId, false);
		}

		Logger.d(LOG_TAG, "Re-enabled offline voice input for all languages");
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
