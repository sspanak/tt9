package io.github.sspanak.tt9.ime.voice;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.util.ConsumerCompat;

class VoiceListener implements RecognitionListener {
	private boolean listening = false;

	private final @NonNull Runnable onStart;
	private final @NonNull ConsumerCompat<ArrayList<String>> onStop;
	private final @NonNull ConsumerCompat<VoiceInputError> onError;

	VoiceListener(
		@NonNull Runnable onStart,
		@NonNull ConsumerCompat<ArrayList<String>> onStop,
		@NonNull ConsumerCompat<VoiceInputError> onError
	) {
		this.onStart = onStart;
		this.onStop = onStop;
		this.onError = onError;
	}

	public boolean isListening() {
		return listening;
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		listening = true;
		onStart.run();
	}

	@Override
	public void onError(int error) {
		listening = false;
		onError.accept(new VoiceInputError(error));
	}

	@Override
	public void onResults(Bundle resultsRaw) {
		listening = false;

		ArrayList<String> results = resultsRaw.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		onStop.accept(results == null ? new ArrayList<>() : results);
	}

	// we don't care about these, but the interface requires us to implement them
	@Override public void onPartialResults(Bundle results) {}
	@Override public void onBeginningOfSpeech() {}
	@Override public void onEndOfSpeech() {}
	@Override public void onEvent(int e, Bundle b) {}
	@Override public void onRmsChanged(float r) {}
	@Override public void onBufferReceived(byte[] b) {}
}
