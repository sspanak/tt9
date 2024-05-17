package io.github.sspanak.tt9.db.exporter;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.sspanak.tt9.util.Logger;

public class LogcatExporter extends AbstractExporter {
	private static LogcatExporter self;
	private final String BASE_FILE_NAME;
	private boolean includeSystemLogs;


	public LogcatExporter() {
		super();
		BASE_FILE_NAME = "tt9-logs-";
		FILE_EXTENSION = ".txt";
		MIME_TYPE = "text/plain";
	}


	public static LogcatExporter getInstance() {
		if (self == null) {
			self = new LogcatExporter();
		}
		return self;
	}


	@NonNull
	public static String getLogs(boolean includeSystemLogs) {
		StringBuilder log = new StringBuilder();
		try {
			Process process = Runtime.getRuntime().exec("logcat -d -v threadtime io.github.sspanak.tt9:D");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (includeSystemLogs || line.contains(Logger.TAG_PREFIX)) {
					log.append(line).append("\n");
				}
			}
		} catch (IOException e) {
			log.append("Error getting the logs. ").append(e.getMessage());
		}

		return log.toString();
	}


	public LogcatExporter setIncludeSystemLogs(boolean includeSystemLogs) {
		this.includeSystemLogs = includeSystemLogs;
		return this;
	}


	@Override
	protected void exportSync(Activity activity) {
		try {
			sendStart("Exporting logs...");
			write(activity);
			sendSuccess();
		} catch (Exception e) {
			sendFailure();
		}
	}


	@NonNull
	@Override
	protected String generateFileName() {
		return BASE_FILE_NAME + System.currentTimeMillis() + FILE_EXTENSION;
	}


	@NonNull
	@Override
	protected byte[] getFileContents(Activity activity) {
		return getLogs(includeSystemLogs).getBytes();
	}
}
