package io.github.sspanak.tt9.ime.voice;

import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

public class VoiceInputError {
	public final static int ERROR_BUSY = 100;
	public final static int ERROR_NOT_AVAILABLE = 101;
	public final static int ERROR_INVALID_LANGUAGE = 102;

	public final int code;
	public final String message;

	public VoiceInputError(int errorCode) {
		code = errorCode;
		message = decodeError(errorCode);
	}

	public boolean isNoPermission() {
		return code == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
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
			case ERROR_BUSY:
				return "Speech recognition is already in progress";
			case ERROR_NOT_AVAILABLE:
				return "Speech recognition is not available";
			case ERROR_INVALID_LANGUAGE:
				return "Invalid language";
			default:
				return "Unknown error";
		}
	}

	@NonNull
	@Override
	public String toString() {
		return message;
	}
}
