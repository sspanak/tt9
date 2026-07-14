package io.github.sspanak.tt9.ime.voice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ggml.DecodingMode;
import io.github.sspanak.tt9.ggml.WhisperGGML;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.ml.AudioProcessor;
import io.github.sspanak.tt9.ml.ModelData;
import io.github.sspanak.tt9.ml.ModelLoader;
import io.github.sspanak.tt9.ml.WhisperModels;
import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Logger;

/**
 * Offline voice input using FUTO Whisper
 * Completely replaces Android SpeechRecognizer with offline processing
 */
public class VoiceInputOps {
	private final static String LOG_TAG = VoiceInputOps.class.getSimpleName();

	private static final int SAMPLE_RATE = 16000;
	private static final int INITIAL_BUFFER_SIZE = 16000 * 60; // Start with 1 minute capacity

	@NonNull private final Context ims;
	@Nullable private Language language;
	private boolean isListening = false;
	private boolean isProcessing = false;
	private final AtomicBoolean isDownloading = new AtomicBoolean(false);

	private AudioRecord audioRecord;
	private WhisperGGML whisperModel;
	private ModelData currentModel; // Track which model is currently loaded
	private FloatBuffer audioSamples;
	private int samplesRecorded = 0;

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	private Future<?> recordingTask;
	private Future<?> processingTask;

	private final Handler mainHandler = new Handler(Looper.getMainLooper());

	@NonNull private final ConsumerCompat<String> onStopListening;
	@NonNull private final ConsumerCompat<String> onPartialResult;
	@NonNull private final ConsumerCompat<VoiceInputError> onListeningError;
	@Nullable private final Runnable onStartListening;


	public VoiceInputOps(
		@NonNull Context ims,
		@Nullable Runnable onStart,
		@Nullable ConsumerCompat<String> onStop,
		@Nullable ConsumerCompat<String> onPartial,
		@Nullable ConsumerCompat<VoiceInputError> onError
	) {
		this.ims = ims;
		this.onStartListening = onStart;
		this.onStopListening = onStop != null ? onStop : result -> {};
		this.onPartialResult = onPartial != null ? onPartial : result -> {};
		this.onListeningError = onError != null ? onError : error -> {};
		this.audioSamples = FloatBuffer.allocate(INITIAL_BUFFER_SIZE);
	}

	public boolean isAvailable() {
		return true; // mic button always shown; unsupported languages are caught in listen()
	}
	public boolean isListening() {
		return isListening || isProcessing;
	}

