package io.github.sspanak.tt9.ui.main;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;

class MainLayoutStealth extends BaseMainLayout {
	private boolean isCommandPaletteShown = false;
	private boolean isTextManipulationPaletteShown = false;

	MainLayoutStealth(TraditionalT9 tt9) { super(tt9, R.layout.main_stealth); }

	@Override void showCommandPalette() {
		isCommandPaletteShown = true;
		isTextManipulationPaletteShown = false;
	}
	@Override void hideCommandPalette() { isCommandPaletteShown = false; }
	@Override boolean isCommandPaletteShown() { return isCommandPaletteShown; }

	@Override void showTextManipulationPalette() {
		isTextManipulationPaletteShown = true;
		isCommandPaletteShown = false;
	}
	@Override void hideTextManipulationPalette() { isTextManipulationPaletteShown = false; }
	@Override boolean isTextManipulationPaletteShown() { return isTextManipulationPaletteShown; }

	@Override void render() {}
}
