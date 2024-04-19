package io.github.sspanak.tt9.db.entities;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.github.sspanak.tt9.util.Logger;

public class WordFile {
	private static final String LOG_TAG = WordFile.class.getSimpleName();

	private final AssetManager assets;
	private final String name;
	private String hash = null;
	private int totalLines = -1;

	public WordFile(String name, AssetManager assets) {
		this.assets = assets;
		this.name = name;
	}

	public static String[] splitLine(String line) {
		String[] parts = { line, "" };

		// This is faster than String.split() by around 10%, so it's worth having it.
		// It runs very often, so any other optimizations are welcome.
		for (int i = 0 ; i < line.length(); i++) {
			if (line.charAt(i) == '	') { // the delimiter is TAB
				parts[0] = line.substring(0, i);
				parts[1] = i < line.length() - 1 ? line.substring(i + 1) : "";
				break;
			}
		}

		return parts;
	}

	public static short getFrequencyFromLineParts(String[] frequencyParts) {
		try {
			return Short.parseShort(frequencyParts[1]);
		} catch (Exception e) {
			return 0;
		}
	}

	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(assets.open(name), StandardCharsets.UTF_8));
	}

	public int getTotalLines() {
		if (totalLines < 0) {
			String rawTotalLines = getProperty("size");
			try {
				totalLines = Integer.parseInt(rawTotalLines);
			} catch (Exception e) {
				Logger.w(LOG_TAG, "Invalid 'size' property of: " + name + ". Expecting an integer, got: '" + rawTotalLines + "'.");
				totalLines = 0;
			}
		}

		return totalLines;
	}

	public String getHash() {
		if (hash == null) {
			hash = getProperty("hash");
		}

		return hash;
	}

	private String getProperty(String propertyName) {
		String propertyFilename = name + "." + propertyName;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(propertyFilename)))) {
			return reader.readLine();
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Could not read the '" + propertyName + "' property of: " + name + " from: " + propertyFilename + ". " + e.getMessage());
			return "";
		}
	}
}
