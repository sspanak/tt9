package io.github.sspanak.tt9.ui.main;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.TraditionalT9;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.preferences.settings.SettingsVirtualNumpad;
import io.github.sspanak.tt9.ui.main.keys.SoftKey;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyArrow;
import io.github.sspanak.tt9.ui.main.keys.SoftKeyText;
import io.github.sspanak.tt9.util.Logger;

class MainLayoutNumpad extends MainLayoutClassic {
	private static final String LOG_TAG = MainLayoutNumpad.class.getSimpleName();

	@NonNull private String lastFnKeyOrder = "";
	private int lastLFnWrapperId = -1;
	private int lastRFnWrapperId = -1;


	MainLayoutNumpad(TraditionalT9 tt9) {
		super(tt9, R.layout.main_numpad);
	}


	@Override void showCommandPalette() {}
	@Override boolean isCommandPaletteShown() { return false; }


	@Override
	protected int[] calculateKeyHeight() {
		final int defaultHeight = super.calculateKeyHeight()[0];

		// in case some of the Fn keys are hidden, we need to stretch the rest
		final int lfnCount = tt9.getSettings().getLfnKeyOrder().length();
		final int lfnHeight = lfnCount > 0 ? defaultHeight * 4 / lfnCount : defaultHeight;

		final int rfnCount = tt9.getSettings().getRfnKeyOrder().length();
		final int rfnHeight = rfnCount > 0 ? defaultHeight * 4 / rfnCount : defaultHeight;

		return new int[] {defaultHeight, lfnHeight, rfnHeight};
	}

	@Override
	protected int getTextKeyHeight(int keyHeight) {
		return tt9.getSettings().isNumpadShapeV() ? Math.round(keyHeight * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_INNER) : keyHeight;
	}

	private int getLastSideKeyHeight(int keyHeight) {
		return tt9.getSettings().isNumpadShapeV() ? Math.round(keyHeight * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_OUTER) : keyHeight;
	}


	@Override
	protected void setKeyHeight(int defaultHeight, int leftHeight, int rightHeight) {
		if (view == null || defaultHeight <= 0) {
			return;
		}

		final int textKeyHeight = getTextKeyHeight(defaultHeight);

		final View leftColumn = view.findViewById(R.id.numpad_column_fn_left);
		final View rightColumn = view.findViewById(R.id.numpad_column_fn_right);

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeyArrow) {
				continue;
			}

			if (key instanceof SoftKeyText) {
				key.setHeight(textKeyHeight);
				continue;
			}

			final View wrapper = (View) key.getParent();
			final View container = wrapper != null ? (View) wrapper.getParent() : null;
			final boolean isLastInColumn = wrapper != null && (wrapper.getId() == lastLFnWrapperId || wrapper.getId() == lastRFnWrapperId);

