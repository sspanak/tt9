package io.github.sspanak.tt9.ui;

import android.app.Activity;
import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.util.Clipboard;
import io.github.sspanak.tt9.util.Logger;

public class WebViewSafeClient extends WebViewClient {
	private final Activity activity;

	public WebViewSafeClient(@NonNull Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if (!url.startsWith("http")) {
			return super.shouldOverrideUrlLoading(view, url);
		}

		if (!DeviceInfo.AT_LEAST_ANDROID_10 || !shareLink(url)) {
			Clipboard.copy(activity, url);
			if (!DeviceInfo.AT_LEAST_ANDROID_13) {
				UI.toastShortSingle(activity, R.string.help_url_copied);
			}
		}

		return true;
	}

	private boolean shareLink(String url) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, url);

		try {
			activity.startActivity(Intent.createChooser(intent, "Share URL"));
			return true;
		} catch (Exception e) {
			Logger.d(getClass().getSimpleName(), "Failed sharing URL: '" + url + "'. " + e.getMessage());
			return false;
		}
	}
}
