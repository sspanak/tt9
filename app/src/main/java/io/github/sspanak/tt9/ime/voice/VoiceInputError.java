package io.github.sspanak.tt9.ime.voice;

import android.content.Context;
import android.speech.SpeechRecognizer;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class VoiceInputError {
	public final static int ERROR_NOT_AVAILABLE = 101;
	public final static int ERROR_INVALID_LANGUAGE = 102;
	public final static int ERROR_CANNOT_BIND_TO_VOICE_SERVICE = 103;

	public final int code;
	public final String message;
	public final String debugMessage;


	public VoiceInputError(Context context, int errorCode) {
		code = errorCode;
		debugMessage = codeToDebugString(errorCode);
		message = codeToString(context, errorCode);
	}


	public boolean isNoPermission() {
		return code == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
	}


	public boolean isIrrelevantToUser() {
		return
			code == SpeechRecognizer.ERROR_NO_MATCH
			|| code == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
			|| code == SpeechRecognizer.ERROR_AUDIO
			|| (DeviceInfo.AT_LEAST_ANDROID_12 && code == SpeechRecognizer.ERROR_SERVER_DISCONNECTED)
			|| (DeviceInfo.AT_LEAST_ANDROID_13 && code == SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT)
			|| (DeviceInfo.AT_LEAST_ANDROID_14 && code == SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS);
	}


	public boolean isLanguageMissing() {
		return DeviceInfo.AT_LEAST_ANDROID_12 && code == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE;
	}


	@NonNull
	@Override
	public String toString() {
		return message;
	}


	@NonNull
	private static String codeToString(Context context, int code) {
		return switch (code) {
			case ERROR_CANNOT_BIND_TO_VOICE_SERVICE
				-> context.getString(R.string.voice_input_error_incompatible_voice_service);
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS
				-> context.getString(R.string.voice_input_error_no_permissions);
			case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED
				-> context.getString(R.string.voice_input_error_language_not_supported);
			case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE
				-> context.getString(R.string.voice_input_error_language_missing);
			case SpeechRecognizer.ERROR_NETWORK
				-> context.getString(R.string.voice_input_error_no_network);
			case ERROR_NOT_AVAILABLE
				-> context.getString(R.string.voice_input_error_not_available);
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
					 SpeechRecognizer.ERROR_SERVER,
					 SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
					 SpeechRecognizer.ERROR_TOO_MANY_REQUESTS
				-> context.getString(R.string.voice_input_error_network_failed);
			default -> context.getString(R.string.voice_input_error_generic);
		};
	}


	private static String codeToDebugString(int code) {
		if (DeviceInfo.AT_LEAST_ANDROID_14 && code == SpeechRecognizer.ERROR_CANNOT_LISTEN_TO_DOWNLOAD_EVENTS) {
			return "Cannot listen to download events.";
		}

		if (DeviceInfo.AT_LEAST_ANDROID_13 && code == SpeechRecognizer.ERROR_CANNOT_CHECK_SUPPORT) {
			return "Cannot check voice input support.";
		}

		String message = codeToDebugStringCommon(code);
		message = message != null ? message : "Unknown voice input error code: " + code;

		return message;
	}


	private static String codeToDebugStringCommon(int code) {
		return switch (code) {
			case SpeechRecognizer.ERROR_AUDIO -> "Audio capture error.";
			case SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error.";
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "No microphone permissions.";
			case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> "Language not supported.";
			case SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "Language missing. Try again later.";
			case SpeechRecognizer.ERROR_NETWORK -> "No network connection.";
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout.";
			case SpeechRecognizer.ERROR_NO_MATCH -> "No match.";
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice input service is busy.";
			case SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error.";
			case SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "Lost connection to the server.";
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected.";
			case SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "Server overloaded. Try again later.";
			case ERROR_NOT_AVAILABLE -> "Voice input is not available.";
			case ERROR_INVALID_LANGUAGE -> "Invalid language for voice input.";
			case ERROR_CANNOT_BIND_TO_VOICE_SERVICE -> "Cannot bind to the current voice input service.";
			default -> null;
		};
	}
}
