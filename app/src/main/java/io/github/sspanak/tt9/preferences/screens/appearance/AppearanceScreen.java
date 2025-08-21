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
		(new ItemDragResize(findPreference(ItemDragResize.NAME), activity.getSettings())).populate();
		(new ItemSuggestionSmoothScroll(findPreference(ItemSuggestionSmoothScroll.NAME), activity.getSettings())).populate();
		(new ItemPrecalculateNavbarHeight(activity.getSettings(), findPreference(ItemPrecalculateNavbarHeight.NAME))).populate();

		ItemAlignment alignment = new ItemAlignment(findPreference(ItemAlignment.NAME), activity.getSettings());
		ItemNumpadKeyHeight numpadKeyHeight = new ItemNumpadKeyHeight(findPreference(ItemNumpadKeyHeight.NAME), activity.getSettings());
		ItemWidth keyboardWidth = new ItemWidth(findPreference(ItemWidth.NAME), activity.getSettings());
		ItemNumpadShape numpadShape = new ItemNumpadShape(findPreference(ItemNumpadShape.NAME), activity.getSettings());
		ItemShowArrows showArrows = new ItemShowArrows(findPreference(ItemShowArrows.NAME), activity.getSettings());
		ItemNumpadFnKeyScale fnKeyWidth = new ItemNumpadFnKeyScale(findPreference(ItemNumpadFnKeyScale.NAME), activity.getSettings());
		ItemNumpadKeyFontSize numpadKeyFontSize = new ItemNumpadKeyFontSize(findPreference(ItemNumpadKeyFontSize.NAME), activity.getSettings());
		ItemSuggestionFontSize suggestionFontSize = new ItemSuggestionFontSize(findPreference(ItemSuggestionFontSize.NAME), activity.getSettings());
		ItemFnKeyOrder fnKeyOrder = new ItemFnKeyOrder(activity.getSettings(), findPreference(ItemFnKeyOrder.NAME));

		ItemSelectLayoutType selectLayout = new ItemSelectLayoutType(findPreference(ItemSelectLayoutType.NAME), activity)
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


		ItemDropDown[] items = {
			new ItemSelectTheme(findPreference(ItemSelectTheme.NAME), activity),
			new ItemSelectSettingsFontSize(findPreference(ItemSelectSettingsFontSize.NAME), this),
			selectLayout,
			numpadKeyHeight,
			alignment,
			keyboardWidth,
			numpadShape,
			fnKeyWidth,
			numpadKeyFontSize,
			suggestionFontSize
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
