package io.github.sspanak.tt9.ui.main;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyCommandPalette;

class MainLayoutSmall extends MainLayoutTray {
	MainLayoutSmall(TraditionalT9 tt9) {
		super(tt9);
	}

	@Override
	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			height = super.getHeight(forceRecalculate);

			if (!isCommandPaletteShown() && !isTextManipulationPaletteShown()) {
				height += tt9.getResources().getDimensionPixelSize(R.dimen.soft_key_height);
			}
		}

		return height;
	}

	@Override
	protected void setSoftKeysVisibility() {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.VISIBLE);
		}
	}

	@Override
	void hideCommandPalette() {
		super.hideCommandPalette();
		view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.VISIBLE);
	}

	@Override
	protected void enableClickHandlers() {
		super.enableClickHandlers();

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeyCommandPalette) {
				((SoftKeyCommandPalette) key).setMainView(tt9.getMainView());
			}
		}
	}

	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (view != null && keys.isEmpty()) {
			super.getKeys();
			keys.addAll(getKeysFromContainer(view.findViewById(R.id.main_soft_keys)));
		}
		return keys;
	}

	@Override
	protected ArrayList<View> getSeparators() {
		ArrayList<View> separators = super.getSeparators();
		separators.add(view.findViewById(R.id.main_separator_left));
		separators.add(view.findViewById(R.id.main_separator_right));

		return separators;
	}

	@Override
	void setDarkTheme(boolean dark) {
		super.setDarkTheme(dark);
		view.findViewById(R.id.main_soft_keys).setBackground(getBackgroundColor(view, dark));
	}
}
