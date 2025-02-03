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

		ItemAlignment alignment = new ItemAlignment(findPreference(ItemAlignment.NAME), activity.getSettings());
		ItemNumpadKeyHeight numpadKeyHeight = new ItemNumpadKeyHeight(findPreference(ItemNumpadKeyHeight.NAME), activity.getSettings());
		ItemWidth keyboardWidth = new ItemWidth(findPreference(ItemWidth.NAME), activity.getSettings());
		ItemNumpadShape numpadShape = new ItemNumpadShape(findPreference(ItemNumpadShape.NAME), activity.getSettings());
		ItemShowArrows showArrows = new ItemShowArrows(findPreference(ItemShowArrows.NAME), activity.getSettings());
		ItemNumpadFnKeyScale fnKeyWidth = new ItemNumpadFnKeyScale(findPreference(ItemNumpadFnKeyScale.NAME), activity.getSettings());

		ItemSelectLayoutType selectLayout = new ItemSelectLayoutType(findPreference(ItemSelectLayoutType.NAME), activity)
			.addOnChangeItem(alignment)
			.addOnChangeItem(fnKeyWidth)
			.addOnChangeItem(keyboardWidth)
			.addOnChangeItem(numpadKeyHeight)
			.addOnChangeItem(numpadShape)
			.addOnChangeItem(showArrows);

		ItemDropDown[] items = {
			new ItemSelectTheme(findPreference(ItemSelectTheme.NAME), activity),
			new ItemSelectSettingsFontSize(findPreference(ItemSelectSettingsFontSize.NAME), this),
			selectLayout,
			numpadKeyHeight,
			alignment,
			keyboardWidth,
			numpadShape,
			fnKeyWidth
		};

		for (ItemDropDown item : items) {
			item.populate().preview().enableClickHandler();
		}

		showArrows.populate();
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
