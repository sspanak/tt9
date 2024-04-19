package io.github.sspanak.tt9.db.exporter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import io.github.sspanak.tt9.util.ConsumerCompat;
import io.github.sspanak.tt9.util.Permissions;

public abstract class AbstractExporter {
	protected static String FILE_EXTENSION = ".csv";
	protected static String MIME_TYPE = "text/csv";

	protected Runnable failureHandler;
	protected Runnable startHandler;
	protected ConsumerCompat<String> successHandler;
	private Thread processThread;
	private String outputFile;
	private String statusMessage = "";


	private void writeAndroid10(Activity activity) throws Exception {
		final String fileName = generateFileName();
		outputFile = getOutputDir() + File.pathSeparator + fileName;

		final ContentValues file = new ContentValues();
		file.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
		file.put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE);
		file.put(MediaStore.MediaColumns.RELATIVE_PATH, getOutputDir());

		final ContentResolver resolver = activity.getContentResolver();
		Uri uri = null;

		try {
			uri = resolver.insert(MediaStore.Files.getContentUri("external"), file);
			if (uri == null) {
				throw new IOException("Failed to create new MediaStore entry.");
			}

			try (OutputStream stream = resolver.openOutputStream(uri)) {
				if (stream == null) {
					throw new IOException("Failed to open output stream.");
				}
				stream.write(getFileContents(activity));
			}
		} catch (IOException e) {
			if (uri != null) {
				resolver.delete(uri, null, null);
			}

			throw e;
		}
	}


	protected void writeLegacy(Activity activity) throws Exception {
		Permissions permissions = new Permissions(activity);
		if (permissions.noWriteStorage()) {
			permissions.requestWriteStorage();
		}

		final String exportDir = Environment.getExternalStoragePublicDirectory(getOutputDir()).getAbsolutePath();
		final String fileName = generateFileName();
		outputFile = getOutputDir() + File.pathSeparator + fileName;

		final File file = new File(exportDir, fileName);
		if (!file.createNewFile()) {
			throw new IOException("Failed to create a new file.");
		}

		try (OutputStream stream = new FileOutputStream(file)) {
			stream.write(getFileContents(activity));
		}

		MediaScannerConnection.scanFile(activity, new String[]{file.getAbsolutePath()}, new String[]{MIME_TYPE}, null);
	}


	protected void write(Activity activity) throws Exception {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			writeAndroid10(activity);
		} else {
			writeLegacy(activity);
		}
	}


	protected String getOutputFile() {
		return outputFile;
	}


	public String getOutputDir() {
		// on some older phones, files may not be visible in the DOCUMENTS directory, so we use DOWNLOADS
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? Environment.DIRECTORY_DOCUMENTS : Environment.DIRECTORY_DOWNLOADS;
	}

	protected void sendFailure() {
		if (failureHandler != null) {
			failureHandler.run();
		}
	}

	protected void sendStart(@NonNull String message) {
		if (startHandler != null) {
			statusMessage = message;
			startHandler.run();
		}
	}

	protected void sendSuccess() {
		if (successHandler != null) {
			successHandler.accept(outputFile);
		}
	}

	public boolean export(@NonNull Activity activity) {
		if (isRunning()) {
			return false;
		}

		processThread = new Thread(() -> exportSync(activity));
		processThread.start();

		return true;
	}

	public boolean isRunning() {
		return processThread != null && processThread.isAlive();
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setFailureHandler(Runnable handler) {
		failureHandler = handler;
	}

	public void setStartHandler(Runnable handler) {
		startHandler = handler;
	}

	public void setSuccessHandler(ConsumerCompat<String> handler) {
		successHandler = handler;
	}


	abstract protected void exportSync(Activity activity);
	@NonNull abstract protected String generateFileName();
	@NonNull abstract protected byte[] getFileContents(Activity activity) throws Exception;
}
