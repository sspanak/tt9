package io.github.sspanak.tt9.preferences.screens.appearance;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
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

	protected void createMainSection() {
		(new ItemStatusIcon(findPreference(ItemStatusIcon.NAME), activity.getSettings())).populate();
		(new ItemDragResize(findPreference(ItemDragResize.NAME), activity.getSettings())).populate();
		(new ItemSuggestionSmoothScroll(findPreference(ItemSuggestionSmoothScroll.NAME), activity.getSettings())).populate();
		(new ItemPrecalculateNavbarHeight(activity.getSettings(), findPreference(ItemPrecalculateNavbarHeight.NAME))).populate();

		DropDownAlignment alignment = findPreference(DropDownAlignment.NAME);
		DropDownKeyHeight numpadKeyHeight = findPreference(DropDownKeyHeight.NAME);
		DropDownWidth keyboardWidth = findPreference(DropDownWidth.NAME);
		DropDownNumpadShape numpadShape = findPreference(DropDownNumpadShape.NAME);
		ItemShowArrows showArrows = new ItemShowArrows(findPreference(ItemShowArrows.NAME), activity.getSettings());
		DropDownNumpadFnKeyScale fnKeyWidth = findPreference(DropDownNumpadFnKeyScale.NAME);
		DropDownNumpadKeyFontSize numpadKeyFontSize = findPreference(DropDownNumpadKeyFontSize.NAME);
		DropDownSuggestionFontSize suggestionFontSize = findPreference(DropDownSuggestionFontSize.NAME);
		ItemFnKeyOrder fnKeyOrder = new ItemFnKeyOrder(activity.getSettings(), findPreference(ItemFnKeyOrder.NAME));

		DropDownLayoutType selectLayout = findPreference(DropDownLayoutType.NAME);
		if (selectLayout != null) {
			selectLayout
				.addOnChangeItem(alignment)
				.addOnChangeItem(fnKeyOrder)
				.addOnChangeItem(fnKeyWidth)
				.addOnChangeItem(keyboardWidth)
				.addOnChangeItem(numpadKeyFontSize)
				.addOnChangeItem(numpadKeyHeight)
				.addOnChangeItem(numpadShape)
				.addOnChangeItem(showArrows)
				.addOnChangeItem(suggestionFontSize)
				.addOnChangePreference(findPreference("hack_precalculate_navbar_height_v3"))
				.addOnChangePreference(findPreference("pref_alternative_suggestion_scrolling"))
				.addOnChangePreference(findPreference("pref_clear_insets"))
				.addOnChangePreference(findPreference("pref_drag_resize"))
				.addOnChangePreference(findPreference("pref_suggestion_smooth_scroll"));
		}

		EnhancedDropDownPreference[] items = {
			findPreference(DropDownSettingsFontSize.NAME),
			selectLayout,
			numpadKeyHeight,
			alignment,
			keyboardWidth,
			numpadShape,
			fnKeyWidth,
			numpadKeyFontSize,
			suggestionFontSize
		};

		for (EnhancedDropDownPreference item : items) {
			if (item instanceof DropDownSettingsFontSize) {
				((DropDownSettingsFontSize) item).setScreen(this);
			} // not else-if, we want both!
			if (item != null) {
				item.populate(activity.getSettings()).preview();
			}
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
