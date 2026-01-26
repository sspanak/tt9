package io.github.sspanak.tt9.ui;

import android.util.TypedValue;
import android.view.View;
import android.view.WindowInsets;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class EdgeToEdgeActivity extends AppCompatActivity {
	/**
	 * Adds padding to prevent the Preferences from going under the System Navigation Bar,
	 * the Status Bar, and other surrounding elements. Otherwise, they become hard to reach and see.
	 */
	public void preventEdgeToEdge(@Nullable View view) {
		if (view == null || !DeviceInfo.AT_LEAST_ANDROID_15) {
			return;
		}

		WindowInsets insets = getInsets(view);
		if (insets == null) {
			return;
		}

		view.setPadding(
			insets.getStableInsetLeft(),
			insets.getStableInsetTop() + getActionBarHeight(),
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
		WindowInsets newInsets = view != null ? view.getRootWindowInsets() : null;
		return newInsets == null ? getWindow().getDecorView().getRootWindowInsets() : newInsets;
	}


	/**
	 * Since com.google.android.material:material 1.13.0, the Title Bar appears over the Preferences.
	 * Here we calculate its height so we can add padding to the top of the Preferences list.
	 */
	private int getActionBarHeight() {
		int actionBarHeight = 0;
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true)) {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
		}

		return actionBarHeight;
	}
}
