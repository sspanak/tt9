package io.github.sspanak.tt9.ui.main;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

public class MainLayoutCommandPalette extends BaseMainLayout {
	MainLayoutCommandPalette(TraditionalT9 tt9) {
		super(tt9, R.layout.main_command_palette);
	}

	MainLayoutCommandPalette(TraditionalT9 tt9, int layoutId) {
		super(tt9, layoutId);
	}

	@Override
	public void setDarkTheme(boolean darkEnabled) {
		if (view == null) {
			return;
		}

		// background
		view.setBackground(ContextCompat.getDrawable(
			view.getContext(),
			darkEnabled ? R.color.dark_numpad_background : R.color.numpad_background
		));

		// text
		for (SoftKey key : getKeys()) {
			key.setDarkTheme(darkEnabled);
		}

		// separators
		int separatorColor = ContextCompat.getColor(
			view.getContext(),
			darkEnabled ? R.color.dark_numpad_separator : R.color.numpad_separator
		);

		for (View separator : getSeparators()) {
			if (separator != null) {
				separator.setBackgroundColor(separatorColor);
			}
		}
	}

	@Override
	public void render() {
		getView();
		enableClickHandlers();
		for (SoftKey key : getKeys()) {
			key.render();
		}
	}

	protected ArrayList<SoftKey> getKeys() {
		if (!keys.isEmpty()) {
			return keys;
		}

		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		int tableRowsCount = table.getChildCount();

		for (int rowId = 0; rowId < tableRowsCount; rowId++) {
			View row = table.getChildAt(rowId);
			if (row instanceof ViewGroup) {
				keys.addAll(getKeysFromContainer((ViewGroup) row));
			}
		}

		return keys;
	}

	protected ArrayList<View> getSeparators() {
		return new ArrayList<>(Arrays.asList(
			view.findViewById(R.id.separator_top),
			view.findViewById(R.id.separator_candidates_bottom)
		));
	}
}
