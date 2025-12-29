package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.github.sspanak.tt9.util.ConsumerCompat;

class VoiceListener implements RecognitionListener {
	private boolean listening = false;

	@NonNull private final Context context;
	@NonNull private final Runnable onStart;
	@NonNull private final ConsumerCompat<ArrayList<String>> onStop;
	@NonNull private final ConsumerCompat<ArrayList<String>> onPartial;
	@NonNull private final ConsumerCompat<VoiceInputError> onError;

	VoiceListener(
		@NonNull Context context,
		@Nullable Runnable onStart,
		@Nullable ConsumerCompat<ArrayList<String>> onStop,
		@Nullable ConsumerCompat<ArrayList<String>> onPartial,
		@Nullable ConsumerCompat<VoiceInputError> onError
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
