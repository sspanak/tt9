package io.github.sspanak.tt9.db.entities;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.github.sspanak.tt9.Logger;

public class WordFile {
	private static final String LOG_TAG = WordFile.class.getSimpleName();
	private final AssetManager assets;
	private final String name;
	private long timestamp = -1;
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
			totalLines = (int) getProperty("size");
		}

		return totalLines;
	}

	public long getTimestamp() {
		if (timestamp < 0) {
			timestamp = getProperty("timestamp");
		}

		return timestamp;
	}

	private long getProperty(String propertyName) {
		String propertyFilename = name + "." + propertyName;

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(propertyFilename)))) {
			return Long.parseLong(reader.readLine());
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Could not read the '" + propertyName + "' property of: " + name + " from: " + propertyFilename + ". " + e.getMessage());
			return 0;
		}
	}
}
