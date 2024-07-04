package io.github.sspanak.tt9.ui.main;

import android.view.View;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class MainView {
	protected final TraditionalT9 tt9;
	protected BaseMainLayout main;


	protected MainView(TraditionalT9 tt9) {
		this.tt9 = tt9;

		forceCreateInputView();
	}

	public boolean createInputView() {
		SettingsStore settings = tt9.getSettings();

		if (settings.isMainLayoutNumpad() && !(main instanceof MainLayoutNumpad)) {
			main = new MainLayoutNumpad(tt9);
		} else if (settings.isMainLayoutSmall() && (main == null || !main.getClass().equals(MainLayoutSmall.class))) {
			main = new MainLayoutSmall(tt9);
		} else if (settings.isMainLayoutTray() && (main == null || !main.getClass().equals(MainLayoutTray.class))) {
			main = new MainLayoutTray(tt9);
		} else if (settings.isMainLayoutStealth() && !(main instanceof MainLayoutStealth)) {
			main = new MainLayoutStealth(tt9);
		} else {
			return false;
		}

		main.render();

		return true;
	}

	public void forceCreateInputView() {
		main = null;
		if (!createInputView()) {
			Logger.w(getClass().getSimpleName(), "Invalid MainView setting. Creating default.");
			main = new MainLayoutSmall(tt9);
		}
	}

	public View getView() {
		return main.getView();
	}

	public void render() {
		main.render();
	}

	public void setDarkTheme(boolean darkEnabled) {
		main.setDarkTheme(darkEnabled);
	}

	public void showCommandPalette() {
		if (main != null) {
			main.showCommandPalette();
		}
	}

	public void hideCommandPalette() {
		if (main != null) {
			main.hideCommandPalette();
		}
	}

	public boolean isCommandPaletteShown() {
		return main != null && main.isCommandPaletteShown();
	}

	public void showTextManipulationPalette() {
		if (main != null) {
			main.showTextManipulationPalette();
		}
	}

	public void hideTextManipulationPalette() {
		if (main != null) {
			main.hideTextManipulationPalette();
		}
	}

	public boolean isTextManipulationPaletteShown() {
		return main != null && main.isTextManipulationPaletteShown();
	}
}
