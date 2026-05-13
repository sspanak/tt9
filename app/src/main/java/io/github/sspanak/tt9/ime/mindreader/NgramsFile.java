package io.github.sspanak.tt9.ime.mindreader;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.RemoteAssetFile;

public class NgramsFile extends RemoteAssetFile {
	private static final String LOG_TAG = NgramsFile.class.getSimpleName();

	@Nullable private String revision = null;


	public NgramsFile(@NonNull Context context, @NonNull AssetManager assets, @NonNull Language language) {
		super(context, assets, language.getNgramsFile());
	}


	public ArrayList<String> getLines() {
		try (BufferedReader reader = getReader()) {
			final ArrayList<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} catch (IOException e) {
			Logger.i(LOG_TAG, "Skipping factory n-grams. " + e.getMessage());
			return new ArrayList<>();
		}
	}


	@NonNull
	public String getRevision() {
		if (revision == null) {
			loadProperties();
			if (revision == null) {
				revision = "";
			}
		}

		return revision;
	}


	@Override
	protected void parseProperties(String rawProperty, String rawValue) {
		super.parseProperties(rawProperty, rawValue);
		setRevision(rawProperty, rawValue);
	}


	private void setRevision(String rawProperty, String rawValue) {
		if (!rawProperty.equals("revision")) {
			return;
		}

		revision = rawValue == null || rawValue.isEmpty() ? "" : rawValue;

		if (revision.isEmpty()) {
			Logger.w(LOG_TAG, "Invalid 'revision' property of: " + path + ". Expecting a string, got: '" + rawValue + "'.");
		}
	}
}
