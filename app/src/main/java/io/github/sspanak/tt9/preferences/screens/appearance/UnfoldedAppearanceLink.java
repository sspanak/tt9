package io.github.sspanak.tt9.preferences.screens.appearance;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.appearanceUnfolded.AppearanceUnfoldedScreen;
import io.github.sspanak.tt9.util.sys.HardwareInfo;

public class UnfoldedAppearanceLink extends Preference {
	public static final String NAME = "UnfoldedAppearanceLink";

	private PreferencesActivity activity;

	public UnfoldedAppearanceLink(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	public UnfoldedAppearanceLink(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public UnfoldedAppearanceLink(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public UnfoldedAppearanceLink(@NonNull Context context) {
		super(context);
		init(context);
	}


	protected void init(@NonNull Context context) {
		setKey(NAME);
		if (HardwareInfo.isFoldable(context)) {
			setSummary(R.string.pref_category_unfolded_appearance_summary);
			setTitle(R.string.pref_category_unfolded_appearance);
			setVisible(true);
		} else {
			setVisible(false);
		}
	}


	@Override
	protected void onClick() {
		if (activity != null) {
			activity.displayScreen(AppearanceUnfoldedScreen.NAME);
			super.onClick();
		}
	}


	public UnfoldedAppearanceLink populate(@NonNull PreferencesActivity activity) {
		this.activity = activity;
		return this;
	}
}
