package io.github.sspanak.tt9.preferences.screens.mindReading;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class MindReadingScreen extends BaseScreenFragment {
	public static final String NAME = "MindReading";

	public MindReadingScreen() { super(); }
	public MindReadingScreen(io.github.sspanak.tt9.preferences.PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_auto_mind_reading; }
	@Override protected int getXml() { return io.github.sspanak.tt9.R.xml.prefs_screen_mind_reading; }

	@Override
	protected void onCreate() {
		resetFontSize(false);
	}
}
