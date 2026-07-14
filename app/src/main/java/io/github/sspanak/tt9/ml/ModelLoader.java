package io.github.sspanak.tt9.ml;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import io.github.sspanak.tt9.util.Logger;

/**
 * Utilities for loading and downloading Whisper models.
 * All models are stored in filesDir — nothing is bundled as an asset.
 */
public class ModelLoader {
	private static final String LOG_TAG = "ModelLoader";
	private static final String FUTO_MODEL_BASE_URL = "https://voiceinput.futo.org/VoiceInput/";

	public interface DownloadProgressCallback {
		void onProgress(int bytesDownloaded, int totalBytes);
		void onComplete();
		void onError(Exception e);
	}

	/**
	 * Load a model from filesDir into a MappedByteBuffer for inference.
	 */
	public static MappedByteBuffer loadModel(Context context, ModelData model) throws IOException {
		File file = new File(context.getFilesDir(), model.ggmlFile);
		if (!file.exists()) {
			throw new IOException("Model file not found: " + model.ggmlFile);
		}
		FileInputStream inputStream = new FileInputStream(file);
		FileChannel fileChannel = inputStream.getChannel();
		MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
		buffer.load();
		fileChannel.close();
		inputStream.close();
		return buffer;
	}

	/**
	 * Returns true if the model file already exists in filesDir.
	 */
	public static boolean modelExists(Context context, ModelData model) {
		return new File(context.getFilesDir(), model.ggmlFile).exists();
	}

	/**
	 * Download a model from FUTO's server to filesDir.
	 * Runs synchronously — call from a background thread.
	 */
	public static void downloadModel(Context context, ModelData model, DownloadProgressCallback callback) {
		File outputFile = new File(context.getFilesDir(), model.ggmlFile);
		if (outputFile.exists()) {
			Logger.d(LOG_TAG, "Model already exists: " + model.ggmlFile);
			if (callback != null) callback.onComplete();
			return;
		}

		File tempFile = new File(context.getCacheDir(), model.ggmlFile + ".download");
		HttpURLConnection connection = null;

		try {
			String downloadUrl = FUTO_MODEL_BASE_URL + model.ggmlFile;
			Logger.d(LOG_TAG, "Downloading from: " + downloadUrl);

			URL url = new URL(downloadUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP error: " + connection.getResponseCode());
			}

			int fileSize = connection.getContentLength();
			InputStream input = connection.getInputStream();
			FileOutputStream output = new FileOutputStream(tempFile);

			byte[] buf = new byte[8192];
			int bytesRead;
			int totalRead = 0;
			int lastPercent = 0;

			while ((bytesRead = input.read(buf)) != -1) {
				output.write(buf, 0, bytesRead);
				totalRead += bytesRead;
				if (fileSize > 0 && callback != null) {
					int percent = (int) ((totalRead * 100L) / fileSize);
					if (percent >= lastPercent + 10) {
						lastPercent = percent;
						callback.onProgress(totalRead, fileSize);
					}
				}
			}

			output.flush();
			output.close();
			input.close();

			// Atomic rename from temp to final
			if (!tempFile.renameTo(outputFile)) {
				throw new IOException("Failed to rename temp file to: " + outputFile.getName());
			}

			Logger.d(LOG_TAG, "Model downloaded successfully: " + model.ggmlFile);
			if (callback != null) callback.onComplete();

		} catch (Exception e) {
			Logger.e(LOG_TAG, "Download failed: " + e.getMessage());
			tempFile.delete();
			if (callback != null) callback.onError(e);
		} finally {
			if (connection != null) connection.disconnect();
		}
	}
}
