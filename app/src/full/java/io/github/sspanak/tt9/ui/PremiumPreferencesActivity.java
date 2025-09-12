package io.github.sspanak.tt9.ui;

import androidx.annotation.Nullable;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;

/**
 * Implemented in the "premium" source set. The open-source version
 * has no premium features, so this class has only minimal functionality.
 */
public class PremiumPreferencesActivity extends ActivityWithNavigation {
	protected BaseScreenFragment getScreen(PreferencesActivity prefsActivity, @Nullable String ignored) {
		return null;
	}
}