	public void listen(@Nullable Language language) {
		if (language == null) {
			Logger.e(LOG_TAG, "listen() called with NULL language");
			postError(new VoiceInputError(ims, VoiceInputError.ERROR_INVALID_LANGUAGE));
			return;
		}

		Logger.d(LOG_TAG, "listen() called with language: " + language.getName() + " (" + language.getLocale().getLanguage() + ")");

		if (isListening || isProcessing) {
			postError(new VoiceInputError(ims, VoiceInputError.ERROR_RECOGNIZER_BUSY));
			return;
		}

		if (ActivityCompat.checkSelfPermission(ims, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			postError(new VoiceInputError(ims, VoiceInputError.ERROR_NO_PERMISSION));
			return;
		}


		// Block languages with no Whisper support (Irish, Toki Pona, Tamazight)
		if (WhisperModels.isLanguageUnsupported(language.getLocale().getLanguage())) {
			Logger.w(LOG_TAG, "Language not supported for voice input: " + language.getName());
			postError(new VoiceInputError(ims, VoiceInputError.ERROR_INVALID_LANGUAGE));
			return;
		}
		this.language = language;

	// Load model asynchronously (may show download dialog)
	loadModelAsync(() -> {
		// Model loaded successfully, now start recording
		startRecording();
	});
}

	private void startRecording() {
		try {
			// Model should already be loaded by loadModelAsync
			int bufferSize = AudioRecord.getMinBufferSize(
				SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT
			);

			if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
				postError(new VoiceInputError(ims, VoiceInputError.ERROR_AUDIO_CAPTURE));
				return;
			}

			audioRecord = new AudioRecord(
				MediaRecorder.AudioSource.VOICE_RECOGNITION,
				SAMPLE_RATE,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				bufferSize * 4
			);

			if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
				postError(new VoiceInputError(ims, VoiceInputError.ERROR_AUDIO_CAPTURE));
				return;
			}

			audioSamples.clear();
			samplesRecorded = 0;
			isListening = true;

			audioRecord.startRecording();
			Logger.d(LOG_TAG, "Started recording audio for offline STT");

			if (onStartListening != null) {
				mainHandler.post(onStartListening);
			}

			// Start recording task
			recordingTask = executorService.submit(this::recordingLoop);

		} catch (SecurityException e) {
			Logger.e(LOG_TAG, "Permission denied: " + e.getMessage());
			postError(new VoiceInputError(ims, VoiceInputError.ERROR_NO_PERMISSION));
		} catch (Exception e) {
			Logger.e(LOG_TAG, "Failed to start recording: " + e.getMessage());
			postError(new VoiceInputError(ims, VoiceInputError.ERROR_AUDIO_CAPTURE));
		}
	}

	private void loadModelAsync(Runnable onComplete) {
		try {
			String langCode = language != null ? language.getLocale().getLanguage() : "en";
			String mappedLangCode = WhisperModels.mapLanguageCode(langCode);
			Logger.d(LOG_TAG, "Loading model for: " + (language != null ? language.getName() : "English") + " (mapped: " + mappedLangCode + ")");

			boolean isEnglish = "en".equals(mappedLangCode);
			ModelData model = isEnglish ? WhisperModels.ENGLISH_TINY : WhisperModels.MULTILINGUAL_TINY;
			Logger.d(LOG_TAG, "Selected model: " + model.name);

			// Reuse already-loaded correct model
			if (whisperModel != null && currentModel != null && currentModel.ggmlFile.equals(model.ggmlFile)) {
				Logger.d(LOG_TAG, "Correct model already loaded, reusing: " + model.name);
				onComplete.run();
				return;
			}

			// Close wrong model if loaded
			if (whisperModel != null) {
				Logger.w(LOG_TAG, "Switching model to: " + model.name);
				whisperModel.close();
				whisperModel = null;
				currentModel = null;
			}

			// Download model if not present
			if (!ModelLoader.modelExists(ims, model)) {
				Logger.w(LOG_TAG, "Model not found, starting download: " + model.ggmlFile);
				showToast("\uD83D\uDCE5 Downloading " + model.name + " (39MB)...", Toast.LENGTH_LONG);
				isDownloading.set(true);

				final ModelData finalModel = model;
				executorService.submit(() -> {
					try {
						ModelLoader.downloadModel(ims, finalModel, new ModelLoader.DownloadProgressCallback() {
							private int lastPercent = 0;

							@Override
							public void onProgress(int bytesDownloaded, int totalBytes) {
								int percent = (int) ((bytesDownloaded * 100L) / totalBytes);
								if (percent >= lastPercent + 10) {
									lastPercent = percent;
									showToast("\uD83D\uDCE5 Downloading: " + percent + "%", Toast.LENGTH_SHORT);
								}
							}

							@Override
							public void onComplete() {
								Logger.d(LOG_TAG, "Model downloaded: " + finalModel.name);
								showToast("\u2705 Voice model ready! Try again.", Toast.LENGTH_LONG);
								isDownloading.set(false);
								try {
									MappedByteBuffer modelBuffer = ModelLoader.loadModel(ims, finalModel);
									whisperModel = new WhisperGGML(modelBuffer, VoiceInputOps.this::onPartialResultFromModel);
									currentModel = finalModel;
									Logger.d(LOG_TAG, "Model loaded after download: " + finalModel.name);
									onComplete.run();
								} catch (IOException e) {
									Logger.e(LOG_TAG, "Failed to load model after download: " + e.getMessage());
									postError(new VoiceInputError(ims, VoiceInputError.ERROR_PROCESSING));
								}
							}

							@Override
							public void onError(Exception e) {
								Logger.e(LOG_TAG, "Download failed: " + e.getMessage());
								showToast("\u274C Download failed. Please try again.", Toast.LENGTH_LONG);
								isDownloading.set(false);
								postError(new VoiceInputError(ims, VoiceInputError.ERROR_PROCESSING));
							}
						});
					} catch (Exception e) {
						Logger.e(LOG_TAG, "Download exception: " + e.getMessage());
						showToast("\u274C Download failed.", Toast.LENGTH_SHORT);
						isDownloading.set(false);
						postError(new VoiceInputError(ims, VoiceInputError.ERROR_PROCESSING));
					}
				});
				return; // download callback handles completion
			}

			// Model already downloaded — load it
			Logger.d(LOG_TAG, "Loading model from files: " + model.name);
			MappedByteBuffer modelBuffer = ModelLoader.loadModel(ims, model);
			whisperModel = new WhisperGGML(modelBuffer, this::onPartialResultFromModel);
			currentModel = model;
			Logger.d(LOG_TAG, "Model loaded: " + model.name);
			onComplete.run();

		} catch (IOException e) {
			Logger.e(LOG_TAG, "Failed to load Whisper model: " + e.getMessage());
			postError(new VoiceInputError(ims, VoiceInputError.ERROR_PROCESSING));
		}
	}


private void showToast(String message, int duration) {
	mainHandler.post(() -> Toast.makeText(ims, message, duration).show());
}

private void onPartialResultFromModel(String text) {
	mainHandler.post(() -> onPartialResult.accept(text));
}

