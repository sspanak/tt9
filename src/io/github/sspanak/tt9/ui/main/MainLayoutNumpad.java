package io.github.sspanak.tt9.ui.main;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutNumpad extends BaseMainLayout {
	public MainLayoutNumpad(TraditionalT9 tt9) {
		super(tt9, R.layout.main_numpad);
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
	}

	@Override
	public void render() {
		getView();
		enableClickHandlers();
		for (SoftKey key : getKeys()) {
			key.render();
		}
	}

	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (keys != null && keys.size() > 0) {
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
}
