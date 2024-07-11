package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
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
	protected int height;

	MainLayoutTray(TraditionalT9 tt9) {
		super(tt9, R.layout.main_small);
	}

	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();
			height = resources.getDimensionPixelSize(R.dimen.candidate_height);

			if (isCommandPaletteShown() || isTextEditingPaletteShown()) {
				height += resources.getDimensionPixelSize(R.dimen.numpad_key_height);
			}
		}

		return height;
	}

	protected void setSoftKeysVisibility() {
		if (view != null) {
			view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
		}
	}

	void showCommandPalette() {
		view.findViewById(R.id.text_editing_container).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.VISIBLE);

		height = 0;
		getHeight();
	}

	void hideCommandPalette() {
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.GONE);

		height = 0;
		getHeight();
	}

	boolean isCommandPaletteShown() {
		return view != null && view.findViewById(R.id.main_command_keys).getVisibility() == LinearLayout.VISIBLE;
	}

	@Override
	void showTextEditingPalette() {
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.text_editing_container).setVisibility(LinearLayout.VISIBLE);

		height = 0;
		getHeight();
	}

	@Override
	void hideTextEditingPalette() {
		view.findViewById(R.id.text_editing_container).setVisibility(LinearLayout.GONE);

		height = 0;
		getHeight();
	}

	@Override
	boolean isTextEditingPaletteShown() {
		return view != null && view.findViewById(R.id.text_editing_container).getVisibility() == LinearLayout.VISIBLE;
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
		view.findViewById(R.id.text_editing_container).setBackground(getBackgroundColor(view, dark));

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
			keys.addAll(getKeysFromContainer(view.findViewById(R.id.text_editing_keys_small)));
		}
		return keys;
	}

	protected ArrayList<View> getSeparators() {
		return new ArrayList<>(Arrays.asList(
			view.findViewById(R.id.separator_top),
			view.findViewById(R.id.separator_candidates_bottom),
			view.findViewById(R.id.separator_1_1),
			view.findViewById(R.id.separator_1_2),
			view.findViewById(R.id.separator_2_1),
			view.findViewById(R.id.separator_2_2),
			view.findViewById(R.id.separator_10_1),
			view.findViewById(R.id.separator_10_2),
			view.findViewById(R.id.separator_10_2),
			view.findViewById(R.id.separator_10_3),
			view.findViewById(R.id.separator_10_4),
			view.findViewById(R.id.separator_10_5),
			view.findViewById(R.id.separator_10_6)
		));
	}
}
