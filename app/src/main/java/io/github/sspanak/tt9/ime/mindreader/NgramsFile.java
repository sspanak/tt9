package io.github.sspanak.tt9.ime.mindreader;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.RemoteAssetFile;

public class NgramsFile extends RemoteAssetFile {
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
			Logger.e(getClass().getSimpleName(), "Error reading N-grams file. " + e.getMessage());
			return new ArrayList<>();
		}
	}
}
