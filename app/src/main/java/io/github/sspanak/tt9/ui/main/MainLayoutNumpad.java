package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;

class MainLayoutNumpad extends BaseMainLayout {
	private int height;

	MainLayoutNumpad(TraditionalT9 tt9) {
		super(tt9, R.layout.main_numpad);
	}

	int getHeight() {
		if (height <= 0) {
			Resources resources = tt9.getResources();
			height =
				resources.getDimensionPixelSize(R.dimen.soft_key_height) * 5
				+ resources.getDimensionPixelSize(R.dimen.numpad_candidate_height)
				+ resources.getDimensionPixelSize(R.dimen.numpad_padding_bottom) * 4;
		}

		return height;
	}

	private int getBackgroundColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getColor(
			contextView.getContext(),
			dark ? R.color.dark_numpad_background : R.color.numpad_background
		);
	}


	private int getSeparatorColor(@NonNull View contextView, boolean dark) {
		return ContextCompat.getColor(
			contextView.getContext(),
			dark ? R.color.dark_numpad_separator : R.color.numpad_separator
		);
	}

	@Override
	void setDarkTheme(boolean dark) {
		if (view == null) {
			return;
		}

		// background
		view.setBackgroundColor(getBackgroundColor(view, dark));

		// text
		for (SoftKey key : getKeys()) {
			key.setDarkTheme(dark);
		}

		// separators
		int separatorColor = getSeparatorColor(view, dark);
		for (View separator : getSeparators()) {
			if (separator != null) {
				separator.setBackgroundColor(separatorColor);
			}
		}
	}

	@Override
	void render() {
		getView();
		enableClickHandlers();
		for (SoftKey key : getKeys()) {
			key.render();
		}
	}

	@NonNull
	@Override
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

		ViewGroup statusBarContainer = view.findViewById(R.id.status_bar_container);
		keys.addAll(getKeysFromContainer(statusBarContainer));

		return keys;
	}

	protected ArrayList<View> getSeparators() {
		// it's fine... it's shorter, faster and easier to read than searching with 3 nested loops
		return new ArrayList<>(Arrays.asList(
			view.findViewById(R.id.separator_top),
			view.findViewById(R.id.separator_candidates_1),
			view.findViewById(R.id.separator_candidates_2),
			view.findViewById(R.id.separator_candidates_bottom),
			view.findViewById(R.id.separator_1_1),
			view.findViewById(R.id.separator_1_2),
			view.findViewById(R.id.separator_2_1),
			view.findViewById(R.id.separator_2_2),
			view.findViewById(R.id.separator_3_1),
			view.findViewById(R.id.separator_3_2),
			view.findViewById(R.id.separator_4_1),
			view.findViewById(R.id.separator_4_2)
		));
	}
}
