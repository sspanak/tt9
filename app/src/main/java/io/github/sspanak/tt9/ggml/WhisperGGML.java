package io.github.sspanak.tt9.ggml;

import androidx.annotation.NonNull;

import java.nio.Buffer;

import io.github.sspanak.tt9.util.ConsumerCompat;

public class WhisperGGML {
	private static final String CANCELLED_PREFIX = "<>CANCELLED<> lang=";

	static {
		System.loadLibrary("voiceinput");
	}

	private final long handle;
	private final ConsumerCompat<String> onPartialResult;

	public WhisperGGML(@NonNull Buffer modelBuffer, @NonNull ConsumerCompat<String> onPartialResult) {
		this.handle = openFromBufferNative(modelBuffer);
		this.onPartialResult = onPartialResult;
	}

	public String infer(
		float[] samples,
		String prompt,
		String[] languages,
		String[] bailLanguages,
		int decodingMode,
		boolean suppressNonSpeechTokens
	) throws BailLanguageException {
		String result = inferNative(handle, samples, prompt, languages, bailLanguages, decodingMode, suppressNonSpeechTokens);
		if (result != null && result.startsWith(CANCELLED_PREFIX)) {
			String lang = result.substring(CANCELLED_PREFIX.length());
			throw new BailLanguageException(lang);
		}
		return result;
	}

	public void close() {
		closeNative(handle);
	}

	// Called from JNI
	@SuppressWarnings("unused")
	public void invokePartialResult(String text) {
		onPartialResult.accept(text);
	}

	private static native long openFromBufferNative(Buffer buffer);
	private native String inferNative(long handle, float[] samples, String prompt, String[] languages, String[] bailLanguages, int decodingMode, boolean suppressNonSpeechTokens);
	private static native void closeNative(long handle);

	public static class BailLanguageException extends Exception {
		public final String language;

		public BailLanguageException(String language) {
			super("Bailed due to language: " + language);
			this.language = language;
		}
	}
}
