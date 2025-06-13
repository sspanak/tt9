package io.github.sspanak.tt9.ui.main;

import android.content.res.Resources;
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
import io.github.sspanak.tt9.ui.main.keys.SoftKeySettings;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

class MainLayoutNumpad extends BaseMainLayout {
	private static final String LOG_TAG = MainLayoutNumpad.class.getSimpleName();

	@NonNull private String lastFnKeyOrder = "";
	private int height;
	private boolean isTextEditingShown = false;


	MainLayoutNumpad(TraditionalT9 tt9) {
		super(tt9, R.layout.main_numpad);
		dynamicKeys.add(R.id.soft_key_filter);
		dynamicKeys.add(R.id.soft_key_shift);
	}


	@Override void showCommandPalette() {}
	@Override void hideCommandPalette() {}
	@Override boolean isCommandPaletteShown() { return false; }


	@Override
	void showTextEditingPalette() {
		isTextEditingShown = true;

		view.findViewById(R.id.numpad_column_1).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_2).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_3).setVisibility(LinearLayout.GONE);

		view.findViewById(R.id.numpad_column_101).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_102).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_103).setVisibility(LinearLayout.VISIBLE);

		renderKeys();
	}

	@Override
	void hideTextEditingPalette() {
		isTextEditingShown = false;

		view.findViewById(R.id.numpad_column_1).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_2).setVisibility(LinearLayout.VISIBLE);
		view.findViewById(R.id.numpad_column_3).setVisibility(LinearLayout.VISIBLE);

		view.findViewById(R.id.numpad_column_101).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_102).setVisibility(LinearLayout.GONE);
		view.findViewById(R.id.numpad_column_103).setVisibility(LinearLayout.GONE);

		renderKeys();
	}

	@Override
	boolean isTextEditingPaletteShown() {
		return isTextEditingShown;
	}


	/**
	 * Uses the key height from the settings, but if the keyboard takes up too much screen space, it
	 * will be adjusted limited to 60% of the screen height in landscape mode and 75% in portrait mode.
	 * This prevents Android from auto-closing the keyboard in some apps that have a lot of content.
	 * Returns the adjusted height of a single key.
	 */
	private int[] calculateKeyHeight() {
		final boolean isLandscape = DeviceInfo.isLandscapeOrientation(tt9.getApplicationContext());

		int bottomPadding = 0;
		if (DeviceInfo.AT_LEAST_ANDROID_15) {
			bottomPadding = isLandscape ? e2ePaddingBottomLandscape : e2ePaddingBottomPortrait;
			bottomPadding = bottomPadding < 0 ? DeviceInfo.getNavigationBarHeight(tt9.getApplicationContext(), tt9.getSettings(), isLandscape) : bottomPadding;
		}

		final int screenHeight = DeviceInfo.getScreenHeight(tt9.getApplicationContext()) - bottomPadding;
		final double maxScreenHeight = isLandscape ? screenHeight * 0.6 : screenHeight * 0.75;
		final int maxKeyHeight = (int) Math.round(maxScreenHeight / 5);

		final int defaultHeight = Math.min(tt9.getSettings().getNumpadKeyHeight(), maxKeyHeight);

		// in case some of the Fn keys are hidden, we need to stretch the rest
		final int lfnCount = tt9.getSettings().getLfnKeyOrder().length();
		final int lfnHeight = lfnCount > 0 ? defaultHeight * 4 / lfnCount : defaultHeight;

		final int rfnCount = tt9.getSettings().getRfnKeyOrder().length();
		final int rfnHeight = rfnCount > 0 ? defaultHeight * 4 / rfnCount : defaultHeight;

		return new int[] {defaultHeight, lfnHeight, rfnHeight};
	}


	private void setKeyHeight(int defaultHeight, int leftHeight, int rightHeight) {
		if (view == null || defaultHeight <= 0) {
			return;
		}

		final View leftColumn = view.findViewById(R.id.numpad_column_fn_left);
		final View rightColumn = view.findViewById(R.id.numpad_column_fn_right);

		for (SoftKey key : getKeys()) {
			final View wrapper = (View) key.getParent();
			final View container = wrapper != null ? (View) wrapper.getParent() : null;

			if (container != null && container.equals(leftColumn)) {
				key.setHeight(leftHeight);
			}	else if (container != null && container.equals(rightColumn)) {
				key.setHeight(rightHeight);
			} else {
				key.setHeight(defaultHeight);
			}
		}
	}


	private int getKeyColumnHeight(int keyHeight) {
		int lastKeyHeight = tt9.getSettings().isNumpadShapeV() ? Math.round(keyHeight * SettingsStore.SOFT_KEY_V_SHAPE_RATIO_OUTER) : keyHeight;
		return keyHeight * 3 + lastKeyHeight;
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


	int getHeight(boolean forceRecalculate) {
		if (height <= 0 || forceRecalculate) {
			Resources resources = tt9.getResources();

			height =
				Math.round(resources.getDimension(R.dimen.numpad_status_bar_spacing_top))
				+ resources.getDimensionPixelSize(R.dimen.numpad_status_bar_spacing_bottom)
				+ resources.getDimensionPixelSize(R.dimen.numpad_suggestion_height)
				+ getKeyColumnHeight(calculateKeyHeight()[0])
				+ Math.round(resources.getDimension(R.dimen.numpad_keys_spacing_bottom));
		}

		return height;
	}


	private void showLongSpace(boolean yes, int keyHeight) {
		LinearLayout longSpacePanel = view != null ? view.findViewById(R.id.panel_long_spacebar) : null;
		if (longSpacePanel != null) {
			longSpacePanel.setVisibility(yes ? LinearLayout.VISIBLE : LinearLayout.GONE);
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) longSpacePanel.getLayoutParams();
			params.height = keyHeight;
			longSpacePanel.setLayoutParams(params);
		}
	}


	@Override
	protected void enableClickHandlers() {
		super.enableClickHandlers();

		for (SoftKey key : getKeys()) {
			if (key instanceof SoftKeySettings) {
				((SoftKeySettings) key).setMainView(tt9.getMainView());
			}
		}
	}


	@NonNull
	@Override
	protected ArrayList<SoftKey> getKeys() {
		if (!keys.isEmpty() || view == null) {
			return keys;
		}

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

		// digits panel
		ViewGroup table = view.findViewById(R.id.main_soft_keys);
		addKey(R.id.soft_key_0, table);
		addKey(R.id.soft_key_1, table);
		addKey(R.id.soft_key_2, table);
		addKey(R.id.soft_key_3, table);
		addKey(R.id.soft_key_4, table);
		addKey(R.id.soft_key_5, table);
		addKey(R.id.soft_key_6, table);
		addKey(R.id.soft_key_7, table);
		addKey(R.id.soft_key_8, table);
		addKey(R.id.soft_key_9, table);
		addKey(R.id.soft_key_punctuation_1, table);
		addKey(R.id.soft_key_punctuation_2, table);

		// text editing panel
		addKey(R.id.soft_key_100, table);
		addKey(R.id.soft_key_101, table);
		addKey(R.id.soft_key_102, table);
		addKey(R.id.soft_key_103, table);
		addKey(R.id.soft_key_104, table);
		addKey(R.id.soft_key_105, table);
		addKey(R.id.soft_key_106, table);
		addKey(R.id.soft_key_107, table);
		addKey(R.id.soft_key_108, table);
		addKey(R.id.soft_key_109, table);
		addKey(R.id.soft_key_punctuation_101, table);
		addKey(R.id.soft_key_punctuation_102, table);

		// Long space panel
		addKey(R.id.soft_key_200, table);
		addKey(R.id.soft_key_punctuation_201, table);
		addKey(R.id.soft_key_punctuation_202, table);

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
		reorderFnColumn(left, lfnOrder, keyWrappers);
		reorderFnColumn(right, rfnOrder, keyWrappers);

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


	private void reorderFnColumn(@NonNull ViewGroup column, @NonNull String order, @NonNull Map<Integer, View> keyWrappers) {
		for (char keyId : order.toCharArray()) {
			Integer viewId = SettingsVirtualNumpad.KEY_ORDER_MAP.get(keyId);
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
	}


	@Override
	void render() {
		final int[] keyHeights = calculateKeyHeight();

		getView();
		reorderFnKeys();
		enableClickHandlers();
		setKeyHeight(keyHeights[0], keyHeights[1], keyHeights[2]);
		preventEdgeToEdge();
		setWidth(tt9.getSettings().getWidthPercent(), tt9.getSettings().getAlignment());
		setKeyColumnWidth(tt9.getSettings().getNumpadFnKeyScale());
		setBackgroundBlending();

		boolean hasLettersOnAllKeys = tt9.getLanguage() != null && tt9.getLanguage().hasLettersOnAllKeys();
		showLongSpace(
			tt9.getSettings().isNumpadShapeLongSpace() && !tt9.isInputModeNumeric() && !hasLettersOnAllKeys,
			keyHeights[0]
		);

		renderKeys();
	}
}
