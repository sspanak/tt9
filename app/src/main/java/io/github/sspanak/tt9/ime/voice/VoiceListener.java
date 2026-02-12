package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

import io.github.sspanak.tt9.preferences.settings.SettingsStatic;

class VoiceListener implements RecognitionListener {
	private boolean listening = false;
	private volatile boolean startSuccess = false;

	@NonNull private final Context context;
	@NonNull private final Runnable onStart;
	@NonNull private final Consumer<ArrayList<String>> onStop;
	@NonNull private final Consumer<ArrayList<String>> onPartial;
	@NonNull private final Consumer<VoiceInputError> onError;
	@NonNull private final Handler startFailureHandler;

	VoiceListener(
		@NonNull Context context,
		@Nullable Runnable onStart,
		@Nullable Consumer<ArrayList<String>> onStop,
		@Nullable Consumer<ArrayList<String>> onPartial,
		@Nullable Consumer<VoiceInputError> onError
	) {
		this.context = context;
		this.onStart = onStart != null ? onStart : () -> {};
		this.startFailureHandler = new Handler(Looper.getMainLooper());
		this.onStop = onStop != null ? onStop : (t) -> {};
		this.onPartial = onPartial != null ? onPartial : (t) -> {};
		this.onError = onError != null ? onError : (e) -> {};
	}

	public boolean isListening() {
		return listening;
	}

	public void onBeforeStart() {
		startSuccess = false;

		startFailureHandler.removeCallbacksAndMessages(null);
		startFailureHandler.postDelayed(this::onStartFailure, SettingsStatic.VOICE_INPUT_START_FAILURE_TIMEOUT);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		startFailureHandler.removeCallbacksAndMessages(null);
		startSuccess = true;
		listening = true;
		onStart.run();
	}

	@Override
	public void onError(int error) {
		startFailureHandler.removeCallbacksAndMessages(null);
		listening = false;
		onError.accept(new VoiceInputError(context, error));
	}

	public void onStartFailure() {
		if (!startSuccess) {
			onError.accept(new VoiceInputError(context, VoiceInputError.ERROR_START_FAILURE));
		}
	}

	@Override
	public void onResults(Bundle resultsRaw) {
		startFailureHandler.removeCallbacksAndMessages(null);
		listening = false;

		ArrayList<String> results = resultsRaw.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		onStop.accept(results == null ? new ArrayList<>() : results);
	}

	@Override
	public void onPartialResults(Bundle resultsRaw) {
		startFailureHandler.removeCallbacksAndMessages(null);
		ArrayList<String> results = resultsRaw.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		onPartial.accept(results == null ? new ArrayList<>() : results);
	}

	// we don't care about these, but the interface requires us to implement them
	@Override public void onBeginningOfSpeech() {}
	@Override public void onEndOfSpeech() {}
	@Override public void onEvent(int e, Bundle b) {}
	@Override public void onRmsChanged(float r) {}
	@Override public void onBufferReceived(byte[] b) {}
}
