package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;

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
			height = resources.getDimensionPixelSize(R.dimen.status_bar_height);

			if (isCommandPaletteShown() || isTextEditingPaletteShown()) {
				height += resources.getDimensionPixelSize(R.dimen.main_small_command_palette_height);
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
		getHeight(true);
	}

	void showKeyboard() {
		view.findViewById(R.id.text_editing_container).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.GONE);
		getHeight(true);
	}

	@Override
	void showTextEditingPalette() {
		view.findViewById(R.id.main_command_keys).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.main_soft_keys).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.text_editing_container).setVisibility(LinearLayout.VISIBLE);
		getHeight(true);
	}

	@Override
	boolean isCommandPaletteShown() {
		return view != null && view.findViewById(R.id.main_command_keys).getVisibility() == LinearLayout.VISIBLE;
	}

	@Override
	boolean isTextEditingPaletteShown() {
		return view != null && view.findViewById(R.id.text_editing_container).getVisibility() == LinearLayout.VISIBLE;
	}

	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (view != null && keys.isEmpty()) {
			keys.addAll(getKeysFromContainer(view.findViewById(R.id.main_command_keys)));
			keys.addAll(getKeysFromContainer(view.findViewById(R.id.text_editing_container)));
		}
		return keys;
	}

	@Override
	void render() {
		getView();
		setSoftKeysVisibility();
		preventEdgeToEdge();
		setWidth(tt9.getSettings().getWidthPercent(), tt9.getSettings().getAlignment());
		setBackgroundBlending();
		enableClickHandlers();
		renderKeys();
	}
}
