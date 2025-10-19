package io.github.sspanak.tt9.ui.main;

import android.view.View;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;

public class StaticMainView {
	private final static String LOG_TAG = StaticMainView.class.getSimpleName();

	protected final TraditionalT9 tt9;
	@Nullable protected BaseMainLayout main;
	private boolean darkTheme;


	protected StaticMainView(TraditionalT9 tt9) {
		this.tt9 = tt9;
		forceCreate();
	}


	protected BaseMainLayout getViewInstance(SettingsStore settings) {
		if (settings.isMainLayoutNumpad() && !(main instanceof MainLayoutNumpad)) {
			return new MainLayoutNumpad(tt9);
		} else if (settings.isMainLayoutSmall() && (main == null || !main.getClass().equals(MainLayoutSmall.class))) {
			return new MainLayoutSmall(tt9);
		} else if (settings.isMainLayoutTray() && (main == null || !main.getClass().equals(MainLayoutTray.class))) {
			return new MainLayoutTray(tt9);
		} else if (settings.isMainLayoutStealth() && !(main instanceof MainLayoutStealth)) {
			return new MainLayoutStealth(tt9);
		}

		return null;
	}


	public boolean create() {
		SettingsStore settings = tt9.getSettings();

		if (darkTheme != settings.getDarkTheme()) {
			darkTheme = settings.getDarkTheme();
			main = null;
		}

		final BaseMainLayout newMain = getViewInstance(settings);
		if (newMain == null) {
			return false;
		}

		main = newMain;

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

		main.showKeyboard();
		main.render();
	}

	public void renderDynamicKeys() {
		if (main == null) {
			Logger.e(LOG_TAG, "Cannot render dynamic keys for a null MainView.");
			return;
		}

		main.renderKeys(true);
	}

	public void showCommandPalette() {
		if (main != null) {
			main.showCommandPalette();
		}
	}

	public void showKeyboard() {
		if (main != null) {
			main.showKeyboard();
		}
	}

	public void showTextEditingPalette() {
		if (main != null) {
			main.showTextEditingPalette();
		}
	}

	public boolean isCommandPaletteShown() {
		return main != null && main.isCommandPaletteShown();
	}

	public boolean isFnPanelVisible() {
		return main != null && main.isFnPanelVisible();
	}

	public boolean isTextEditingPaletteShown() {
		return main != null && main.isTextEditingPaletteShown();
	}
}
