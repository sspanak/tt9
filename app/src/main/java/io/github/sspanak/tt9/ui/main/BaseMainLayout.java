package io.github.sspanak.tt9.ui.main;

import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashSet;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.util.ThemedContextBuilder;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract class BaseMainLayout {
	protected int e2ePaddingBottomLandscape = -1;
	protected int e2ePaddingBottomPortrait = -1;

	protected final TraditionalT9 tt9;
	private final int xml;

	protected View view = null;
	@NonNull protected final ArrayList<SoftKey> keys = new ArrayList<>();
	@NonNull protected final HashSet<Integer> dynamicKeys = new HashSet<>();


	BaseMainLayout(TraditionalT9 tt9, int xml) {
		this.tt9 = tt9;
		this.xml = xml;
	}


	protected void addKey(int keyId, @Nullable ViewGroup container) {
		View source = container != null ? container : view;
		if (source == null) {
			return;
		}

		SoftKey key = source.findViewById(keyId);
		if (key != null) {
			keys.add(key);
		}
	}


	protected void addKey(int keyId) {
		addKey(keyId, null);
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
		if (DeviceInfo.AT_LEAST_ANDROID_15) {
			preventEdgeToEdge(v, windowInsets);
			return WindowInsets.CONSUMED;
		} else {
			return windowInsets;
		}
	}


	/**
	 * Apply the padding to prevent edge-to-edge on Android 15+. Without padding,
	 * the bottom of the View will be cut off by the system navigation bar.
	 */
	@RequiresApi(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
	protected void preventEdgeToEdge(@NonNull View v, @NonNull WindowInsets windowInsets) {
		Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
		v.setPadding(insets.left, 0, insets.right, insets.bottom);

		// cache the padding for use when the insets are not available
		if (e2ePaddingBottomLandscape < 0 || e2ePaddingBottomPortrait < 0) {
			boolean isLandscape = DeviceInfo.isLandscapeOrientation(view.getContext());
			if (isLandscape) {
				e2ePaddingBottomLandscape = insets.bottom;
			} else {
				e2ePaddingBottomPortrait = insets.bottom;
			}
		}
	}


	/**
	 * Similar to the above method, but reuses the last known padding. Useful for when the Main View
	 * is re-created and it is not yet possible to get the new window insets.
 	 */
	public void preventEdgeToEdge() {
		if (tt9 == null || view == null || !DeviceInfo.AT_LEAST_ANDROID_15) {
			return;
		}

		boolean isLandscape = DeviceInfo.isLandscapeOrientation(view.getContext());

		int bottomPadding = isLandscape ? e2ePaddingBottomLandscape : e2ePaddingBottomPortrait;
		bottomPadding = bottomPadding < 0 ? DeviceInfo.getNavigationBarHeight(view.getContext(), tt9.getSettings(), isLandscape) : bottomPadding;
		view.setPadding(view.getPaddingLeft(), 0, view.getPaddingRight(), bottomPadding);
	}


	void requestPreventEdgeToEdge() {
		if (view != null && DeviceInfo.AT_LEAST_ANDROID_15) {
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


	int getKeyboardHeight() {
		View keyboard = view != null ? view.findViewById(R.id.keyboard_container) : null;
		return keyboard != null ? keyboard.getMeasuredHeight() : 0;
	}


	boolean setKeyboardHeight(int height) {
		View keyboard = view != null ? view.findViewById(R.id.keyboard_container) : null;
		if (keyboard == null) {
			return false;
		}

		ViewGroup.LayoutParams params = keyboard.getLayoutParams();
		if (params == null) {
			return false;
		}

		params.height = height;
		keyboard.setLayoutParams(params);
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


	private boolean shouldEnableBackgroundBlending() {
		if (view == null || tt9 == null) {
			return true;
		}

		boolean isLandscape = DeviceInfo.isLandscapeOrientation(view.getContext());
		int width = tt9.getSettings().getWidthPercent();

		return
			DeviceInfo.AT_LEAST_ANDROID_15
			&& ((isLandscape && width >= 75) || (!isLandscape && width >= 65));
	}


	protected void setBackgroundBlending() {
		if (view == null) {
			return;
		}

		boolean yes = shouldEnableBackgroundBlending();

		view.setBackgroundColor(
				yes ? view.getContext().getResources().getColor(R.color.keyboard_background) : Color.TRANSPARENT
		);

		final int separatorVisibility = yes ? View.VISIBLE : View.GONE;

		View leftBumperTopSeparator = view.findViewById(R.id.bumper_left_top_separator);
		if (leftBumperTopSeparator != null) {
			leftBumperTopSeparator.setVisibility(separatorVisibility);
		}

		View rightBumperTopSeparator = view.findViewById(R.id.bumper_right_top_separator);
		if (rightBumperTopSeparator != null) {
			rightBumperTopSeparator.setVisibility(separatorVisibility);
		}
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

	/**
	 * Render specific keys to update their state. If the list is empty, no keys are rendered. If the
	 * list is null, all keys are rendered.
	 */
	private void renderKeys(@Nullable HashSet<Integer> keyIds) {
		if (keyIds != null && keyIds.isEmpty()) {
			return;
		}

		for (SoftKey key : getKeys()) {
			if (keyIds == null || keyIds.contains(key.getId())) {
				key.render();
			}
		}
	}

	void renderKeys() {
		renderKeys(null);
	}

	void renderDynamicKeys() {
		renderKeys(dynamicKeys);
	}
}
