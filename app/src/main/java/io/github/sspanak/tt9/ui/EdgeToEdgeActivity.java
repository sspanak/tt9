package io.github.sspanak.tt9.ui;

import android.view.View;
import android.view.WindowInsets;

import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.hacks.DeviceInfo;


public class EdgeToEdgeActivity extends AppCompatActivity {
	public void preventEdgeToEdge(View view) {
		if (view == null || !DeviceInfo.AT_LEAST_ANDROID_15) {
			return;
		}

		WindowInsets insets = getInsets(view);
		if (insets == null) {
			return;
		}

		view.setPadding(
			insets.getStableInsetLeft(),
			insets.getStableInsetTop(),
			insets.getStableInsetRight(),
			insets.getStableInsetBottom()
		);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		applyThemeToSystemUi();
	}

	private void applyThemeToSystemUi() {
		if (DeviceInfo.AT_LEAST_ANDROID_10) {
			getWindow().setStatusBarContrastEnforced(true);
		}
	}

	private WindowInsets getInsets(View view) {
		if (!DeviceInfo.AT_LEAST_ANDROID_6) {
			return null;
		}

		WindowInsets newInsets = view != null ? view.getRootWindowInsets() : null;
		return newInsets == null ? getWindow().getDecorView().getRootWindowInsets() : newInsets;
	}
}
