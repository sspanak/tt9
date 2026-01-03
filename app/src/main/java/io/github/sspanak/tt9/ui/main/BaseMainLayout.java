package io.github.sspanak.tt9.ui.main;

import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ime.helpers.Key;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyAddWord;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyBackspace;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyCommandPalette;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyFilter;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyLF4;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyNumberNumpad;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyOk;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyRF3;
import io.github.sspanak.tt9.ui.main.keys.SoftKeySettings;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyShift;
import io.github.sspanak.tt9.util.ThemedContextBuilder;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract public class BaseMainLayout {
	private int lastLandscapeBottomInset = -1;

	protected final TraditionalT9 tt9;
	private final int xml;

	protected View view = null;
	@NonNull protected final ArrayList<SoftKey> keys = new ArrayList<>();


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
				.setTheme(R.style.TTheme)
				.build();
			view = View.inflate(themedContext, xml, null);
			view.setOnApplyWindowInsetsListener(this::onApplyInsets);
		}

		return view;
	}


	protected WindowInsets onApplyInsets(@NonNull View v, @NonNull WindowInsets windowInsets) {
		if (DeviceInfo.AT_LEAST_ANDROID_15) {
			setPadding(v, windowInsets);
			return WindowInsets.CONSUMED;
		} else {
			return windowInsets;
		}
	}


	/**
	 * Apply proper padding to prevent edge-to-edge on Android 15+. Without padding,
	 * the bottom of the View will be cut off by the system navigation bar.
	 */
	protected void setPadding(@NonNull View v, @NonNull WindowInsets windowInsets) {
		final WindowInsetsCompat insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(windowInsets);
		final Insets insets = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars());
		final boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9);

		int bottomPadding;

		if (tt9 == null) {
			bottomPadding = insets.bottom;
		} else if (isLandscape) {
			bottomPadding = lastLandscapeBottomInset = insets.bottom;
		} else {
			tt9.getSettings().setSamsungBottomPaddingPortrait(Math.round(insets.bottom / DeviceInfo.getScreenPixelDensity(view.getContext())));
			bottomPadding = tt9.getSettings().getBottomPaddingPortraitPx();
		}

		v.setPadding(insets.left, 0, insets.right, bottomPadding);
	}


	/**
	 * Similar to the above method, but reuses the last known padding. Useful for when the Main View
	 * is re-created and it is not yet possible to get the new window insets.
 	 */
	public void setPadding() {
		if (tt9 == null || view == null) {
			return;
		}

		int bottomPadding;

		if (DeviceInfo.isLandscapeOrientation(tt9)) {
			bottomPadding = lastLandscapeBottomInset >= 0 ? lastLandscapeBottomInset : view.getPaddingBottom();
		} else {
			bottomPadding = tt9.getSettings().getBottomPaddingPortraitPx();
		}

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


	public boolean shouldEnableBackgroundBlending() {
		if (view == null || tt9 == null) {
			return true;
		}

		boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9);
		int width = tt9.getSettings().getWidthPercent(!isLandscape);

		return
			DeviceInfo.AT_LEAST_ANDROID_15
			&& ((isLandscape && width >= 75) || (!isLandscape && width >= 65));
	}


	protected void setBackgroundBlending() {
		if (view == null) {
			return;
		}

		boolean yes = shouldEnableBackgroundBlending();

		// super wrapper of everything
		view.setBackgroundColor(yes ? tt9.getSettings().getKeyboardBackground() | 0xFF000000 : Color.TRANSPARENT);

		// top separator
		final int separatorVisibility = yes ? View.VISIBLE : View.GONE;

		// transparent space when the width < 100%
		View leftBumperTopSeparator = view.findViewById(R.id.bumper_left_top_separator);
		if (leftBumperTopSeparator != null) {
			leftBumperTopSeparator.setVisibility(separatorVisibility);
		}

		View rightBumperTopSeparator = view.findViewById(R.id.bumper_right_top_separator);
		if (rightBumperTopSeparator != null) {
			rightBumperTopSeparator.setVisibility(separatorVisibility);
		}

		// keys container
		View container = view.findViewById(R.id.keyboard_container);
		if (container != null) {
			container.setBackgroundColor(tt9.getSettings().getKeyboardBackground() | 0xFF000000);
		}
	}


	protected void togglePanel(int panelId, boolean show) {
		ViewGroup keysContainer = view != null && view.findViewById(panelId) != null ? view.findViewById(panelId) : null;
		if (keysContainer != null) {
			keysContainer.setVisibility(show ? ViewGroup.VISIBLE : ViewGroup.GONE);
		}
	}


	abstract void showCommandPalette();
	abstract void showKeyboard();
	abstract void showTextEditingPalette();
	abstract boolean isCommandPaletteShown();
	abstract boolean isTextEditingPaletteShown();


	/**
	 * Determines if any function panel is visible. This method can be overridden by plugin layouts
	 * to include their own panels.
	 */

	protected boolean isFnPanelVisible() {
		return isCommandPaletteShown() || isTextEditingPaletteShown();
	}


	/**
	 * render
	 * Do all the necessary stuff to display the View.
	 */
	abstract void render();


	/**
	 * Renders a visual click effect on the key that corresponds to the given keyCode, without
	 * performing any action.
	 */
	void renderClickFn(int keyCode) {
		if (tt9 == null || !tt9.getSettings().getHardwareKeyVisualFeedback()) {
			return;
		}

		final SettingsStore cfg = tt9.getSettings();

		for (SoftKey key : getKeys()) {
			final int keyId = key.getId();

			if (
				(key instanceof SoftKeyAddWord && keyCode == cfg.getKeyAddWord())
				|| (key instanceof SoftKeyBackspace && Key.isBackspace(cfg, keyCode))
				|| (key instanceof SoftKeyCommandPalette && keyCode == cfg.getKeyCommandPalette())
				|| (key instanceof SoftKeyLF4 && (keyCode == cfg.getKeyNextInputMode() || keyCode == cfg.getKeyNextLanguage()))
				|| (key instanceof SoftKeyFilter && (keyCode == cfg.getKeyFilterSuggestions() || keyCode == cfg.getKeyFilterClear()))
				|| (key instanceof SoftKeyOk && Key.isOK(keyCode))
				|| (key instanceof SoftKeySettings && keyCode == cfg.getKeyShowSettings())
				|| (key instanceof SoftKeyShift && keyCode == cfg.getKeyShift())
				|| (key instanceof SoftKeyRF3 && (keyCode == cfg.getKeyEditText() || keyCode == cfg.getKeyVoiceInput()))
				|| (keyId == R.id.soft_key_left_arrow && (Key.isArrowLeft(keyCode) || keyCode == cfg.getKeyPreviousSuggestion()))
				|| (keyId == R.id.soft_key_right_arrow && (Key.isArrowRight(keyCode) || keyCode == cfg.getKeyNextSuggestion()))
			) {
				key.renderClick();
				return;
			}
		}
	}


	/**
	 * Renders a visual click effect on given number key, without performing any action.
	 */
	void renderClickNumber(int number) {
		if (tt9 == null || !tt9.getSettings().getHardwareKeyVisualFeedback()) {
			return;
		}

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeyNumberNumpad && ((SoftKeyNumberNumpad) key).getNumber() == number) {
				key.renderClick();
				return;
			}
		}
	}


	/**
	 * Tells all layout keys to re-render themselves. If onlyDynamic is true, only keys that
	 * return true from isDynamic() will be re-rendered.
	 */
	void renderKeys(boolean onlyDynamic) {
		for (SoftKey key : getKeys()) {
			if (!onlyDynamic || key.isDynamic()) {
				key.render();
			}
		}
	}
}
