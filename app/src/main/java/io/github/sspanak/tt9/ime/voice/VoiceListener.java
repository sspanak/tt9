package io.github.sspanak.tt9.ime.voice;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.util.Logger;

class VoiceListener implements RecognitionListener {
	private boolean listening = false;
	private final @NonNull Runnable onStop;

	VoiceListener(@NonNull Runnable onStop) {
		this.onStop = onStop;
	}

	public boolean isListening() {
		return listening;
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		listening = true;
		Logger.d(getClass().getSimpleName(), " ====> ready for speech");
	}

	@Override
	public void onBeginningOfSpeech() {
		Logger.d(getClass().getSimpleName(), " ====> speech start");
	}

	@Override
	public void onEndOfSpeech() {
		Logger.d(getClass().getSimpleName(), " ====> speech end");
	}

	@Override
	public void onError(int error) {
		listening = false;
		onStop.run();

		Logger.e(getClass().getSimpleName(), "Speech recognition failed. " + decodeError(error));
	}

	@Override
	public void onResults(Bundle resultsRaw) {
		listening = false;
		onStop.run();

		Logger.d(getClass().getSimpleName(), " ====> speech end");
		ArrayList<String> results = resultsRaw.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if (results == null) {
			Logger.d(getClass().getSimpleName(), " ====> no results");
			return;
		}

		for (String result : results) {
			Logger.d(getClass().getSimpleName(), "\n\t - " + result);
		}
	}

	@Override
	public void onPartialResults(Bundle resultsRaw) {
		Logger.d(getClass().getSimpleName(), " ====> partial results");
		ArrayList<String> results = resultsRaw.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		if (results == null) {
			Logger.d(getClass().getSimpleName(), " ====> no results");
			return;
		}

		for (String result : results) {
			Logger.d(getClass().getSimpleName(), "\n\t - " + result);
		}
	}


	private String decodeError(int errorCode) {
		switch (errorCode) {
			case SpeechRecognizer.ERROR_AUDIO:
				return "Audio recording error";
			case SpeechRecognizer.ERROR_CLIENT:
				return "Client side error";
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				return "Insufficient permissions";
			case SpeechRecognizer.ERROR_NETWORK:
				return "Network error";
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				return "Network timeout";
			case SpeechRecognizer.ERROR_NO_MATCH:
				return "No match";
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				return "RecognitionService busy";
			case SpeechRecognizer.ERROR_SERVER:
				return "Error from server";
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				return "No speech input";
			default:
				return "Unknown error";
		}
	}


	// we don't care about these, but the interface requires us to implement them
	@Override public void onEvent(int e, Bundle b) {}
	@Override public void onRmsChanged(float r) {}
	@Override public void onBufferReceived(byte[] b) {}
}
