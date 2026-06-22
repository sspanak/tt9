package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.screens.ScreenWithPreviewKeyboardHeaderFragment;

public class AppearanceUnfoldedScreen extends ScreenWithPreviewKeyboardHeaderFragment {
	public static final String NAME = "AppearanceUnfolded";

	public AppearanceUnfoldedScreen() { super(); }
	public AppearanceUnfoldedScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_unfolded_appearance; }
	@Override protected int getXml() { return R.xml.prefs_screen_appearance_unfolded; }

	@Override
	protected void onCreate() {
		createGeometrySection();
		createKeysSection();
		resetFontSize(false);
	}


	private void createGeometrySection() {
		if (activity == null) {
			return;
		}

		DropDownAlignmentUnfolded alignment = findPreference(DropDownAlignmentUnfolded.NAME);
		DropDownBottomPaddingPortraitUnfolded bottomPadding = findPreference(DropDownBottomPaddingPortraitUnfolded.NAME);
		DropDownNumpadFnKeyScaleUnfolded fnKeyScale = findPreference(DropDownNumpadFnKeyScaleUnfolded.NAME);
		DropDownSuggestionFontSizeUnfolded fontSize = findPreference(DropDownSuggestionFontSizeUnfolded.NAME);
		DropDownKeyHeightUnfolded height = findPreference(DropDownKeyHeightUnfolded.NAME);
		DropDownNumpadKeyFontSizeUnfolded keyFontSize = findPreference(DropDownNumpadKeyFontSizeUnfolded.NAME);
		DropDownWidthUnfolded width = findPreference(DropDownWidthUnfolded.NAME);

		EnhancedDropDownPreference[] dropdowns = {
			alignment,
			bottomPadding,
			fnKeyScale,
			fontSize,
			height,
			keyFontSize,
			width
		};

		for (EnhancedDropDownPreference item : dropdowns) {
			if (item != null) {
				item.populate(activity.getSettings()).preview();
			}
		}
	}


	private void createKeysSection() {
		if (activity == null) {
			return;
		}
	}

}
