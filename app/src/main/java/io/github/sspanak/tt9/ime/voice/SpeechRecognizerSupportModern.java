package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.os.Build;
import android.speech.RecognitionSupport;
import android.speech.RecognitionSupportCallback;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;

import io.github.sspanak.tt9.languages.Language;


@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
class SpeechRecognizerSupportModern extends SpeechRecognizerSupportLegacy implements RecognitionSupportCallback {
	private final HashSet<String> missingOfflineLanguages = new HashSet<>();
	private final HashSet<String> availableOfflineLanguages = new HashSet<>();

	private final Context ims;
	private String locale;
	private Runnable onSupportChecked;
	private SpeechRecognizer recognizer;


	SpeechRecognizerSupportModern(@NonNull Context ims) {
		super(ims);
		this.ims = ims;
	}


	@Override
	SpeechRecognizerSupportModern setLanguage(@Nullable Language language) {
		locale = language == null ? null : VoiceInputOps.getLocale(language);
		return this;
	}


	@Override
	void checkOfflineSupport(@NonNull Runnable onSupportChecked) {
		if (
			locale == null
			|| !isOnDeviceRecognitionAvailable
			|| missingOfflineLanguages.contains(locale)
			|| availableOfflineLanguages.contains(locale)
		) {
			onSupportChecked.run();
			return;
		}

		this.onSupportChecked = onSupportChecked;

		recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(ims);
		recognizer.checkRecognitionSupport(VoiceInputOps.createIntent(locale), Executors.newSingleThreadExecutor(), this);
	}


	@Override
	boolean isLanguageSupportedOffline(@Nullable Language language) {
		return language != null && isOnDeviceRecognitionAvailable && availableOfflineLanguages.contains(VoiceInputOps.getLocale(language));
	}


	public void onSupportResult(@NonNull RecognitionSupport recognitionSupport) {
		recognizer.destroy();

		List<String> locales = recognitionSupport.getSupportedOnDeviceLanguages();
		if (locales.contains(locale)) {
			availableOfflineLanguages.add(locale);
			missingOfflineLanguages.remove(locale);
		} else {
			availableOfflineLanguages.remove(locale);
			missingOfflineLanguages.add(locale);
		}

		onSupportChecked.run();
	}


	public void onError(int error) {
		recognizer.destroy();
		missingOfflineLanguages.add(locale);
		onSupportChecked.run();
	}
}
