package io.github.sspanak.tt9.ime.voice;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;

public class VoiceInputError {
	public final static int ERROR_NOT_AVAILABLE = 101;
	public final static int ERROR_INVALID_LANGUAGE = 102;
	public final static int ERROR_CANNOT_BIND_TO_VOICE_SERVICE = 103;
	public final static int ERROR_NO_PERMISSION = 104;
	public final static int ERROR_AUDIO_CAPTURE = 105;
	public final static int ERROR_PROCESSING = 106;
	public final static int ERROR_RECOGNIZER_BUSY = 107;

	public final int code;
	public final String message;
	public final String debugMessage;


	public VoiceInputError(Context context, int errorCode) {
		code = errorCode;
		debugMessage = codeToDebugString(errorCode);
		message = codeToString(context, errorCode);
	}


	public boolean isNoPermission() {
		return code == ERROR_NO_PERMISSION;
	}


	public boolean isIrrelevantToUser() {
		return false; // All errors are relevant for offline mode
	}


	public boolean isLanguageMissing() {
		return false; // No language packages needed for offline
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
			case ERROR_NO_PERMISSION
				-> context.getString(R.string.voice_input_error_no_permissions);
			case ERROR_NOT_AVAILABLE
				-> context.getString(R.string.voice_input_error_not_available);
			case ERROR_INVALID_LANGUAGE
				-> context.getString(R.string.voice_input_error_language_not_supported);
			case ERROR_AUDIO_CAPTURE
				-> "Failed to capture audio from microphone";
			case ERROR_PROCESSING
				-> "Failed to process audio";
			case ERROR_RECOGNIZER_BUSY
				-> "Voice input is already running";
			default -> context.getString(R.string.voice_input_error_generic);
		};
	}


	private static String codeToDebugString(int code) {
		return switch (code) {
			case ERROR_NO_PERMISSION -> "No microphone permissions.";
			case ERROR_NOT_AVAILABLE -> "Voice input is not available.";
			case ERROR_INVALID_LANGUAGE -> "Invalid language for voice input.";
			case ERROR_CANNOT_BIND_TO_VOICE_SERVICE -> "Cannot bind to the voice input service.";
			case ERROR_AUDIO_CAPTURE -> "Failed to capture audio.";
			case ERROR_PROCESSING -> "Failed to process audio.";
			case ERROR_RECOGNIZER_BUSY -> "Voice input service is busy.";
			default -> "Unknown voice input error code: " + code;
		};
	}
}