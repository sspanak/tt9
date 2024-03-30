package io.github.sspanak.tt9.ui.main;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutStealth extends BaseMainLayout {
	MainLayoutStealth(TraditionalT9 tt9) { super(tt9, R.layout.main_stealth); }

	@Override public void render() {}
	@Override public void setDarkTheme(boolean y) {}
	@Override protected ArrayList<SoftKey> getKeys() { return keys; }
}
