package io.github.sspanak.tt9.preferences.screens.appearance;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.items.ItemDropDown;
import io.github.sspanak.tt9.preferences.items.ItemSwitch;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class AppearanceScreen extends BaseScreenFragment {
	final public static String NAME = "Appearance";
	public AppearanceScreen() { init(); }
	public AppearanceScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_appearance; }
	@Override protected int getXml() { return R.xml.prefs_screen_appearance; }

	@Override
	protected void onCreate() {
		createMainSection();
		createHacksSection();
		resetFontSize(true);
	}

	private void createMainSection() {
		(new ItemStatusIcon(findPreference(ItemStatusIcon.NAME), activity.getSettings())).populate();
		ItemHapticFeedback hapticFeedback = (new ItemHapticFeedback(findPreference(ItemHapticFeedback.NAME), activity.getSettings())).populate();

		ItemNumpadKeyHeight numpadKeyHeight = new ItemNumpadKeyHeight(findPreference(ItemNumpadKeyHeight.NAME), activity.getSettings());
		ItemDropDown[] items = {
			new ItemSelectTheme(findPreference(ItemSelectTheme.NAME), activity),
			new ItemSelectLayoutType(
				findPreference(ItemSelectLayoutType.NAME),
				activity,
				(layout) -> {
					hapticFeedback.onLayoutChange(layout);
					numpadKeyHeight.onLayoutChange(layout);
				}),
			new ItemSelectSettingsFontSize(findPreference(ItemSelectSettingsFontSize.NAME), this),
			numpadKeyHeight
		};

		for (ItemDropDown item : items) {
			item.populate().preview().enableClickHandler();
		}
	}

	private void createHacksSection() {
		ItemSwitch[] items = {
			new ItemAlternativeSuggestionScrolling(findPreference(ItemAlternativeSuggestionScrolling.NAME), activity.getSettings()),
			new ItemClearInsets(findPreference(ItemClearInsets.NAME), activity.getSettings())
		};

		for (ItemSwitch item : items) {
			item.populate().enableClickHandler();
		}
	}
}
