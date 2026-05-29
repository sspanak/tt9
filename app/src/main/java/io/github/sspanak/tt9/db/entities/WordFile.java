package io.github.sspanak.tt9.db.entities;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.RemoteAssetFile;

public class WordFile extends RemoteAssetFile {
	private static final String LOG_TAG = WordFile.class.getSimpleName();

	private final boolean hasSyllables;

	private int lastCharCode;

	private String hash = null;
	private int words = -1;
	private long size = -1;


	public WordFile(@NonNull Context context, Language language, AssetManager assets) {
		super(context, assets, language != null ? language.getDictionaryFile() : "");
		hasSyllables = language != null && language.isTranscribed();

		lastCharCode = 0;
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


	@Override
	protected void parseProperties(String rawProperty, String rawValue) {
		super.parseProperties(rawProperty, rawValue);

		setHash(rawProperty, rawValue);
		setSize(rawProperty, rawValue);
		setWords(rawProperty, rawValue);
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
			return String
				.format(Locale.ROOT, "%1.2fM %s", getWords() / 1000000.0, suffix)
				.replace("0M ", "M ");
		} else if (getWords() > 1000) {
			return getWords() / 1000 + "k " + suffix;
		} else {
			return getWords() + " " + suffix;
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
		if (getSize() >= 1_000_000) {
			final float sizeMB = getSize() / 1_000_000f;
			return String.format(Locale.ROOT, "%1.2f MB", sizeMB)
				.replace("0 ", " ")
				.replace(".0 ", " ");
		} else {
			return Math.round(getSize() / 1000f) + " KB";
		}
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
