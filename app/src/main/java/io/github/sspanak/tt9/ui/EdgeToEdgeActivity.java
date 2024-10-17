package io.github.sspanak.tt9.ui;

import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

import androidx.appcompat.app.AppCompatActivity;


public class EdgeToEdgeActivity extends AppCompatActivity {
	public void preventEdgeToEdge(View view) {
		if (view == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			getWindow().setStatusBarContrastEnforced(true);
		}
	}

	private WindowInsets getInsets(View view) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return null;
		}

		WindowInsets newInsets = view != null ? view.getRootWindowInsets() : null;
		return newInsets == null ? getWindow().getDecorView().getRootWindowInsets() : newInsets;
	}
}
