package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutTray extends BaseMainLayout {
	protected int height;
	protected boolean isCommandPaletteShown = false;
	protected boolean isTextEditingPaletteShown = false;


	MainLayoutTray(TraditionalT9 tt9) {
		super(tt9, R.layout.main_small);
	}


	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();
			height = resources.getDimensionPixelSize(R.dimen.status_bar_height) + getPanelHeight(resources);
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


	protected void setSoftKeysVisibility() {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
		}
	}


	void showCommandPalette() {
		isCommandPaletteShown = true;
		isTextEditingPaletteShown = false;
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.VISIBLE);
		getHeight(true);
		renderKeys();
	}


	void showKeyboard() {
		isCommandPaletteShown = false;
		isTextEditingPaletteShown = false;
		getHeight(true);
		renderKeys();
	}


	@Override
	void showTextEditingPalette() {
		isCommandPaletteShown = false;
		isTextEditingPaletteShown = true;
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.VISIBLE);
		getHeight(true);
		renderKeys();
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
		renderKeys();
	}
}
