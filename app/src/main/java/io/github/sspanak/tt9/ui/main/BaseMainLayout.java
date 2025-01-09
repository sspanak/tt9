package io.github.sspanak.tt9.ui.main;

import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

abstract class BaseMainLayout {
	protected final TraditionalT9 tt9;
	private final int xml;

	protected View view = null;
	@NonNull protected final ArrayList<SoftKey> keys = new ArrayList<>();


	BaseMainLayout(TraditionalT9 tt9, int xml) {
		this.tt9 = tt9;
		this.xml = xml;
	}


	/** setDarkTheme
	 * <p>Changes the main view colors according to the theme.</p>
	 *
	 * <p>We need to do this manually, instead of relying on the Context to resolve the appropriate colors,
	 * because this View is part of the main service View. And service Views are always locked to the
	 * system context and theme.</p>
	 *
	 * <p>More info:
	 * <a href="https://stackoverflow.com/questions/72382886/system-applies-night-mode-to-views-added-in-service-type-application-overlay">...</a>
	 * </p>
	 */
	void setDarkTheme(boolean dark) {}


	/**
	 * getKeys
	 * Returns a list of all the usable Soft Keys. Useful for attaching click handlers and changing
	 * the color theme.
	 */
	@NonNull protected ArrayList<SoftKey> getKeys() { return keys; }


	protected View getView() {
		if (view == null) {
			// Adding the ContextThemeWrapper fixes this error log:
			// "View class SoftKeyXXX is an AppCompat widget that can only be used with a
			// Theme.AppCompat theme (or descendant)."
			ContextThemeWrapper themedCtx = new ContextThemeWrapper(tt9, R.style.TTheme);
			view = View.inflate(themedCtx, xml, null);
		}

		return view;
	}


	/**
	 * Calculate the bottom padding for the edge-to-edge mode in Android 15+. Without padding,
	 * the bottom of the View will be cut off by the system navigation bar.
	 */
	protected int getBottomInsetSize() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM || tt9 == null) {
			return 0;
		}

		final int DEFAULT_SIZE = 96;
		WindowInsets insets = tt9.getWindow().findViewById(android.R.id.content).getRootWindowInsets();
		return insets != null ? insets.getStableInsetBottom() : DEFAULT_SIZE;
	}


	protected void enableClickHandlers() {
		for (SoftKey key : getKeys()) {
			key.setTT9(tt9);
		}
	}


	@NonNull
	protected ArrayList<SoftKey> getKeysFromContainer(ViewGroup container) {
		ArrayList<SoftKey> keyList = new ArrayList<>();
		final int childrenCount = container != null ? container.getChildCount() : 0;

		for (int i = 0; i < childrenCount; i++) {
			View child = container.getChildAt(i);
			if (child instanceof SoftKey) {
				keyList.add((SoftKey) child);
			}
		}

		return keyList;
	}


	int getHeight(boolean forceRecalculate) {
		return 0;
	}


	abstract void showCommandPalette();
	abstract void hideCommandPalette();
	abstract boolean isCommandPaletteShown();
	abstract void showTextEditingPalette();
	abstract void hideTextEditingPalette();
	abstract boolean isTextEditingPaletteShown();


	/**
	 * render
	 * Do all the necessary stuff to display the View.
	 */
	abstract void render();
}
