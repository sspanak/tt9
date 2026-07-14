package io.github.sspanak.tt9.ml;

/**
 * Audio processing utilities for speech recognition
 */
public class AudioProcessor {

	/**
	 * Convert short audio samples to float samples normalized to [-1, 1]
	 */
	public static float[] shortToFloat(short[] shorts) {
		float[] floats = new float[shorts.length];
		for (int i = 0; i < shorts.length; i++) {
			floats[i] = shorts[i] / 32768.0f;
		}
		return floats;
	}

	/**
	 * Calculate RMS (Root Mean Square) magnitude of audio samples
	 */
	public static float calculateMagnitude(float[] samples, int start, int length) {
		if (samples == null || samples.length == 0 || length == 0) {
			return 0.0f;
		}

		double sum = 0.0;
		int count = Math.min(length, samples.length - start);

		for (int i = start; i < start + count; i++) {
			sum += samples[i] * samples[i];
		}

		return (float) Math.sqrt(sum / count);
	}

	/**
	 * Check if audio magnitude indicates speech
	 */
	public static boolean isSpeech(float magnitude) {
		// Threshold determined empirically
		// Typical background noise: 0.001-0.01
		// Speech: 0.02-0.5
		return magnitude > 0.015f;
	}

	/**
	 * Resample audio if needed (basic implementation)
	 * Note: This is a simple decimation. For production use a proper resampler.
	 */
	public static float[] resample(float[] samples, int originalRate, int targetRate) {
		if (originalRate == targetRate) {
			return samples;
		}

		double ratio = (double) originalRate / targetRate;
		int newLength = (int) (samples.length / ratio);
		float[] resampled = new float[newLength];

		for (int i = 0; i < newLength; i++) {
			int srcIndex = (int) (i * ratio);
			if (srcIndex < samples.length) {
				resampled[i] = samples[srcIndex];
			}
		}

		return resampled;
	}

	/**
	 * Pad or trim audio to exact length
	 */
	public static float[] padOrTrim(float[] samples, int targetLength) {
		if (samples.length == targetLength) {
			return samples;
		}

		float[] result = new float[targetLength];
		int copyLength = Math.min(samples.length, targetLength);
		System.arraycopy(samples, 0, result, 0, copyLength);
		return result;
	}
}
