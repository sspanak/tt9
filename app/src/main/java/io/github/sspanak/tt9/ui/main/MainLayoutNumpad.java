package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.languages.LanguageKind;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeySettings;

class MainLayoutNumpad extends BaseMainLayout {
	private boolean isTextEditingShown = false;
	private int height;


	MainLayoutNumpad(TraditionalT9 tt9) {
		super(tt9, R.layout.main_numpad);
	}


	@Override void showCommandPalette() {}
	@Override void hideCommandPalette() {}
	@Override boolean isCommandPaletteShown() { return false; }


	@Override
	void showTextEditingPalette() {
		isTextEditingShown = true;

		view.findViewById(R.id.numpad_column_1).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_2).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_3).setVisibility(LinearLayout.GONE);

		view.findViewById(R.id.numpad_column_101).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_102).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_103).setVisibility(LinearLayout.VISIBLE);

		for (SoftKey key : getKeys()) {
			int keyId = key.getId();

			if (
				keyId == R.id.soft_key_add_word
				|| keyId == R.id.soft_key_filter
				|| keyId == R.id.soft_key_shift
				|| keyId == R.id.soft_key_rf3
				|| keyId == R.id.soft_key_lf4
				|| keyId == R.id.soft_key_0
				|| keyId == R.id.soft_key_100
				|| keyId == R.id.soft_key_punctuation_201
				|| keyId == R.id.soft_key_punctuation_202
			) {
				key.render();
			}
		}
	}

	@Override
	void hideTextEditingPalette() {
		isTextEditingShown = false;

		view.findViewById(R.id.numpad_column_1).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_2).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_3).setVisibility(LinearLayout.VISIBLE);

		view.findViewById(R.id.numpad_column_101).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_102).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_103).setVisibility(LinearLayout.GONE);

		for (SoftKey key : getKeys()) {
			int keyId = key.getId();

			if (
				keyId == R.id.soft_key_add_word
				|| keyId == R.id.soft_key_filter
				|| keyId == R.id.soft_key_shift
				|| keyId == R.id.soft_key_rf3
				|| keyId == R.id.soft_key_lf4
				|| keyId == R.id.soft_key_0
				|| keyId == R.id.soft_key_100
				|| keyId == R.id.soft_key_punctuation_201
				|| keyId == R.id.soft_key_punctuation_202
			) {
				key.render();
			}
		}
	}

	@Override
	boolean isTextEditingPaletteShown() {
		return isTextEditingShown;
	}


	/**
	 * Uses the key height from the settings, but if it takes up too much of the screen, it will
	 * be adjusted so that the entire Main View would take up around 50%  of the screen in landscape mode
	 * and 75% in portrait mode. Returns the adjusted height of a single key.
	 */
	private int calculateKeyHeight() {
		int keyHeight = tt9.getSettings().getNumpadKeyHeight();
		int screenHeight = DeviceInfo.getScreenHeight(tt9.getApplicationContext());

		boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9.getApplicationContext());
		double maxScreenHeight = isLandscape ? screenHeight * 0.75 : screenHeight * 0.8;
		double maxKeyHeight = isLandscape ? screenHeight * 0.115 : screenHeight * 0.125;

		// it's all very approximate but when it comes to screen dimensions,
		// accuracy is not that important
		return keyHeight * 5 > maxScreenHeight ? (int) Math.round(maxKeyHeight) : keyHeight;
	}


	private void setKeyHeight(int height) {
		if (view == null || height <= 0) {
			return;
		}

		for (SoftKey key : getKeys()) {
			key.setHeight(height);
		}
	}


	private int getKeyColumnHeight(int keyHeight) {
		int lastKeyHeight = tt9.getSettings().isNumpadShapeV() ? Math.round(keyHeight * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_OUTER) : keyHeight;
		return keyHeight * 3 + lastKeyHeight;
	}


	private void setKeyColumnWidth(float layoutWeight) {
		if (view == null || layoutWeight <= 0) {
			return;
		}

		LinearLayout leftColumn = view.findViewById(R.id.numpad_column_fn_left);
		LinearLayout rightColumn = view.findViewById(R.id.numpad_column_fn_right);

		LinearLayout.LayoutParams leftParams = leftColumn != null ? (LinearLayout.LayoutParams) leftColumn.getLayoutParams() : null;
		LinearLayout.LayoutParams rightParams = rightColumn != null ? (LinearLayout.LayoutParams) rightColumn.getLayoutParams() : null;
		if (leftParams == null || rightParams == null) {
			return;
		}

		leftParams.weight = layoutWeight;
		rightParams.weight = layoutWeight;
		leftColumn.setLayoutParams(leftParams);
		rightColumn.setLayoutParams(rightParams);
	}


	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();

			height =
				Math.round(resources.getDimension(R.dimen.numpad_status_bar_spacing_top))
				+ resources.getDimensionPixelSize(R.dimen.numpad_status_bar_spacing_bottom)
				+ resources.getDimensionPixelSize(R.dimen.numpad_suggestion_height)
				+ getKeyColumnHeight(calculateKeyHeight())
				+ Math.round(resources.getDimension(R.dimen.numpad_keys_spacing_bottom));
		}

		return height;
	}


	private void showLongSpace(boolean yes, int keyHeight) {
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

		// status bar
		ViewGroup statusBar = view.findViewById(R.id.status_bar_container);
		keys.add(statusBar.findViewById(R.id.soft_key_left_arrow));
		keys.add(statusBar.findViewById(R.id.soft_key_right_arrow));


		// left Fn
		ViewGroup left = view.findViewById(R.id.numpad_column_fn_left);
		keys.add(left.findViewById(R.id.soft_key_settings));
		keys.add(left.findViewById(R.id.soft_key_add_word));
		keys.add(left.findViewById(R.id.soft_key_shift));
		keys.add(left.findViewById(R.id.soft_key_lf4));

		// right Fn
		ViewGroup right = view.findViewById(R.id.numpad_column_fn_right);
		keys.add(right.findViewById(R.id.soft_key_numpad_backspace));
		keys.add(right.findViewById(R.id.soft_key_filter));
		keys.add(right.findViewById(R.id.soft_key_rf3));
		keys.add(right.findViewById(R.id.soft_key_numpad_ok));

		// digits panel
		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		keys.add(table.findViewById(R.id.soft_key_0));
		keys.add(table.findViewById(R.id.soft_key_1));
		keys.add(table.findViewById(R.id.soft_key_2));
		keys.add(table.findViewById(R.id.soft_key_3));
		keys.add(table.findViewById(R.id.soft_key_4));
		keys.add(table.findViewById(R.id.soft_key_5));
		keys.add(table.findViewById(R.id.soft_key_6));
		keys.add(table.findViewById(R.id.soft_key_7));
		keys.add(table.findViewById(R.id.soft_key_8));
		keys.add(table.findViewById(R.id.soft_key_9));
		keys.add(table.findViewById(R.id.soft_key_punctuation_1));
		keys.add(table.findViewById(R.id.soft_key_punctuation_2));

		// text editing panel
		keys.add(table.findViewById(R.id.soft_key_100));
		keys.add(table.findViewById(R.id.soft_key_101));
		keys.add(table.findViewById(R.id.soft_key_102));
		keys.add(table.findViewById(R.id.soft_key_103));
		keys.add(table.findViewById(R.id.soft_key_104));
		keys.add(table.findViewById(R.id.soft_key_105));
		keys.add(table.findViewById(R.id.soft_key_106));
		keys.add(table.findViewById(R.id.soft_key_107));
		keys.add(table.findViewById(R.id.soft_key_108));
		keys.add(table.findViewById(R.id.soft_key_109));
		keys.add(table.findViewById(R.id.soft_key_punctuation_101));
		keys.add(table.findViewById(R.id.soft_key_punctuation_102));

		// Long space panel
		keys.add(table.findViewById(R.id.soft_key_200));
		keys.add(table.findViewById(R.id.soft_key_punctuation_201));
		keys.add(table.findViewById(R.id.soft_key_punctuation_202));

		keys.addAll(getKeysFromContainer(view.findViewById(R.id.status_bar_container)));

		return keys;
	}


	@Override
	void render() {
		int defaultKeyHeight = calculateKeyHeight();

		getView();
		enableClickHandlers();
		setKeyHeight(defaultKeyHeight);
		setWidth(tt9.getSettings().getWidthPercent(), tt9.getSettings().getAlignment());
		setKeyColumnWidth(tt9.getSettings().getNumpadFnKeyScale());
		showLongSpace(
			tt9.getSettings().isNumpadShapeLongSpace() && !tt9.isInputModeNumeric() && !LanguageKind.isKorean(tt9.getLanguage()),
			defaultKeyHeight
		);
		for (SoftKey key : getKeys()) {
			key.render();
		}
	}
}
