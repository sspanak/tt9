package io.github.sspanak.tt9.preferences.screens.appearanceUnfolded;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.screens.ScreenWithPreviewKeyboardHeaderFragment;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class AppearanceUnfoldedScreen extends ScreenWithPreviewKeyboardHeaderFragment {
	public static final String NAME = "AppearanceUnfolded";

	public AppearanceUnfoldedScreen() { super(); }
	public AppearanceUnfoldedScreen(@Nullable PreferencesActivity activity) { super(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_unfolded_appearance; }
	@Override protected int getXml() { return R.xml.prefs_screen_appearance_unfolded; }

	@Override
	protected void onCreate() {
		super.onCreate();
		if (activity != null) {
			EnhancedDropDownPreference[] dropdowns = getDropDowns();
			initOptions(activity.getSettings(), dropdowns);
			enablePreviewOnChange(dropdowns);
		}
		resetFontSize(false);
	}


	@NonNull private EnhancedDropDownPreference[] getDropDowns() {
		return new EnhancedDropDownPreference[] {
			findPreference(DropDownAlignmentUnfolded.NAME),
			findPreference(DropDownBottomPaddingPortraitUnfolded.NAME),
			findPreference(DropDownNumpadFnKeyScaleUnfolded.NAME),
			findPreference(DropDownSuggestionFontSizeUnfolded.NAME),
			findPreference(DropDownKeyHeightUnfolded.NAME),
			findPreference(DropDownNumpadKeyFontSizeUnfolded.NAME),
			findPreference(DropDownWidthUnfolded.NAME)
		};
	}


	private void initOptions(@NonNull SettingsStore settings, @NonNull EnhancedDropDownPreference[] dropdowns) {
		for (EnhancedDropDownPreference item : dropdowns) {
			if (item != null) {
				item.populate(settings).preview();
			}
		}
	}


	private void enablePreviewOnChange(@NonNull EnhancedDropDownPreference[] dropdowns) {
		for (EnhancedDropDownPreference dropdown : dropdowns) {
			if (dropdown != null) {
				dropdown.setOnChangeListener(this::previewDropDownChange);
			}
		}
	}


	private void previewDropDownChange(String s) {
		previewKeyboard();
	}


	@Override
	protected void onBeforePreview() {
		if (activity != null) {
			SettingsStore.isFoldedPreview = false;
		}

		super.onBeforePreview();
	}
}
