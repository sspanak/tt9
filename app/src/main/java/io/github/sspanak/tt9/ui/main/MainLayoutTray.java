package io.github.sspanak.tt9.ui.main;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutTray extends BaseMainLayout {
	MainLayoutTray(TraditionalT9 tt9) {
		super(tt9, R.layout.main_small);
	}

	protected void setSoftKeysVisibility() {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
		}
	}

	void showCommandPalette() {
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
	}

	void hideCommandPalette() {
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.GONE);
		if (this instanceof MainLayoutSmall) {
			view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.VISIBLE);
		}
	}

	boolean isCommandPaletteShown() {
		return view.findViewById(R.id.main_command_keys).getVisibility() == LinearLayout.VISIBLE;
	}

	protected Drawable getBackgroundColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getDrawable(
			contextView.getContext(),
			dark ? R.drawable.button_background_dark : R.drawable.button_background
		);
	}

	protected Drawable getSeparatorColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getDrawable(
			contextView.getContext(),
			dark ? R.drawable.button_separator_dark : R.drawable.button_separator
		);
	}

	@Override
	void setDarkTheme(boolean dark) {
		if (view == null) {
			return;
		}

		// background
		view.findViewById(R.id.main_command_keys).setBackground(getBackgroundColor(view, dark));

		// text
		for (SoftKey key : getKeys()) {
			key.setDarkTheme(dark);
		}

		// separators
		for (View separator : getSeparators()) {
			if (separator != null) {
				separator.setBackground(getSeparatorColor(separator, dark));
			}
		}
	}

	@Override
	void render() {
		getView();
		enableClickHandlers();
		setSoftKeysVisibility();
		for (SoftKey key : getKeys()) {
			key.render();
		}
	}

	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (view != null && keys.isEmpty()) {
			keys.addAll(getKeysFromContainer(view.findViewById(R.id.main_command_keys)));
		}
		return keys;
	}

	protected ArrayList<View> getSeparators() {
		return new ArrayList<>(Arrays.asList(
			view.findViewById(R.id.separator_top),
			view.findViewById(R.id.separator_candidates_bottom),
			view.findViewById(R.id.separator_2_1),
			view.findViewById(R.id.separator_2_2),
			view.findViewById(R.id.separator_3_1)
		));
	}
}
