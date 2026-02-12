package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class SpeechRecognizerSupportLegacy {
	SpeechRecognizerSupportLegacy() {}

	SpeechRecognizerSupportLegacy setLanguage(@Nullable Language l) { return this; }

	boolean isAlternativeAvailable(@NonNull Context context) {
		final List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(
			VoiceInputOps.createIntent(null),
			PackageManager.MATCH_DEFAULT_ONLY
		);

		return resolveInfo.size() > 1;
	}

	boolean isGoogleOfflineRecognitionAvailable(@NonNull Context context) {
		return DeviceInfo.AT_LEAST_ANDROID_12 && SpeechRecognizer.isOnDeviceRecognitionAvailable(context);
	}

	boolean isGoogleOnlineRecognitionAvailable(@NonNull Context context) {
		return SpeechRecognizer.isRecognitionAvailable(context);
	}

	boolean isLanguageSupportedOffline(@NonNull Context c, @Nullable Language l) { return false; }
	void checkOfflineSupport(@NonNull Context c, @NonNull Runnable onSupportChecked) { onSupportChecked.run(); }
}
