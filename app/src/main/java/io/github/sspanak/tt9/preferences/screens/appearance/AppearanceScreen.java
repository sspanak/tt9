package io.github.sspanak.tt9.preferences.screens.appearance;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.items.ItemSwitch;
import io.github.sspanak.tt9.preferences.screens.ScreenWithPreviewKeyboardHeaderFragment;

public class AppearanceScreen extends ScreenWithPreviewKeyboardHeaderFragment {
	final public static String NAME = "Appearance";

	public AppearanceScreen() { super(); }
	public AppearanceScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_appearance; }
	@Override protected int getXml() { return R.xml.prefs_screen_appearance; }

	@Override
	protected void onCreate() {
		super.onCreate();
		createMainSection();
		createHacksSection();
		enablePreviewOnChange();
		resetFontSize(true);
	}


	@Override
	public void onResume() {
		super.onResume();
		DropDownColorScheme colorScheme = findPreference(DropDownColorScheme.NAME);
		if (colorScheme != null && activity != null) {
			colorScheme.populate(activity.getSettings());
		}
	}


	protected void createMainSection() {
		if (activity == null) {
			return;
		}

		(new ItemStatusIcon(findPreference(ItemStatusIcon.NAME), activity.getSettings())).populate();
		(new ItemSuggestionSmoothScroll(findPreference(ItemSuggestionSmoothScroll.NAME), activity.getSettings())).populate();

		ItemCategory categoryColors = new ItemCategory(findPreference("category_keyboard_color_scheme"));
		ItemCategory categoryGeometry = new ItemCategory(findPreference("category_keyboard_geometry"));
		ItemCategory categoryKeys = new ItemCategory(findPreference("category_keyboard_keys"));

		categoryColors.onLayoutChange(activity.getSettings().getMainViewLayout());
		categoryGeometry.onLayoutChange(activity.getSettings().getMainViewLayout());
		categoryKeys.onLayoutChange(activity.getSettings().getMainViewLayout());

		DropDownAlignment alignment = findPreference(DropDownAlignment.NAME);
		DropDownKeyHeight numpadKeyHeight = findPreference(DropDownKeyHeight.NAME);
		DropDownWidth keyboardWidth = findPreference(DropDownWidth.NAME);
		DropDownNumpadShape numpadShape = findPreference(DropDownNumpadShape.NAME);
		DropDownNumpadFnKeyScale fnKeyWidth = findPreference(DropDownNumpadFnKeyScale.NAME);
		DropDownNumpadKeyFontSize numpadKeyFontSize = findPreference(DropDownNumpadKeyFontSize.NAME);
		DropDownSuggestionFontSize suggestionFontSize = findPreference(DropDownSuggestionFontSize.NAME);
		ItemFnKeyOrder fnKeyOrder = new ItemFnKeyOrder(activity.getSettings(), findPreference(ItemFnKeyOrder.NAME));
		DropDownBottomPaddingPortrait bottomPadding = findPreference(DropDownBottomPaddingPortrait.NAME);
		SwitchLeftRightArrows leftRightArrows = findPreference(SwitchLeftRightArrows.NAME);
		SwitchDoubleTapResize doubleTapResize = findPreference(SwitchDoubleTapResize.NAME);
		SwitchDragResize dragResize = findPreference(SwitchDragResize.NAME);

		DropDownLayoutType selectLayout = findPreference(DropDownLayoutType.NAME);
		if (selectLayout != null) {
			selectLayout
				.addOnChangeItem(new ItemCategory(findPreference("category_keyboard_color_scheme")))
				.addOnChangeItem(new ItemCategory(findPreference("category_keyboard_geometry")))
				.addOnChangeItem(new ItemCategory(findPreference("category_keyboard_keys")))
				.addOnChangeItem(fnKeyOrder)
				.addOnChangeItem(fnKeyWidth)
				.addOnChangeItem(numpadKeyFontSize)
				.addOnChangeItem(numpadKeyHeight)
				.addOnChangeItem(numpadShape)
				.addOnChangeItem(suggestionFontSize)
				.addOnChangePreference(bottomPadding)
				.addOnChangePreference(findPreference("pref_alternative_suggestion_scrolling"))
				.addOnChangePreference(findPreference(SwitchShowArrowsUpDown.NAME))
				.addOnChangePreference(leftRightArrows)
				.addOnChangePreference(findPreference("pref_clear_insets"))
				.addOnChangePreference(doubleTapResize)
				.addOnChangePreference(dragResize)
				.addOnChangePreference(findPreference("pref_suggestion_smooth_scroll"));
		}

		EnhancedDropDownPreference[] dropdowns = {
			findPreference(DropDownSettingsFontSize.NAME),
			selectLayout,
			numpadKeyHeight,
			alignment,
			keyboardWidth,
			numpadShape,
			fnKeyWidth,
			numpadKeyFontSize,
			suggestionFontSize,
			bottomPadding
		};

		for (EnhancedDropDownPreference item : dropdowns) {
			if (item instanceof DropDownSettingsFontSize) {
				((DropDownSettingsFontSize) item).setScreen(this);
			} // not else-if, we want both!
			if (item != null) {
				item.populate(activity.getSettings()).preview();
			}
		}

		SwitchWhenLargeTouchscreenLayout[] switches = {
			leftRightArrows,
			dragResize,
			doubleTapResize
		};

		for (SwitchWhenLargeTouchscreenLayout item : switches) {
			if (item != null) {
				item.populate(activity.getSettings());
			}
		}
	}


	private void createHacksSection() {
		if (activity == null) {
			return;
		}

		ItemSwitch[] items = {
			new ItemAlternativeSuggestionScrolling(findPreference(ItemAlternativeSuggestionScrolling.NAME), activity.getSettings()),
			new ItemClearInsets(findPreference(ItemClearInsets.NAME), activity.getSettings())
		};

		for (ItemSwitch item : items) {
			item.populate().enableClickHandler();
		}
	}


	private void enablePreviewOnChange() {
		DropDownColorScheme colorScheme = findPreference(DropDownColorScheme.NAME);
		if (colorScheme != null) {
			colorScheme.setOnChangeListener(this::onThemeChange);
		}

		EnhancedDropDownPreference[] items = {
			findPreference(DropDownLayoutType.NAME),
			findPreference(DropDownAlignment.NAME),
			findPreference(DropDownBottomPaddingPortrait.NAME),
			findPreference(DropDownWidth.NAME),
			findPreference(DropDownNumpadShape.NAME),
			findPreference(DropDownNumpadFnKeyScale.NAME),
			findPreference(DropDownNumpadKeyFontSize.NAME),
			findPreference(DropDownSuggestionFontSize.NAME),
		};

		for (EnhancedDropDownPreference item : items) {
			if (item != null) {
				item.setOnChangeListener(this::previewDropDownChange);
			}
		}

		SwitchPreferenceCompat[] switches = {
			findPreference(SwitchKeyShadows.NAME),
			findPreference(SwitchLeftRightArrows.NAME),
			findPreference(SwitchShowArrowsUpDown.NAME),
			findPreference("pref_status_icon"),
		};

		for (SwitchPreferenceCompat sw : switches) {
			if (sw != null) {
				sw.setOnPreferenceChangeListener(this::previewSwitchChange);
			}
		}
	}


	protected void onThemeChange(String s) {
		previewDropDownChange(null);
	}


	private void previewDropDownChange(String s) {
		previewKeyboard();
	}


	private boolean previewSwitchChange(Preference p, Object o) {
		previewDropDownChange(null);
		return true;
	}
}
