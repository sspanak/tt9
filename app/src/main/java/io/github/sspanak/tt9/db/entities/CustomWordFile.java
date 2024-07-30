package io.github.sspanak.tt9.db.entities;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.NaturalLanguage;
import io.github.sspanak.tt9.util.Logger;

public class CustomWordFile {
	public static final String MIME_TYPE = "text/*"; // for some reason, text/csv does not work as a filter when browsing

	private final ContentResolver contentResolver;
	private final Uri fileUri;
	private int size = -1;

	public CustomWordFile(Uri fileUri, @NonNull ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
		this.fileUri = fileUri;
	}

	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(contentResolver.openInputStream(fileUri)));
	}

	public String getName() {
		return fileUri.getLastPathSegment();
	}

	public boolean exists() {
		try {
			return getReader() != null;
		} catch (IOException e) {
			return false;
		}
	}

	public int getSize() {
		if (size < 0) {
			calculateSize();
		}

		return size;
	}


	private void calculateSize() {
		size = 0;

		try {
			BufferedReader reader = getReader();
			while (reader.readLine() != null) {
				size++;
			}
		} catch (IOException e) {
			Logger.w(getClass().getSimpleName(), "Failed to read file size. " + e.getMessage());
		}
	}


	public static NaturalLanguage getLanguage(@NonNull Context context, String line) {
		if (line == null) {
			return null;
		}

		String[] parts = WordFile.splitLine(line);
		if (parts == null || parts.length < 2) {
			return null;
		}

		try {
			return LanguageCollection.getLanguage(context, Integer.parseInt(parts[1]));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@NonNull public static String getWord(String line) {
		String[] parts = WordFile.splitLine(line);
		return parts != null && parts.length > 0 ? parts[0] : "";
	}
}
