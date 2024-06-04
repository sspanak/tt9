package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.os.Build;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import java.util.HashMap;

import io.github.sspanak.tt9.R;

public class VoiceInputError {
	public final static int ERROR_NOT_AVAILABLE = 101;
	public final static int ERROR_INVALID_LANGUAGE = 102;


	public final int code;
	public final String message;

	public VoiceInputError(int errorCode) {
		code = errorCode;
		message = codeToMessage(errorCode);
	}

	public boolean isNoPermission() {
		return code == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
	}

	public boolean isIrrelevantToUser() {
		return
			code == SpeechRecognizer.ERROR_NO_MATCH
			|| code == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
			|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && code == SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS)
			|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && code == SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT)
			|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && code == SpeechRecognizer.ERROR_SERVER_DISCONNECTED);
	}

	private void initModernErrors() {

	}

	private String codeToMessage(int code) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && code == SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS) {
			return "Cannot listen to download events.";
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && code == SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT) {
			return "Cannot check voice input support.";
		}


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			switch (code) {
				case SpeechRecognizer.ERROR_TOO_MANY_REQUESTS:
					return "Server overloaded. Try again later.";
				case SpeechRecognizer.ERROR_SERVER_DISCONNECTED:
					return "Lost connection to the server.";
				case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
					return "Language not supported.";
				case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:
					return "Language missing. Try again later.";
			}
		}

		switch (code) {
			case SpeechRecognizer.ERROR_AUDIO:
				return "Audio capture error.";
			case SpeechRecognizer.ERROR_CLIENT:
				return "Speech recognition client error.";
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				return "No microphone permissions.";
			case SpeechRecognizer.ERROR_NETWORK:
				return "No network connection.";
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				return "Network timeout.";
			case SpeechRecognizer.ERROR_NO_MATCH:
				return "No match.";
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				return "Voice input service is busy.";
			case SpeechRecognizer.ERROR_SERVER:
				return "Speech recognition server error.";
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				return "No speech detected.";
			case ERROR_NOT_AVAILABLE:
				return "Voice input is not available.";
			case ERROR_INVALID_LANGUAGE:
				return "Invalid language for voice input.";
			default:
				return "Unknown voice input error.";
		}
	}


	@NonNull
	@Override
	public String toString() {
		return message;
	}


	@NonNull
	public String toUserString(Context context) {
		switch (code) {
			case SpeechRecognizer.ERROR_AUDIO:
				return context.getString(R.string.voice_input_error_ERROR_AUDIO);
			case SpeechRecognizer.ERROR_CLIENT:
				return context.getString(R.string.voice_input_error_ERROR_CLIENT);
			case ERROR_INVALID_LANGUAGE:
				return context.getString(R.string.voice_input_error_ERROR_INVALID_LANGUAGE);
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				return context.getString(R.string.voice_input_error_ERROR_INSUFFICIENT_PERMISSIONS);
			case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
				return context.getString(R.string.voice_input_error_ERROR_LANGUAGE_NOT_SUPPORTED);
			case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE:
				return context.getString(R.string.voice_input_error_ERROR_LANGUAGE_UNAVAILABLE);
			case SpeechRecognizer.ERROR_NETWORK:
				return context.getString(R.string.voice_input_error_ERROR_NETWORK);
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				return context.getString(R.string.voice_input_error_ERROR_NETWORK_TIMEOUT);
			case ERROR_NOT_AVAILABLE:
				return context.getString(R.string.voice_input_error_ERROR_NOT_AVAILABLE);
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				return context.getString(R.string.voice_input_error_ERROR_RECOGNIZER_BUSY);
			case SpeechRecognizer.ERROR_SERVER:
				return context.getString(R.string.voice_input_error_ERROR_SERVER);
			case SpeechRecognizer.ERROR_SERVER_DISCONNECTED:
				return context.getString(R.string.voice_input_error_ERROR_SERVER_DISCONNECTED);
			case SpeechRecognizer.ERROR_TOO_MANY_REQUESTS:
				return context.getString(R.string.voice_input_error_ERROR_TOO_MANY_REQUESTS);
			default:
				return context.getString(R.string.voice_input_error_GENERIC);
		}
	}
}
