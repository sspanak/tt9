package io.github.sspanak.tt9.db.entities;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.github.sspanak.tt9.extensions.ChineseExtension;
import io.github.sspanak.tt9.extensions.ChineseExtensions;
import io.github.sspanak.tt9.languages.LanguageCollection;
import io.github.sspanak.tt9.languages.LanguageKind;
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
		return new BufferedReader(new InputStreamReader(contentResolver.openInputStream(fileUri), StandardCharsets.UTF_8));
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


	public static NaturalLanguage getLanguage(String line) {
		if (line == null) {
			return null;
		}

		String[] parts = line.split("\t", -1);
		if (parts == null || parts.length < 2) {
			return null;
		}

		return LanguageCollection.getLanguage(parts[1]);
	}

	public static CustomWord getCustomWord(String line) throws Exception {
		String[] parts = line.replaceFirst("^\\uFEFF", "").split("\t", -1);
		NaturalLanguage language = parts.length >= 2 ? LanguageCollection.getLanguage(parts[1]) : null;
		if (language != null && LanguageKind.isChinesePinyin(language)) {
			ChineseExtension extension = ChineseExtensions.get();
			if (extension == null) throw new IllegalArgumentException("Install the Chinese feature before importing Chinese words");
			if (parts.length == 3) return extension.parseUserWord(parts[0], parts[2]);
			if (parts.length == 2) {
				// The old raw spelling must pass the same syllable checks as the readable format.
				String[] expanded = extension.exportUserWord(parts[0]).split("\t", 3);
				return extension.parseUserWord(expanded[0], expanded[2]);
			}
			throw new IllegalArgumentException("Chinese entries require word, language ID and Pinyin");
		}
		if (parts.length != 2) throw new IllegalArgumentException("Expected word and language ID");
		return new CustomWord(parts[0], language);
	}

	@NonNull public static String getWord(String line) {
		String[] parts = WordFile.getLineData(line);
		return parts != null && parts.length > 0 ? parts[0] : "";
	}
}
