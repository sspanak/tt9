package io.github.sspanak.tt9.ui;

import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

abstract public class WebViewActivity extends AppCompatActivity {
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
		// The "app:" prefix is mandatory, otherwise the anchor links do not work.
		// Reference: https://developer.android.com/develop/ui/views/layout/webapps/webview
		String text = getText();
		String encodedHtml = "app:" + Base64.encodeToString(text.getBytes(), Base64.NO_PADDING);
		container.loadDataWithBaseURL(encodedHtml, text, getMimeType(), "UTF-8", null);

		setContentView(container);
	}

	abstract protected String getText();
	abstract protected String getMimeType();
}
