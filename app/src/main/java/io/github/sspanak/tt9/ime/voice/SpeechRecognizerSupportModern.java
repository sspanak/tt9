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
		super();
		this.ims = ims;
	}


	@Override
	SpeechRecognizerSupportModern setLanguage(@Nullable Language language) {
		locale = language == null ? null : VoiceInputOps.getLocale(language);
		return this;
	}


	@Override
	void checkOfflineSupport(@NonNull Context context, @NonNull Runnable onSupportChecked) {
		if (
			locale == null
			|| !isGoogleOfflineRecognitionAvailable(context)
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
	boolean isLanguageSupportedOffline(@NonNull Context context, @Nullable Language language) {
		return language != null && isGoogleOfflineRecognitionAvailable(context) && availableOfflineLanguages.contains(VoiceInputOps.getLocale(language));
	}


	public void onSupportResult(@NonNull RecognitionSupport recognitionSupport) {
		recognizer.destroy();

		List<String> installed = recognitionSupport.getInstalledOnDeviceLanguages();
		List<String> missing = recognitionSupport.getSupportedOnDeviceLanguages();

		if (installed.contains(locale)) {
			availableOfflineLanguages.add(locale);
			missingOfflineLanguages.remove(locale);
		} else if (missing.contains(locale)) {
			availableOfflineLanguages.remove(locale);
			missingOfflineLanguages.remove(locale);
		}	else {
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
