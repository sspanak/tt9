package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyFn;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyNumber;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyPunctuation;
import io.github.sspanak.tt9.ui.main.keys.SoftKeySettings;

class MainLayoutNumpad extends BaseMainLayout {
	private boolean isTextEditingShown = false;
	private int height;


	MainLayoutNumpad(TraditionalT9 tt9) {
		super(tt9, R.layout.main_numpad);
	}

	private void alignView() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || view == null) {
			return;
		}

		LinearLayout container = view.findViewById(R.id.numpad_container);
		if (container != null) {
			container.setGravity(tt9.getSettings().getNumpadAlignment());
		}
	}

	private int getBackgroundColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getColor(
			contextView.getContext(),
			dark ? R.color.dark_numpad_background : R.color.numpad_background
		);
	}


	private int getSeparatorColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getColor(
			contextView.getContext(),
			dark ? R.color.dark_numpad_separator : R.color.numpad_separator
		);
	}


	@Override
	void setDarkTheme(boolean dark) {
		if (view == null) {
			return;
		}

		// background
		view.setBackgroundColor(getBackgroundColor(view, dark));

		// text
		for (SoftKey key : getKeys()) {
			key.setDarkTheme(dark);
		}

		// separators
		int separatorColor = getSeparatorColor(view, dark);
		for (View separator : getSeparators()) {
			if (separator != null) {
				separator.setBackgroundColor(separatorColor);
			}
		}
	}


	@Override void showCommandPalette() {}
	@Override void hideCommandPalette() {}
	@Override boolean isCommandPaletteShown() { return false; }


	@Override
	void showTextEditingPalette() {
		isTextEditingShown = true;

		for (SoftKey key : getKeys()) {
			int keyId = key.getId();

			if (keyId == R.id.soft_key_0) {
				key.setEnabled(tt9 != null && !tt9.isInputModeNumeric());
			} else if (key.getClass().equals(SoftKeyNumber.class)) {
				key.setVisibility(View.GONE);
			}

			if (key.getClass().equals(SoftKeyPunctuation.class)) {
				key.setVisibility(View.INVISIBLE);
			}

			if (key.getClass().equals(SoftKeyFn.class)) {
				key.setVisibility(View.VISIBLE);
			}

			if (keyId == R.id.soft_key_rf3) {
				key.render();
			}

			if (
				keyId == R.id.soft_key_add_word
				|| keyId == R.id.soft_key_lf3
				|| keyId == R.id.soft_key_lf4
				|| keyId == R.id.soft_key_filter_suggestions
			) {
				key.setEnabled(false);
			}
		}
	}

	@Override
	void hideTextEditingPalette() {
		isTextEditingShown = false;

		for (SoftKey key : getKeys()) {
			if (key.getClass().equals(SoftKeyNumber.class) || key.getClass().equals(SoftKeyPunctuation.class)) {
				key.setVisibility(View.VISIBLE);
				key.setEnabled(true);
			}

			if (key.getClass().equals(SoftKeyFn.class)) {
				key.setVisibility(View.GONE);
			}


			int keyId = key.getId();

			if (keyId == R.id.soft_key_rf3) {
				key.render();
			}

			if (
				keyId == R.id.soft_key_add_word
				|| keyId == R.id.soft_key_lf3
				|| keyId == R.id.soft_key_lf4
				|| keyId == R.id.soft_key_filter_suggestions
			) {
				key.setEnabled(true);
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
	private int getKeyHeightCompat() {
		int keyHeight = tt9.getSettings().getNumpadKeyHeight();
		int screenHeight = DeviceInfo.getScreenHeight(tt9.getApplicationContext());

		boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9.getApplicationContext());
		double maxScreenHeight = isLandscape ? screenHeight * 0.75 : screenHeight * 0.8;
		double maxKeyHeight = isLandscape ? screenHeight * 0.115 : screenHeight * 0.125;

		// it's all very approximate but when it comes to screen dimensions,
		// accuracy is not that important
		return keyHeight * 5 > maxScreenHeight ? (int) Math.round(maxKeyHeight) : keyHeight;
	}


	void setKeyHeight(int height) {
		if (view == null || height <= 0) {
			return;
		}

		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		int tableRowsCount = table.getChildCount();

		for (int rowId = 0; rowId < tableRowsCount; rowId++) {
			View row = table.getChildAt(rowId);
			ViewGroup.LayoutParams layout = row.getLayoutParams();
			if (layout != null) {
				layout.height = height;
				row.setLayoutParams(layout);
			}
		}
	}


	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();
			height = getKeyHeightCompat() * 4
				+ resources.getDimensionPixelSize(R.dimen.numpad_candidate_height)
				+ Math.round(resources.getDimension(R.dimen.numpad_padding_bottom))
				+ getBottomInsetSize();
		}

		return height;
	}


	@Override
	void render() {
		getView();
		alignView();
		setKeyHeight(getKeyHeightCompat());
		enableClickHandlers();
		for (SoftKey key : getKeys()) {
			key.render();
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
		if (!keys.isEmpty()) {
			return keys;
		}

		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		int tableRowsCount = table.getChildCount();

		for (int rowId = 0; rowId < tableRowsCount; rowId++) {
			View row = table.getChildAt(rowId);
			if (row instanceof ViewGroup) {
				keys.addAll(getKeysFromContainer((ViewGroup) row));
			}
		}

		keys.addAll(getKeysFromContainer(view.findViewById(R.id.status_bar_container)));

		return keys;
	}


	protected ArrayList<View> getSeparators() {
		// it's fine... it's shorter, faster and easier to read than searching with 3 nested loops
		return new ArrayList<>(Arrays.asList(
			view.findViewById(R.id.separator_top),
			view.findViewById(R.id.separator_candidates_1),
			view.findViewById(R.id.separator_candidates_2),
			view.findViewById(R.id.separator_candidates_bottom),
			view.findViewById(R.id.separator_1_1),
			view.findViewById(R.id.separator_1_2),
			view.findViewById(R.id.separator_2_1),
			view.findViewById(R.id.separator_2_2),
			view.findViewById(R.id.separator_3_1),
			view.findViewById(R.id.separator_3_2),
			view.findViewById(R.id.separator_4_1),
			view.findViewById(R.id.separator_4_2)
		));
	}
}
