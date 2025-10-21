package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutTray extends MainLayoutExtraPanel {
	protected int height;
	protected boolean isCommandPaletteShown = false;
	protected boolean isTextEditingPaletteShown = false;


	MainLayoutTray(TraditionalT9 tt9) {
		super(tt9, R.layout.main_small);
	}


	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();
			height = getStatusBarHeight(resources, tt9.getSettings()) + getPanelHeight(resources);
		}

		return height;
	}


	protected int getPanelHeight(@NonNull Resources resources) {
		if (isCommandPaletteShown() || isTextEditingPaletteShown()) {
			return resources.getDimensionPixelSize(R.dimen.main_small_command_palette_height);
		} else {
			return 0;
		}
	}


	private int getStatusBarHeight(@NonNull Resources resources, @NonNull SettingsStore settings) {
		float textSize = resources.getDimension(R.dimen.status_bar_text_size);
		float padding = textSize * 0.45f;
		padding = padding < 1 ? 1 : padding;
		return Math.round((padding + textSize) * settings.getSuggestionFontScale());
	}


	protected void setSoftKeysVisibility() {
		if (view != null) {
			togglePanel(R.id.main_soft_keys, false);
		}
	}


	void showCommandPalette() {
		super.showCommandPalette();
		isCommandPaletteShown = true;
		isTextEditingPaletteShown = false;
		togglePanel(R.id.main_command_keys, true);
		getHeight(true);
		renderKeys(false);
	}


	void showKeyboard() {
		super.showKeyboard();
		togglePanel(R.id.main_command_keys, false);
		isCommandPaletteShown = false;
		isTextEditingPaletteShown = false;
		getHeight(true);
		renderKeys(false);
	}


	@Override
	void showTextEditingPalette() {
		super.showTextEditingPalette();
		isCommandPaletteShown = false;
		isTextEditingPaletteShown = true;
		togglePanel(R.id.main_command_keys, true);
		getHeight(true);
		renderKeys(false);
	}


	@Override
	boolean isCommandPaletteShown() {
		return isCommandPaletteShown;
	}


	@Override
	boolean isTextEditingPaletteShown() {
		return isTextEditingPaletteShown;
	}


	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (view != null && keys.isEmpty()) {
			keys.addAll(getKeysFromContainer(view.findViewById(R.id.main_command_keys)));
		}
		return keys;
	}


	@Override
	void render() {
		getView();
		setSoftKeysVisibility();
		preventEdgeToEdge();
		setWidth(tt9.getSettings().getWidthPercent(), tt9.getSettings().getAlignment());
		setBackgroundBlending();
		enableClickHandlers();
		renderKeys(false);
	}
}
