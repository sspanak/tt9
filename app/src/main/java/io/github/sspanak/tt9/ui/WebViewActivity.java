package io.github.sspanak.tt9.ui;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

abstract public class WebViewActivity extends EdgeToEdgeActivity implements View.OnAttachStateChangeListener {
	private WebView container;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		buildLayout();
	}

	@Override
	public void onViewAttachedToWindow(@NonNull View view) {
		preventEdgeToEdge((View) view.getParent());
	}

	@Override
	public void onViewDetachedFromWindow(@NonNull View view) {}

	@Override
	protected void onDestroy() {
		container.removeOnAttachStateChangeListener(this);
		super.onDestroy();
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
		container = new WebView(this);
		container.addOnAttachStateChangeListener(this);

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
