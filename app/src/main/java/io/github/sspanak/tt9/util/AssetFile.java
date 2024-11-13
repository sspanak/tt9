package io.github.sspanak.tt9.util;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AssetFile {
	protected final AssetManager assets;
	protected final String path;

	public AssetFile(@NonNull AssetManager assets, String path) {
		this.assets = assets;
		this.path = path;
	}

	public boolean exists() {
		try {
			assets.open(path).close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getStream(), StandardCharsets.UTF_8));
	}

	protected InputStream getStream() throws IOException {
		return assets.open(path);
	}
}
