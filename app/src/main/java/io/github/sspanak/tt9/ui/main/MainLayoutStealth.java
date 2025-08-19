package io.github.sspanak.tt9.ui.main;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

class MainLayoutStealth extends BaseMainLayout {
	private boolean isCommandPaletteShown = false;
	private boolean isTextEditingPaletteShown = false;

	MainLayoutStealth(TraditionalT9 tt9) { super(tt9, R.layout.main_stealth); }

	@Override void showCommandPalette() {
		isCommandPaletteShown = true;
		isTextEditingPaletteShown = false;
	}

	@Override
	void showKeyboard() {
		isCommandPaletteShown = false;
		isTextEditingPaletteShown = false;
	}

	@Override void showTextEditingPalette() {
		isTextEditingPaletteShown = true;
		isCommandPaletteShown = false;
	}

	@Override boolean isCommandPaletteShown() { return isCommandPaletteShown; }
	@Override boolean isTextEditingPaletteShown() { return isTextEditingPaletteShown; }
	@Override void setWidth(int w, int g) {}
	@Override void render() {}
	@Override void renderKeys() {}
}
