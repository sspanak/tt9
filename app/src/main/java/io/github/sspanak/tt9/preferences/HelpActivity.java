package io.github.sspanak.tt9.preferences;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.Logger;

public class HelpActivity extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		buildLayout();
	}

	@Override
	public boolean onSupportNavigateUp() {
		finish();
		return true;
	}

	private void buildLayout() {
		enableBackButton();
		setContent();
	}

	private void enableBackButton() {
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayShowHomeEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	private void setContent() {
		WebView container = new WebView(this);

		// On API > 30 the WebView does not load the entire String with .loadData(),
		// so we need to do this weird shit.
		// Reference: https://developer.android.com/develop/ui/views/layout/webapps/webview
		String html = getHelpHtml();
		String encodedHtml = Base64.encodeToString(html.getBytes(), Base64.NO_PADDING);
		container.loadDataWithBaseURL(encodedHtml, html, "text/html", "UTF-8", null);

		setContentView(container);
	}

	private String getHelpHtml() {
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
