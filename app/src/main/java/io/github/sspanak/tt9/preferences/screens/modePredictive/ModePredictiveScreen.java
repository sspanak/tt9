package io.github.sspanak.tt9.preferences.screens.modePredictive;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class ModePredictiveScreen extends BaseScreenFragment {
	public static final String NAME = "ModePredictive";

	public ModePredictiveScreen() { super(); }
	public ModePredictiveScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_predictive_mode; }
	@Override protected int getXml() { return R.xml.prefs_screen_mode_predictive; }

	@Override
	protected void onCreate() {
		EnhancedDropDownPreference[] dropdowns = {
			findPreference(DropDownOneKeyEmoji.NAME),
			findPreference(DropDownZeroKeyCharacter.NAME),
		};

		for (EnhancedDropDownPreference dropdown : dropdowns) {
			if (dropdown != null && activity != null) dropdown.populate(activity.getSettings()).preview();
		}

		resetFontSize(false);
	}
}
