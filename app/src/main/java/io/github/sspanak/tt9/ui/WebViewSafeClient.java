package io.github.sspanak.tt9.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.Clipboard;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class WebViewSafeClient extends WebViewClient {
	private final Activity activity;

	public WebViewSafeClient(@NonNull Activity activity) {
		super();
		this.activity = activity;
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
		final Uri uri = request.getUrl();

		if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme())) {
			return super.shouldOverrideUrlLoading(view, request);
		}

		if (!openLink(uri)) {
			Clipboard.copy(activity, uri.toString());
			if (!DeviceInfo.AT_LEAST_ANDROID_13) {
				UI.toastShortSingle(activity, R.string.help_url_copied);
			}
		}

		return true;
	}

	private boolean openLink(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);

		try {
			activity.startActivity(intent);
			return true;
		} catch (Exception e) {
			Logger.d(getClass().getSimpleName(), "Failed opening URL: '" + uri + "'. " + e.getMessage());
			return false;
		}
	}
}
