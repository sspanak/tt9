package io.github.sspanak.tt9.ui.main;

import android.graphics.Insets;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.util.ThemedContextBuilder;

abstract class BaseMainLayout {
	protected final TraditionalT9 tt9;
	private final int xml;

	protected View view = null;
	@NonNull protected final ArrayList<SoftKey> keys = new ArrayList<>();


	BaseMainLayout(TraditionalT9 tt9, int xml) {
		this.tt9 = tt9;
		this.xml = xml;
	}


	/**
	 * getKeys
	 * Returns a list of all the usable Soft Keys. Useful for attaching click handlers and changing
	 * the color theme.
	 */
	@NonNull protected ArrayList<SoftKey> getKeys() { return keys; }


	protected View getView() {
		if (view == null) {
			ContextThemeWrapper themedContext = new ThemedContextBuilder()
				.setConfiguration(tt9.getResources().getConfiguration())
				.setContext(tt9)
				.setSettings(tt9.getSettings())
				.setTheme(R.style.TTheme)
				.build();
			view = View.inflate(themedContext, xml, null);
			view.setOnApplyWindowInsetsListener(this::onApplyInsets);
		}

		return view;
	}


	protected WindowInsets onApplyInsets(@NonNull View v, @NonNull WindowInsets windowInsets) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
			return preventEdgeToEdge(v, windowInsets);
		} else {
			return windowInsets;
		}
	}


	/**
	 * Apply the padding to prevent edge-to-edge on Android 15+. Without padding,
	 * the bottom of the View will be cut off by the system navigation bar.
	 */
	@RequiresApi(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
	protected WindowInsets preventEdgeToEdge(@NonNull View v, @NonNull WindowInsets windowInsets) {
		Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
		ViewGroup.MarginLayoutParams layout = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
		if (layout == null) {
			return windowInsets;
		}

		layout.rightMargin = insets.right;
		layout.bottomMargin = insets.bottom;
		layout.leftMargin = insets.left;
		v.setLayoutParams(layout);
		return WindowInsets.CONSUMED;
	}


	void requestPreventEdgeToEdge() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM && view != null) {
			view.requestApplyInsets();
		}
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


	boolean setHeight(int height) {
		if (view == null) {
			return false;
		}

		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (params == null) {
			return false;
		}

		params.height = height;
		view.setLayoutParams(params);
		return true;
	}


	/**
	 * Adjusts the width of the keyboard to the given percentage of the screen width.
	 */
	private void setKeyboardWidth(int widthPercent) {
		View keyboard = view != null ? view.findViewById(R.id.keyboard_container) : null;
		if (keyboard == null) {
			return;
		}

		LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) keyboard.getLayoutParams();
		if (layout != null) {
			layout.weight = widthPercent;
			keyboard.setLayoutParams(layout);
		}
	}


	/**
	 * Adjust the padding on both sides of the keyboard to make it centered, or aligned to the
	 * left or right.
	 */
	private void setBumperWidth(int widthPercent, int gravity) {
		View leftBumper = view.findViewById(R.id.bumper_left);
		View rightBumper = view.findViewById(R.id.bumper_right);
		if (leftBumper == null || rightBumper == null) {
			return;
		}

		int leftPadding = 0;
		int rightPadding = 0;

		switch (gravity) {
			case Gravity.CENTER_HORIZONTAL:
				leftPadding = rightPadding = (100 - widthPercent) / 2;
				break;

			case Gravity.START:
				rightPadding = 100 - widthPercent;
				break;

			case Gravity.END:
				leftPadding = 100 - widthPercent;
				break;
		}

		LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) leftBumper.getLayoutParams();
		if (layout != null) {
			layout.weight = leftPadding;
			leftBumper.setLayoutParams(layout);
		}

		layout = (LinearLayout.LayoutParams) rightBumper.getLayoutParams();
		if (layout != null) {
			layout.weight = rightPadding;
			rightBumper.setLayoutParams(layout);
		}
	}


	void setWidth(int widthPercent, int gravity) {
		if (view == null || widthPercent <= 0 || widthPercent > 100) {
			return;
		}

		setBumperWidth(widthPercent, gravity);
		setKeyboardWidth(widthPercent);
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
