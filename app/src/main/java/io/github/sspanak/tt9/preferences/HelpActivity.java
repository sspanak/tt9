package io.github.sspanak.tt9.preferences;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.github.sspanak.tt9.ui.WebViewActivity;
import io.github.sspanak.tt9.util.Logger;

public class HelpActivity extends WebViewActivity {
	@Override
	protected String getMimeType() {
		return "text/html";
	}

	@Override
	protected String getText() {
		AssetManager assets = getAssets();
		try {
			InputStream stream = assets.open("help.html");
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			return builder.toString();
		} catch (Exception e) {
			Logger.e(getClass().getSimpleName(), "Failed opening the help.html file.");
			return "";
		}
	}
}
