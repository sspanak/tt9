package io.github.sspanak.tt9.db.entities;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.AssetFile;
import io.github.sspanak.tt9.util.Logger;

public class WordFile extends AssetFile {
	private static final String LOG_TAG = WordFile.class.getSimpleName();

	private final Context context;
	private final boolean hasSyllables;

	private int lastCharCode;
	private BufferedReader reader;

	private String hash = null;
	private String downloadUrl = null;
	private int words = -1;
	private long size = -1;


	public WordFile(@NonNull Context context, Language language, AssetManager assets) {
		super(assets, language != null ? language.getDictionaryFile() : "");
		this.context = context;
		hasSyllables = language != null && language.isTranscribed();

		lastCharCode = 0;
		reader = null;
	}


	public static String[] getLineData(String line) {
		String[] parts = {line, ""};

		// This is faster than String.split() by around 10%, so it's worth having it.
		// It runs very often, so any other optimizations are welcome.
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == '	') { // the delimiter is TAB
				parts[0] = line.substring(0, i);
				parts[1] = i < line.length() - 1 ? line.substring(i + 1) : "";
				break;
			}
		}

		return parts;
	}


	public InputStream getRemoteStream() throws IOException {
		URLConnection connection = new URL(getDownloadUrl()).openConnection();
		connection.setConnectTimeout(SettingsStore.DICTIONARY_DOWNLOAD_CONNECTION_TIMEOUT);
		connection.setReadTimeout(SettingsStore.DICTIONARY_DOWNLOAD_READ_TIMEOUT);
		return connection.getInputStream();
	}


	public BufferedReader getReader() throws IOException {
		if (reader != null) {
			return reader;
		}

		InputStream stream = exists() ? getStream() : getRemoteStream();
		ZipInputStream zipStream = new ZipInputStream(stream);
		ZipEntry entry = zipStream.getNextEntry();
		if (entry == null) {
			throw new IOException("Dictionary ZIP file: " + path + " is empty.");
		}
		return reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8));
	}


	private String getDownloadUrl() {
		if (downloadUrl == null) {
			loadProperties();
		}

		return downloadUrl;
	}


	private void setDownloadUrl(String rawProperty, String rawValue) {
		if (!rawProperty.equals("revision")) {
			return;
		}

		downloadUrl = null;

		String revision = rawValue == null || rawValue.isEmpty() ? "" : rawValue;
		if (revision.isEmpty()) {
			Logger.w(LOG_TAG, "Invalid 'revision' property of: " + path + ". Expecting a string, got: '" + rawValue + "'.");
			return;
		}

		if (path == null || path.isEmpty()) {
			Logger.w(LOG_TAG, "Cannot generate a download URL for an empty path.");
			return;
		}

		downloadUrl = context.getString(R.string.dictionary_url, revision, new File(path).getName());
	}


	public String getHash() {
		if (hash == null) {
			loadProperties();
		}

		return hash;
	}


	private void setHash(String rawProperty, String rawValue) {
		if (!rawProperty.equals("hash")) {
			return;
		}

		hash = rawValue == null || rawValue.isEmpty() ? "" : rawValue;

		if (hash.isEmpty()) {
			Logger.w(LOG_TAG, "Invalid 'hash' property of: " + path + ". Expecting a string, got: '" + rawValue + "'.");
		}
	}


	public int getWords() {
		if (words < 0) {
			loadProperties();
		}

		return words;
	}


	public String getFormattedWords(String suffix) {
		if (getWords() > 1000000) {
			return String.format(Locale.ROOT, "%1.2fM %s", getWords() / 1000000.0, suffix);
		} else {
			return getWords() / 1000 + "k " + suffix;
		}
	}


	private void setWords(String rawProperty, String rawValue) {
		if (!rawProperty.equals("words")) {
			return;
		}

		try {
			words = Integer.parseInt(rawValue);
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Invalid 'words' property of: " + path + ". Expecting an integer, got: '" + rawValue + "'.");
			words = 0;
		}
	}


	public long getSize() {
		if (size < 0) {
			loadProperties();
		}

		return size;
	}


	public String getFormattedSize() {
		return String.format(Locale.ROOT, "%1.2f Mb", getSize() / 1048576.0);
	}


	private void setSize(String rawProperty, String rawValue) {
		if (!rawProperty.equals("size")) {
			return;
		}

		try {
			size = Long.parseLong(rawValue);
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Invalid 'size' property of: " + path + ". Expecting an integer, got: '" + rawValue + "'.");
			size = 0;
		}
	}


	private void loadProperties() {
		String propertyFilename = path.replaceFirst("\\.\\w+$", "") + ".props.yml";

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(propertyFilename)))) {
			for (String line; (line = reader.readLine()) != null; ) {
				String[] parts = line.split("\\s*:\\s*");
				if (parts.length < 2) {
					continue;
				}

				setDownloadUrl(parts[0], parts[1]);
				setHash(parts[0], parts[1]);
				setWords(parts[0], parts[1]);
				setSize(parts[0], parts[1]);
			}
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Could not read the property file: " + propertyFilename + ". " + e.getMessage());
		}
	}


	public String getNextSequence() throws IOException {
		if (reader == null || !notEOF()) {
			return "";
		}

		StringBuilder sequence = new StringBuilder();

		// use the last char from getNextWords() if it's a digit
		if (Character.isDigit(lastCharCode)) {
			sequence.append((char) lastCharCode);
		}

		while ((lastCharCode = reader.read()) != -1) {
			if (Character.isDigit(lastCharCode)) {
				sequence.append((char) lastCharCode);
			} else {
				break;
			}
		}

		if (sequence.length() == 0) {
			throw new IOException("Could not find next sequence. Unexpected end of file.");
		}

		return sequence.toString();
	}


	public ArrayList<String> getNextWords(String digitSequence) throws IOException {
		ArrayList<String> words = new ArrayList<>();

		if (reader == null || !notEOF()) {
			return words;
		}

		boolean areWordsSeparated = hasSyllables; // if the language chars are syllables, there is no leading space to hint word separation
		StringBuilder word = new StringBuilder();

		// If the word string starts with a space, it means there are words longer than the sequence.
		// We must make sure to extract them correctly.
		if (lastCharCode == ' ') {
			areWordsSeparated = true;
		}
		// use the last char from getNextSequence() if it's a letter
		else if (!Character.isDigit(lastCharCode)) {
			word.append((char) lastCharCode);
		}

		int sequenceLength = digitSequence.length();

		// start extracting the words
		int wordLength = word.length();
		while ((lastCharCode = reader.read()) != -1) {
			if (Character.isDigit(lastCharCode)) {
				break;
			}

			if (lastCharCode == ' ') {
				areWordsSeparated = true;
			} else {
				word.append((char) lastCharCode);
				wordLength++;
			}

			if ((areWordsSeparated && lastCharCode == ' ' && wordLength > 0) || (!areWordsSeparated && wordLength == sequenceLength)) {
				words.add(word.toString());
				wordLength = 0;
				word.setLength(wordLength);
			}
    }

		if ((areWordsSeparated && wordLength > 0) || (!areWordsSeparated && wordLength == sequenceLength)) {
			words.add(word.toString());
		} else if (wordLength > 0) {
			throw new IOException("Unexpected end of file. Word: '" + word + "' length (" + wordLength + ") differs from the length of sequence: " + digitSequence);
		}

		if (words.isEmpty()) {
			throw new IOException("Could not find any words for sequence: " + digitSequence);
		}

		return words;
	}


	public boolean notEOF() {
		return lastCharCode != -1;
	}
}
