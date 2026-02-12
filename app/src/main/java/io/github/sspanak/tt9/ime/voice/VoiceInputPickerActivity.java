package io.github.sspanak.tt9.ime.voice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.UI;
import io.github.sspanak.tt9.util.Logger;

public class VoiceInputPickerActivity extends AppCompatActivity {
	private static final String LOG_TAG = VoiceInputPickerActivity.class.getSimpleName();

	private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
		new ActivityResultContracts.StartActivityForResult(),
		this::onResult
	);


	public static Intent generateShowIntent(Context context) {
		Intent intent = new Intent(context, VoiceInputPickerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		return intent;
	}


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		launchPicker();
	}


	private void launchPicker() {
		final Language language = LanguageCollection.getLanguage(new SettingsStore(this).getInputLanguage());
		final String locale = language != null ? VoiceInputOps.getLocale(language) : null;
		launcher.launch(VoiceInputOps.createIntent(locale));
	}


	private void onResult(ActivityResult result) {
		if (result == null) {
			Logger.e(LOG_TAG, "Alternative voice input failed. NULL activity result received.");
		} else if (result.getResultCode() != Activity.RESULT_OK) {
			Logger.e(LOG_TAG, "Alternative voice input failed with code: " + result.getResultCode());
		} else {
			sendToMain(result.getData());
		}

		finish();
	}


	private void sendToMain(@Nullable Intent voiceIntent) {
		if (voiceIntent == null) {
			Logger.d(LOG_TAG, "Alternative voice input ended with a NULL result");
			return;
		}

		ArrayList<String> results = voiceIntent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		if (results == null || results.isEmpty()) {
			results = voiceIntent.getStringArrayListExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS);
		}

		final String text = results == null || results.isEmpty() ? null : results.get(0);

		final Intent printIntent = new Intent(this, TraditionalT9.class);
		printIntent.putExtra(UI.COMMAND, UI.COMMAND_PRINT_VOICE_INPUT);
		printIntent.putExtra(UI.COMMAND_PRINT_VOICE_INPUT_TEXT, text);

		try {
			startService(printIntent);
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed to send voice input text to main. " + e.getMessage());
		}
	}
}
