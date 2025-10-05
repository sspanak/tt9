package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;

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
		height = super.getHeight(forceRecalculate);

		if (height <= 0 || forceRecalculate) {
			if (tt9.getSettings().getMessengerReplyExtraPadding()) {
				height += tt9.getResources().getDimensionPixelSize(R.dimen.main_small_main_key_wrapper_extra_height_for_messenger);
			}
		}

		return height;
	}


	@Override
	protected int getPanelHeight(@NonNull Resources resources) {
		if (isCommandPaletteShown() || isTextEditingPaletteShown()) {
			return super.getPanelHeight(resources);
		} else {
			return tt9.getResources().getDimensionPixelSize(R.dimen.main_small_main_key_wrapper_height);
		}
	}


	@Override
	protected void setSoftKeysVisibility() {
		if (view != null) {
			togglePanel(R.id.main_soft_keys, true);
			togglePanel(R.id.main_small_messenger_padding_hack, tt9.getSettings().getMessengerReplyExtraPadding());
		}
	}


	@Override
	void showCommandPalette() {
		togglePanel(R.id.main_soft_keys, false);
		super.showCommandPalette();
	}


	@Override
	void showKeyboard() {
		togglePanel(R.id.main_soft_keys, true);
		super.showKeyboard();
	}


	@Override
	void showTextEditingPalette() {
		togglePanel(R.id.main_soft_keys, false);
		super.showTextEditingPalette();
	}


	@Override
	void showDeveloperCommands() {
		togglePanel(R.id.main_soft_keys, false);
		super.showDeveloperCommands();
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
}
