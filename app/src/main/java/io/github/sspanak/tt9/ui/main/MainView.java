package io.github.sspanak.tt9.ui.main;

import android.view.View;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class MainView {
	private final static String LOG_TAG = MainView.class.getSimpleName();

	protected final TraditionalT9 tt9;
	@Nullable protected BaseMainLayout main;
	private boolean darkTheme;


	protected MainView(TraditionalT9 tt9) {
		this.tt9 = tt9;
		forceCreate();
	}

	public boolean create() {
		SettingsStore settings = tt9.getSettings();

		if (darkTheme != settings.getDarkTheme()) {
			darkTheme = settings.getDarkTheme();
			main = null;
		}

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

	public void destroy() {
		main = null;
	}

	public void forceCreate() {
		if (main != null) {
			Logger.w(LOG_TAG, "MainView already exists. Re-creating by request.");
			destroy();
		}
		if (!create()) {
			Logger.w(LOG_TAG, "Invalid MainView setting. Creating default.");
			main = new MainLayoutSmall(tt9);
		}
	}

	@Nullable
	public View getView() {
		return main != null ? main.getView() : null;
	}

	public void render() {
		if (main == null) {
			Logger.e(LOG_TAG, "Cannot render a null MainView.");
			return;
		}

		main.hideCommandPalette();
		main.hideTextEditingPalette();
		main.render();
	}

	public void renderDynamicKeys() {
		if (main == null) {
			Logger.e(LOG_TAG, "Cannot render dynamic keys for a null MainView.");
			return;
		}

		main.renderDynamicKeys();
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

	public void showTextEditingPalette() {
		if (main != null) {
			main.showTextEditingPalette();
		}
	}

	public void hideTextEditingPalette() {
		if (main != null) {
			main.hideTextEditingPalette();
		}
	}

	public boolean isTextEditingPaletteShown() {
		return main != null && main.isTextEditingPaletteShown();
	}
}
