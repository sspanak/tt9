package io.github.sspanak.tt9.preferences.screens.fnKeyOrder;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class FnKeyOrderScreen extends BaseScreenFragment {
	final public static String NAME = "FnKeyOrder";
	public FnKeyOrderScreen() { init(); }
	public FnKeyOrderScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_fn_key_order; }
	@Override protected int getXml() { return R.xml.prefs_screen_fn_key_order; }

	@Override
	protected void onCreate() {
		resetFontSize(true);
	}
}
