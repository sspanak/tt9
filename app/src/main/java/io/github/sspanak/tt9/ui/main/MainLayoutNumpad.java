package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.hacks.DeviceInfo;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyArrow;
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

		for (SoftKey key : getKeys()) {
			if ((key instanceof SoftKeyArrow)) {
				continue;
			}

			// adjust the key height
			ViewGroup.LayoutParams layout = key.getLayoutParams();
			if (layout != null) {
				layout.height = height;
				key.setLayoutParams(layout);
			}

			// adjust the overlay height (if it exists)
			ViewParent parent = key.getParent();
			if (!(parent instanceof RelativeLayout)) {
				continue;
			}

			layout = ((RelativeLayout) parent).getLayoutParams();
			if (layout != null) {
				layout.height = height;
				((RelativeLayout) parent).setLayoutParams(layout);
			}
		}
	}


	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();
			height = getKeyHeightCompat() * 4
				+ resources.getDimensionPixelSize(R.dimen.numpad_suggestion_height)
				+ Math.round(resources.getDimension(R.dimen.numpad_padding_bottom))
				+ getBottomInsetSize();
		}

		return height;
	}


	private void setWidth(int width) {
		if (view == null || width <= 0) {
			return;
		}

		View widthConstraintWrapper = view.findViewById(R.id.numpad_width_constraint_wrapper);
		if (widthConstraintWrapper == null) {
			return;
		}

		ViewGroup.LayoutParams layout = widthConstraintWrapper.getLayoutParams();
		if (layout != null) {
			layout.width = width;
			widthConstraintWrapper.setLayoutParams(layout);
		}
	}


	@Override
	void render() {
		getView();
		alignView();
		setKeyHeight(getKeyHeightCompat());
		setWidth(tt9.getSettings().getNumpadWidth());
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

		ViewGroup statusBar = view.findViewById(R.id.status_bar_container);
		keys.add(statusBar.findViewById(R.id.soft_key_left_arrow));
		keys.add(statusBar.findViewById(R.id.soft_key_right_arrow));

		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		keys.add(table.findViewById(R.id.soft_key_settings));
		keys.add(table.findViewById(R.id.soft_key_add_word));
		keys.add(table.findViewById(R.id.soft_key_shift));
		keys.add(table.findViewById(R.id.soft_key_lf4));

		keys.add(table.findViewById(R.id.soft_key_numpad_backspace));
		keys.add(table.findViewById(R.id.soft_key_filter));
		keys.add(table.findViewById(R.id.soft_key_rf3));
		keys.add(table.findViewById(R.id.soft_key_numpad_ok));

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

		keys.addAll(getKeysFromContainer(view.findViewById(R.id.status_bar_container)));

		return keys;
	}
}
