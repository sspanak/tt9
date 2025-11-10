package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.util.ConsumerCompat;

class VoiceListener implements RecognitionListener {
	private boolean listening = false;

	@NonNull private final Context context;
	private final Runnable onStart;
	private final ConsumerCompat<ArrayList<String>> onStop;
	private final ConsumerCompat<ArrayList<String>> onPartial;
	private final ConsumerCompat<VoiceInputError> onError;

	VoiceListener(
		@NonNull Context context,
		Runnable onStart,
		ConsumerCompat<ArrayList<String>> onStop,
		ConsumerCompat<ArrayList<String>> onPartial,
		ConsumerCompat<VoiceInputError> onError
	) {
		this.context = context;
		this.onStart = onStart != null ? onStart : () -> {};
		this.onStop = onStop != null ? onStop : (t) -> {};
		this.onPartial = onPartial != null ? onPartial : (t) -> {};
		this.onError = onError != null ? onError : (e) -> {};
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
		onError.accept(new VoiceInputError(context, error));
	}

	@Override
	public void onResults(Bundle resultsRaw) {
		listening = false;

		ArrayList<String> results = resultsRaw.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		onStop.accept(results == null ? new ArrayList<>() : results);
	}

	@Override
	public void onPartialResults(Bundle resultsRaw) {
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
