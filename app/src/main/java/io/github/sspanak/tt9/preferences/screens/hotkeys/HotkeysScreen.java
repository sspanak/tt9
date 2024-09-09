package io.github.sspanak.tt9.preferences.screens.hotkeys;

import androidx.preference.DropDownPreference;

import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

public class HotkeysScreen extends BaseScreenFragment {
	final public static String NAME = "Hotkeys";
	public HotkeysScreen() { init(); }
	public HotkeysScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_function_keys; }
	@Override protected int getXml() { return R.xml.prefs_screen_hotkeys; }

	@Override
	public void onCreate() {
		DropDownPreference[] dropDowns = {
			findPreference(SectionKeymap.ITEM_ADD_WORD),
			findPreference(SectionKeymap.ITEM_BACKSPACE),
			findPreference(SectionKeymap.ITEM_COMMAND_PALETTE),
			findPreference(SectionKeymap.ITEM_EDIT_TEXT),
			findPreference(SectionKeymap.ITEM_FILTER_CLEAR),
			findPreference(SectionKeymap.ITEM_FILTER_SUGGESTIONS),
			findPreference(SectionKeymap.ITEM_PREVIOUS_SUGGESTION),
			findPreference(SectionKeymap.ITEM_NEXT_SUGGESTION),
			findPreference(SectionKeymap.ITEM_NEXT_INPUT_MODE),
			findPreference(SectionKeymap.ITEM_NEXT_LANGUAGE),
			findPreference(SectionKeymap.ITEM_SELECT_KEYBOARD),
			findPreference(SectionKeymap.ITEM_SHOW_SETTINGS),
			findPreference(SectionKeymap.ITEM_VOICE_INPUT),
		};
		SectionKeymap section = new SectionKeymap(Arrays.asList(dropDowns), activity);
		section.populate().activate();

		(new ItemResetKeys(findPreference(ItemResetKeys.NAME), activity, section))
			.enableClickHandler();

		resetFontSize(false);
	}
}
