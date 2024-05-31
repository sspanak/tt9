package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

public class VoiceInputOps {
    private final boolean isOnDeviceRecognitionAvailable;
	private final boolean isRecognitionAvailable;
	private final Context context;
	private SpeechRecognizer speechRecognizer;
	private final VoiceListener listener;


	public VoiceInputOps(Context context) {
		isOnDeviceRecognitionAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context);
		isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(context);
		listener = new VoiceListener(this::onStop);

		this.context = context;
	}


	private void createRecognizer() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isOnDeviceRecognitionAvailable) {
			speechRecognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(context);
		} else if (isRecognitionAvailable) {
			speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
		} else {
			return;
		}

		speechRecognizer.setRecognitionListener(listener);
	}


	public boolean isAvailable() {
		return isRecognitionAvailable || isOnDeviceRecognitionAvailable;
	}


	public boolean isListening() {
		return listener.isListening();
	}


	public void listen() {
		if (!isAvailable() || listener.isListening()) {
			return;
		}

		createRecognizer();

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		speechRecognizer.startListening(intent);
	}


	public void stop() {
		if (isAvailable() && listener.isListening()) {
			speechRecognizer.stopListening();
		}
	}


	private void onStop() {
		if (speechRecognizer != null) {
			speechRecognizer.destroy();
			speechRecognizer = null;
		}
	}
}
