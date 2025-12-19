package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyNumber2to9;
import io.github.sspanak.tt9.ui.main.keys.SoftKeySettings;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyTextLeft;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyTextRight;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class MainLayoutClassic extends MainLayoutExtraPanel {
	protected int height;
	protected boolean isCommandPaletteShown = false;
	protected boolean isTextEditingShown = false;


	MainLayoutClassic(TraditionalT9 tt9) {
		super(tt9, R.layout.main_classic);
	}


	protected MainLayoutClassic(TraditionalT9 tt9, int layoutResId) {
		super(tt9, layoutResId);
	}


	@Override
	void showCommandPalette() {
		super.showCommandPalette();
		togglePanel(R.id.main_soft_keys, true);
		toggleTextEditingColumns(false);
		toggleCommandPaletteColumns(true);
		renderKeys(false);
	}


	protected void toggleCommandPaletteColumns(boolean show) {
		isCommandPaletteShown = show;
	}


	@Override
	boolean isCommandPaletteShown() {
		return isCommandPaletteShown;
	}


	@Override
	void showKeyboard() {
		super.showKeyboard();
		togglePanel(R.id.main_soft_keys, true);
		toggleTextEditingColumns(false);
		toggleCommandPaletteColumns(false);
		renderKeys(false);
	}


	@Override
	void showTextEditingPalette() {
		super.showTextEditingPalette();
		togglePanel(R.id.main_soft_keys, true);
		toggleCommandPaletteColumns(false);
		toggleTextEditingColumns(true);
		renderKeys(false);
	}


	protected void toggleTextEditingColumns(boolean show) {
		isTextEditingShown = show;
	}


	@Override
	boolean isTextEditingPaletteShown() {
		return isTextEditingShown;
	}


	/**
	 * Uses the key height from the settings, but if the keyboard takes up too much screen space, it
	 * will be adjusted limited to 60% of the screen height in landscape mode and 75% in portrait mode.
	 * This prevents Android from auto-closing the keyboard in some apps that have a lot of content.
	 * Returns the adjusted height of a single key.
	 */
	protected int[] calculateKeyHeight() {
		final boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9);

		final int screenHeight = DeviceInfo.getScreenHeight(tt9.getApplicationContext());
		final double maxScreenHeight = isLandscape ? screenHeight * 0.6 : screenHeight * 0.75;
		final int maxKeyHeight = (int) Math.round(maxScreenHeight / 5);

		final int defaultHeight = Math.min(tt9.getSettings().getNumpadKeyHeight(), maxKeyHeight);
		final int textKeyHeight = getTextKeyHeight(defaultHeight);

		return new int[] {defaultHeight, textKeyHeight, textKeyHeight};
	}

	protected int getTextKeyHeight(int keyHeight) {
		return tt9.getSettings().isNumpadShapeV() ? Math.round(keyHeight * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_INNER) : keyHeight;
	}


	protected void setKeyHeight(int defaultHeight, int leftHeight, int rightHeight) {
		if (defaultHeight <= 0) {
			return;
		}

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeyTextLeft) {
				key.setHeight(leftHeight);
			} else if (key instanceof SoftKeyTextRight) {
				key.setHeight(rightHeight);
			} else {
				key.setHeight(defaultHeight);
			}
		}
	}


	protected int getKeyColumnHeight(int keyHeight) {
		return keyHeight * 4 + getTextKeyHeight(keyHeight);
	}


	@Override
	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();

			height =
				Math.round(resources.getDimension(R.dimen.numpad_status_bar_spacing_top))
				+ resources.getDimensionPixelSize(R.dimen.numpad_status_bar_spacing_bottom)
				+ resources.getDimensionPixelSize(R.dimen.numpad_suggestion_height)
				+ getKeyColumnHeight(calculateKeyHeight()[0])
				+ Math.round(resources.getDimension(R.dimen.numpad_keys_spacing_bottom));
		}

		return height;
	}


	protected void showLongSpace(boolean yes, int keyHeight) {
		LinearLayout longSpacePanel = view != null ? view.findViewById(R.id.panel_long_spacebar) : null;
		if (longSpacePanel != null) {
			longSpacePanel.setVisibility(yes ? LinearLayout.VISIBLE : LinearLayout.GONE);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) longSpacePanel.getLayoutParams();
			params.height = keyHeight;
			longSpacePanel.setLayoutParams(params);
		}
	}


	@Override
	protected void enableClickHandlers() {
		super.enableClickHandlers();

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeySettings) {
				((SoftKeySettings) key).setMainView(tt9.getMainView());
			}
		}
	}


	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (!keys.isEmpty() || view == null) {
			return keys;
		}

		addNumericKeys();

		// function keys
		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		addKey(R.id.soft_key_command_palette, table);
		addKey(R.id.soft_key_left_arrow, table);
		addKey(R.id.soft_key_numpad_ok, table);
		addKey(R.id.soft_key_right_arrow, table);
		addKey(R.id.soft_key_numpad_backspace, table);

		return keys;
	}


	protected void addNumericKeys() {
		if (view == null) {
			return;
		}

		// digits panel
		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		addKey(R.id.soft_key_0, table);
		addKey(R.id.soft_key_1, table);
		addKey(R.id.soft_key_2, table);
		addKey(R.id.soft_key_3, table);
		addKey(R.id.soft_key_4, table);
		addKey(R.id.soft_key_5, table);
		addKey(R.id.soft_key_6, table);
		addKey(R.id.soft_key_7, table);
		addKey(R.id.soft_key_8, table);
		addKey(R.id.soft_key_9, table);
		addKey(R.id.soft_key_text_1, table);
		addKey(R.id.soft_key_text_2, table);

		// Long space panel
		addKey(R.id.soft_key_200, table);
		addKey(R.id.soft_key_text_201, table);
		addKey(R.id.soft_key_text_202, table);
	}


	@Override
	void renderKeys(boolean onlyDynamic) {
		super.renderKeys(onlyDynamic);

		// toggle the long space row
		if (!tt9.getSettings().isNumpadShapeLongSpace() || tt9.isInputModeNumeric() || isFnPanelVisible() || (tt9.getLanguage() != null && tt9.getLanguage().hasLettersOnAllKeys())) {
			showLongSpace(false, 0);
			return;
		}

		// set the same height as other numeric keys
		int numericKeyHeight = 0;

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeyNumber2to9) {
				numericKeyHeight = key.getHeight();
				break;
			}
		}

		// or calculate it if no numeric keys are found (should not happen)
		if (numericKeyHeight <= 0) {
			numericKeyHeight = calculateKeyHeight()[0];
		}

		showLongSpace(true, numericKeyHeight);
	}


	/**
	 * Do layout-specific rendering work before the main rendering happens.
	 */
	protected void beforeRender() {
		if (view == null) {
			return;
		}

		final int fnKeyWeight = tt9.getSettings().getArrowsLeftRight() ? 2 : 1;

		final View backspace = view.findViewById(R.id.soft_key_numpad_backspace);
		if (backspace instanceof SoftKey) {
			((SoftKey) backspace).setWeight(fnKeyWeight);
		}

		final View commandPalette = view.findViewById(R.id.soft_key_command_palette);
		if (commandPalette instanceof SoftKey) {
			((SoftKey) commandPalette).setWeight(fnKeyWeight);
		}
	}


	@Override
	void render() {
		final int[] keyHeights = calculateKeyHeight();
		final boolean isPortrait = !DeviceInfo.isLandscapeOrientation(tt9);

		// @todo: add up/down swipe functions to OK + make it optional

		// @todo: add Shift and LF4 keys
		// @todo: make "!" and "?" optional

		// @todo: set Settings.UI.DEFAULT_LARGE_LAYOUT to CLASSIC when screen <= 5.5". https://stackoverflow.com/questions/35780980/getting-the-actual-screen-height-android

		getView();
		beforeRender();
		enableClickHandlers();
		setKeyHeight(keyHeights[0], keyHeights[1], keyHeights[2]);
		setPadding();
		setWidth(tt9.getSettings().getWidthPercent(isPortrait), tt9.getSettings().getAlignment());
		setBackgroundBlending();
		renderKeys(false);
	}
}