			if (container != null && container.equals(leftColumn)) {
				key.setHeight(isLastInColumn ? getLastSideKeyHeight(leftHeight) : leftHeight);
			}	else if (container != null && container.equals(rightColumn)) {
				key.setHeight(isLastInColumn ? getLastSideKeyHeight(rightHeight) : rightHeight);
			} else {
				key.setHeight(defaultHeight);
			}
		}
	}


	private void setKeyColumnWidth(float layoutWeight) {
		if (view == null || layoutWeight <= 0) {
			return;
		}

		LinearLayout leftColumn = view.findViewById(R.id.numpad_column_fn_left);
		LinearLayout rightColumn = view.findViewById(R.id.numpad_column_fn_right);

		LinearLayout.LayoutParams leftParams = leftColumn != null ? (LinearLayout.LayoutParams) leftColumn.getLayoutParams() : null;
		LinearLayout.LayoutParams rightParams = rightColumn != null ? (LinearLayout.LayoutParams) rightColumn.getLayoutParams() : null;
		if (leftParams == null || rightParams == null) {
			return;
		}

		leftParams.weight = layoutWeight;
		rightParams.weight = layoutWeight;
		leftColumn.setLayoutParams(leftParams);
		rightColumn.setLayoutParams(rightParams);
	}


	@Override
	protected int getKeyColumnHeight(int keyHeight) {
		return keyHeight * 3 + getLastSideKeyHeight(keyHeight);
	}


	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (!keys.isEmpty() || view == null) {
			return keys;
		}

		addNumericKeys();

		// status bar
		keys.addAll(getKeysFromContainer(view.findViewById(R.id.status_bar_container)));

		// left Fn
		addKey(R.id.soft_key_settings);
		addKey(R.id.soft_key_add_word);
		addKey(R.id.soft_key_shift);
		addKey(R.id.soft_key_lf4);

		// right Fn
		addKey(R.id.soft_key_numpad_backspace);
		addKey(R.id.soft_key_filter);
		addKey(R.id.soft_key_rf3);
		addKey(R.id.soft_key_numpad_ok);

		return keys;
	}


	private void reorderFnKeys() {
		String lfnOrder = tt9.getSettings().getLfnKeyOrder();
		String rfnOrder = tt9.getSettings().getRfnKeyOrder();

		final String newOrder = lfnOrder + "," + rfnOrder;
		if (newOrder.equals(lastFnKeyOrder)) {
			Logger.d(LOG_TAG, "Preserving current key order: '" + lastFnKeyOrder + "'");
			return;
		}

		if (view == null) {
			return;
		}

		ViewGroup left = view.findViewById(R.id.numpad_column_fn_left);
		ViewGroup right = view.findViewById(R.id.numpad_column_fn_right);
		if (left == null || right == null) {
			Logger.w(LOG_TAG, "Reordering keys failed: left or right column is null");
			return;
		}

		Map<Integer, View> keyWrappers = new HashMap<>();
		for (Map.Entry<Character, Integer> entry : SettingsVirtualNumpad.KEY_ORDER_MAP.entrySet()) {
			keyWrappers.put(entry.getValue(), view.findViewById(entry.getValue()));
		}

		hideUnusedFnKeys(keyWrappers, lfnOrder, rfnOrder);
		lastLFnWrapperId = reorderFnColumn(left, lfnOrder, keyWrappers);
		lastRFnWrapperId = reorderFnColumn(right, rfnOrder, keyWrappers);

		lastFnKeyOrder = newOrder;
		Logger.d(LOG_TAG, "Reordered keys: '" + lastFnKeyOrder + "'");
	}


	private void hideUnusedFnKeys(@NonNull Map<Integer, View> keyWrappers, @NonNull String leftOrder, @NonNull String rightOrder) {
		for (Map.Entry<Integer, View> entry : keyWrappers.entrySet()) {
			Integer keyId = entry.getKey();
			View key = entry.getValue();

			if (key != null && leftOrder.indexOf(keyId) < 0 && rightOrder.indexOf(keyId) < 0) {
				key.setVisibility(View.GONE);
			}
		}

		View leftColumn = view.findViewById(R.id.numpad_column_fn_left);
		if (leftColumn != null) {
			leftColumn.setVisibility(leftOrder.isEmpty() ? View.GONE : View.VISIBLE);
		}

		View rightColumn = view.findViewById(R.id.numpad_column_fn_right);
		if (rightColumn != null) {
			rightColumn.setVisibility(rightOrder.isEmpty() ? View.GONE : View.VISIBLE);
		}
	}


	private int reorderFnColumn(@NonNull ViewGroup column, @NonNull String order, @NonNull Map<Integer, View> keyWrappers) {
		Integer viewId = null;

		for (char keyId : order.toCharArray()) {
			viewId = SettingsVirtualNumpad.KEY_ORDER_MAP.get(keyId);
			if (viewId == null) {
				continue;
			}

			View key = keyWrappers.get(viewId);
			if (key == null) {
				Logger.w(LOG_TAG, "Failed reordering a NULL key with expected ID: " + keyId);
				continue;
			}

			((ViewGroup) key.getParent()).removeView(key);
			column.addView(key);
			key.setVisibility(View.VISIBLE);
		}

		return viewId != null ? viewId : -1;
	}


	@Override
	protected void beforeRender() {
		reorderFnKeys();
		setKeyColumnWidth(tt9.getSettings().getNumpadFnKeyScale());
	}
}
