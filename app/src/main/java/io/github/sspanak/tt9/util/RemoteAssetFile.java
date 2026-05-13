package io.github.sspanak.tt9.util;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

abstract public class RemoteAssetFile extends AssetFile {
	private static final String LOG_TAG = RemoteAssetFile.class.getSimpleName();

	private final Context context;
	protected BufferedReader reader;

	private String downloadUrl = null;

	public RemoteAssetFile(@NonNull Context context, @NonNull AssetManager assets, String path) {
		super(assets, path);
		this.context = context;
		reader = null;
	}


	private InputStream getRemoteStream() throws IOException {
		final String url = getDownloadUrl();
		if (url == null || url.isEmpty()) {
			throw new IOException("Missing download URL for file: " + path);
		}

		try {
			URLConnection connection = new URL(getDownloadUrl()).openConnection();
			connection.setConnectTimeout(SettingsStore.DICTIONARY_DOWNLOAD_CONNECTION_TIMEOUT);
			connection.setReadTimeout(SettingsStore.DICTIONARY_DOWNLOAD_READ_TIMEOUT);
			return connection.getInputStream();
		} catch (MalformedURLException e) {
			throw new IOException("Malformed download URL: " + url, e);
		}
	}


	public BufferedReader getReader() throws IOException {
		if (reader != null) {
			return reader;
		}

		InputStream stream = exists() ? getStream() : getRemoteStream();
		ZipInputStream zipStream = new ZipInputStream(stream);
		ZipEntry entry = zipStream.getNextEntry();
		if (entry == null) {
			zipStream.close();
			throw new IOException("ZIP file: " + path + " is empty.");
		}
		return reader = new BufferedReader(new InputStreamReader(zipStream, StandardCharsets.UTF_8));
	}


	protected String getDownloadUrl() {
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


	@CallSuper
	protected void parseProperties(String rawProperty, String rawValue) {
		setDownloadUrl(rawProperty, rawValue);
	}


	protected void loadProperties() {
		String propertyFilename = path.replaceFirst("\\.\\w+$", "") + ".props.yml";

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(propertyFilename), StandardCharsets.UTF_8))) {
			for (String line; (line = reader.readLine()) != null; ) {
				String[] parts = line.split("\\s*:\\s*");
				if (parts.length < 2) {
					continue;
				}

				parseProperties(parts[0], parts[1]);
			}
		} catch (Exception e) {
			Logger.w(LOG_TAG, "Could not read the property file: " + propertyFilename + ". " + e.getMessage());
		}
	}
}