private void recordingLoop() {
	short[] buffer = new short[1600];

	try {
		while (isListening) {
			int samplesRead = audioRecord.read(buffer, 0, buffer.length);

			if (samplesRead > 0) {
				// Convert to float and append
				float[] floatSamples = AudioProcessor.shortToFloat(buffer);

				// Check if we need to expand the buffer
				if (samplesRecorded + samplesRead > audioSamples.capacity()) {
					// Double the buffer size
					int newCapacity = audioSamples.capacity() * 2;
					Logger.d(LOG_TAG, "Expanding audio buffer from " + audioSamples.capacity() + " to " + newCapacity);
					FloatBuffer newBuffer = FloatBuffer.allocate(newCapacity);
					audioSamples.flip();
					newBuffer.put(audioSamples);
					audioSamples = newBuffer;
				}

				// Append samples
				for (int i = 0; i < samplesRead; i++) {
					audioSamples.put(floatSamples[i]);
					samplesRecorded++;
				}
			}
		}

		// Recording stopped manually by user
		Logger.d(LOG_TAG, "Recording stopped by user");

	} catch (Exception e) {
		Logger.e(LOG_TAG, "Error during recording: " + e.getMessage());
		postError(new VoiceInputError(ims, VoiceInputError.ERROR_AUDIO_CAPTURE));
	}
}

	public void stop() {
		if (!isListening) {
			return;
		}

		isListening = false;
		Logger.d(LOG_TAG, "Stopping recording");

		// Stop recording
		if (audioRecord != null) {
			try {
				audioRecord.stop();
			} catch (Exception e) {
				Logger.e(LOG_TAG, "Error stopping AudioRecord: " + e.getMessage());
			}
		}

		// Cancel recording task
		if (recordingTask != null && !recordingTask.isDone()) {
			recordingTask.cancel(true);
		}

		// Process the recorded audio
		if (samplesRecorded > SAMPLE_RATE) { // At least 1 second of audio
			processAudio();
		} else {
			Logger.d(LOG_TAG, "Not enough audio recorded");
			cleanup();
			postResult(null);
		}
	}

	private void processAudio() {
		if (isProcessing) {
			return;
		}

		isProcessing = true;
		processingTask = executorService.submit(() -> {
			try {
				Logger.d(LOG_TAG, "Processing " + samplesRecorded + " samples with model: " + (currentModel != null ? currentModel.name : "unknown"));

				// Get samples array
				audioSamples.flip();
				float[] samples = new float[samplesRecorded];
				audioSamples.get(samples, 0, samplesRecorded);

				// Determine language
				String langCode = "en";
				if (language != null) {
					langCode = WhisperModels.mapLanguageCode(language.getLocale().getLanguage());
					Logger.d(LOG_TAG, "Processing audio for language: " + language.getName() + " (code: " + langCode + ")");
				}

				// Prepare language array
				String[] languages = new String[]{langCode};

				// Run inference
				Logger.d(LOG_TAG, "Running Whisper inference with language hint: " + langCode);
				String result = whisperModel.infer(
					samples,
					"", // No prompt
					languages,
					new String[]{}, // No bail languages
					DecodingMode.GREEDY.getValue(),
					true // Suppress non-speech tokens
				);

				Logger.d(LOG_TAG, "Recognition result: " + result);
				postResult(result);

			} catch (WhisperGGML.BailLanguageException e) {
				Logger.e(LOG_TAG, "Bail language exception: " + e.language);
				postResult("");
			} catch (Exception e) {
				Logger.e(LOG_TAG, "Error during processing: " + e.getMessage());
				e.printStackTrace();
				postError(new VoiceInputError(ims, VoiceInputError.ERROR_PROCESSING));
			} finally {
				cleanup();
			}
		});
	}

	private void cleanup() {
		isProcessing = false;
		isListening = false;

		if (audioRecord != null) {
			try {
				audioRecord.release();
			} catch (Exception e) {
				Logger.e(LOG_TAG, "Error releasing AudioRecord: " + e.getMessage());
			}
			audioRecord = null;
		}

		// Keep model loaded for faster next recognition (like FUTO does)
		// Model is only closed when switching between different models or on destroy()

		audioSamples.clear();
		samplesRecorded = 0;
	}

	public void destroy() {
		cleanup();

		// Close Whisper model
		if (whisperModel != null) {
			whisperModel.close();
			whisperModel = null;
			currentModel = null;
		}

		if (executorService != null && !executorService.isShutdown()) {
			executorService.shutdownNow();
		}
	}

	private void postResult(String result) {
		mainHandler.post(() -> onStopListening.accept(result));
	}

	private void postError(VoiceInputError error) {
		cleanup();
		mainHandler.post(() -> onListeningError.accept(error));
	}

	// Legacy compatibility methods
	public boolean enableOfflineMode(@NonNull Language language, boolean yes) {
		// Always offline, nothing to do
		return false;
	}

	public void enableOfflineMode() {
		// Always offline, nothing to do
	}

	// Legacy compatibility methods for SpeechRecognizerSupportModern
	public static String getLocale(@NonNull Language language) {
		return language.getLocale().toString();
	}

	public static android.content.Intent createIntent(String locale) {
		// No longer used with offline Whisper, return null
		return null;
	}

	@NonNull
	@Override
	public String toString() {
		return ims.getString(R.string.voice_input_listening);
	}
}
