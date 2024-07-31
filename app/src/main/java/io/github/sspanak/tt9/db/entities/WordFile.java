package io.github.sspanak.tt9.db.entities;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class WordFile {
	private static final String LOG_TAG = WordFile.class.getSimpleName();

	private final AssetManager assets;
	private final Context context;
	private final String name;
	private String hash = null;
	private String downloadUrl = null;
	private int totalLines = -1;


	public WordFile(Context context, String name, AssetManager assets) {
		this.assets = assets;
		this.context = context;
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


	public boolean exists() {
		try {
			assets.open(name).close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}


	public InputStream getRemoteStream() throws IOException {
		URLConnection connection = new URL(getDownloadUrl()).openConnection();
		connection.setConnectTimeout(SettingsStore.DICTIONARY_DOWNLOAD_CONNECTION_TIMEOUT);
		connection.setReadTimeout(SettingsStore.DICTIONARY_DOWNLOAD_READ_TIMEOUT);
		return connection.getInputStream();
	}


	public BufferedReader getReader() throws IOException {
		InputStream stream = exists() ? assets.open(name) : getRemoteStream();
		return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
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

		String revision = rawValue == null || rawValue.isEmpty() ? "" : rawValue;
		downloadUrl = revision.isEmpty() ? null : context.getString(R.string.dictionary_url, revision, name);

		if (revision.isEmpty()) {
			Logger.w(LOG_TAG, "Invalid 'revision' property of: " + name + ". Expecting a string, got: '" + rawValue + "'.");
		}
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
			Logger.w(LOG_TAG, "Invalid 'hash' property of: " + name + ". Expecting a string, got: '" + rawValue + "'.");
		}
	}


	public int getTotalLines() {
		if (totalLines < 0) {
			loadProperties();
		}

		return totalLines;
	}


	private void setTotalLines(String rawProperty, String rawValue) {
		if (!rawProperty.equals("words")) {
			return;
		}

		try {
			totalLines = Integer.parseInt(rawValue);
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Invalid 'words' property of: " + name + ". Expecting an integer, got: '" + rawValue + "'.");
			totalLines = 0;
		}
	}


	private void loadProperties() {
		String propertyFilename = name + ".props.yml";

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(propertyFilename)))) {
			for (String line; (line = reader.readLine()) != null; ) {
				String[] parts = line.split("\\s*:\\s*");
				if (parts.length < 2) {
					continue;
				}

				setDownloadUrl(parts[0], parts[1]);
				setHash(parts[0], parts[1]);
				setTotalLines(parts[0], parts[1]);
			}
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Could not read the property file: " + propertyFilename + ". " + e.getMessage());
		}
	}
}
