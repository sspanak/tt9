package io.github.sspanak.tt9.ui.main;

import android.view.View;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class MainView {
	private final TraditionalT9 tt9;
	private BaseMainLayout main;

	public MainView(TraditionalT9 tt9) {
		this.tt9 = tt9;

		forceCreateView();
	}

public boolean createView() {
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


	public void forceCreateView() {
		main = null;
		if (!createView()) {
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
}
