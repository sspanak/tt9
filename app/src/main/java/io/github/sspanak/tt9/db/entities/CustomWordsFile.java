package io.github.sspanak.tt9.db.entities;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class CustomWordsFile {
	public static final String MIME_TYPE = "text/*";
	private static final Pattern VALID_LINE_PATTERN = Pattern.compile("^\\p{L}+\\t\\d+$");

	private final ContentResolver contentResolver;
	private final Uri fileUri;

	public CustomWordsFile(Uri fileUri, @NonNull ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
		this.fileUri = fileUri;
	}

	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(contentResolver.openInputStream(fileUri)));
	}

	public boolean exists() {
		try {
			return getReader() != null;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean isLineValid(String line) {
		return line != null && !line.isEmpty() && VALID_LINE_PATTERN.matcher(line).matches();
	}
}
