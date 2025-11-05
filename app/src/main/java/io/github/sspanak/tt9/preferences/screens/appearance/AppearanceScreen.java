package io.github.sspanak.tt9.preferences.screens.appearance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.custom.EnhancedDropDownPreference;
import io.github.sspanak.tt9.preferences.custom.KeyboardPreviewSwitchPreference;
import io.github.sspanak.tt9.preferences.items.ItemSwitch;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

public class AppearanceScreen extends BaseScreenFragment {
	final public static String NAME = "Appearance";

	private KeyboardPreviewSwitchPreference preview;

	public AppearanceScreen() { init(); }
	public AppearanceScreen(PreferencesActivity activity) { init(activity); }

	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.pref_category_appearance; }
	@Override protected int getXml() { return R.xml.prefs_screen_appearance; }

	@Override
	protected void onCreate() {
		createMainSection();
		createHacksSection();
		enablePreviewOnChange();
		resetFontSize(true);
	}


	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// The preview switch is not accessible on devices without touch screen, so omit it.
		if (DeviceInfo.noTouchScreen(activity)) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}

		View root = inflater.inflate(R.layout.prefs_screen_with_preview_header, container, false);

		preview = new KeyboardPreviewSwitchPreference(activity);
		preview.bindView(root.findViewById(R.id.static_preview_header));

		View prefs = super.onCreateView(inflater, container, savedInstanceState);
		FrameLayout prefsContainer = root.findViewById(R.id.preferences_container);
		if (prefsContainer != null) {
			prefsContainer.addView(prefs);
		}

		return root;
	}


	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (preview != null) {
			preview.stop();
		}
		if (activity != null) {
			activity.getSettings().reloadColorScheme(); // clear any invalid preview cache
		}
	}


	@Override
	public void onPause() {
		super.onPause();
		if (preview != null) {
			preview.stop();
		}
		if (activity != null) {
			activity.getSettings().reloadColorScheme(); // clear any invalid preview cache
		}
	}


	protected void createMainSection() {
		(new ItemStatusIcon(findPreference(ItemStatusIcon.NAME), activity.getSettings())).populate();
		(new ItemDragResize(findPreference(ItemDragResize.NAME), activity.getSettings())).populate();
		(new ItemSuggestionSmoothScroll(findPreference(ItemSuggestionSmoothScroll.NAME), activity.getSettings())).populate();

		DropDownAlignment alignment = findPreference(DropDownAlignment.NAME);
		DropDownKeyHeight numpadKeyHeight = findPreference(DropDownKeyHeight.NAME);
		DropDownWidth keyboardWidth = findPreference(DropDownWidth.NAME);
		DropDownNumpadShape numpadShape = findPreference(DropDownNumpadShape.NAME);
		ItemShowArrows showArrows = new ItemShowArrows(findPreference(ItemShowArrows.NAME), activity.getSettings());
		DropDownNumpadFnKeyScale fnKeyWidth = findPreference(DropDownNumpadFnKeyScale.NAME);
		DropDownNumpadKeyFontSize numpadKeyFontSize = findPreference(DropDownNumpadKeyFontSize.NAME);
		DropDownSuggestionFontSize suggestionFontSize = findPreference(DropDownSuggestionFontSize.NAME);
		ItemFnKeyOrder fnKeyOrder = new ItemFnKeyOrder(activity.getSettings(), findPreference(ItemFnKeyOrder.NAME));

		DropDownLayoutType selectLayout = findPreference(DropDownLayoutType.NAME);
		if (selectLayout != null) {
			selectLayout
				.addOnChangeItem(alignment)
				.addOnChangeItem(fnKeyOrder)
				.addOnChangeItem(fnKeyWidth)
				.addOnChangeItem(keyboardWidth)
				.addOnChangeItem(numpadKeyFontSize)
				.addOnChangeItem(numpadKeyHeight)
				.addOnChangeItem(numpadShape)
				.addOnChangeItem(showArrows)
				.addOnChangeItem(suggestionFontSize)
				.addOnChangePreference(findPreference(PrecalculateNavbarHeightSwitch.NAME))
				.addOnChangePreference(findPreference("pref_alternative_suggestion_scrolling"))
				.addOnChangePreference(findPreference("pref_clear_insets"))
				.addOnChangePreference(findPreference("pref_drag_resize"))
				.addOnChangePreference(findPreference("pref_suggestion_smooth_scroll"));
		}

		EnhancedDropDownPreference[] items = {
			findPreference(DropDownSettingsFontSize.NAME),
			selectLayout,
			numpadKeyHeight,
			alignment,
			keyboardWidth,
			numpadShape,
			fnKeyWidth,
			numpadKeyFontSize,
			suggestionFontSize
		};

		for (EnhancedDropDownPreference item : items) {
			if (item instanceof DropDownSettingsFontSize) {
				((DropDownSettingsFontSize) item).setScreen(this);
			} // not else-if, we want both!
			if (item != null) {
				item.populate(activity.getSettings()).preview();
			}
		}

		showArrows.populate();
	}


	private void createHacksSection() {
		ItemSwitch[] items = {
			new ItemAlternativeSuggestionScrolling(findPreference(ItemAlternativeSuggestionScrolling.NAME), activity.getSettings()),
			new ItemClearInsets(findPreference(ItemClearInsets.NAME), activity.getSettings())
		};

		for (ItemSwitch item : items) {
			item.populate().enableClickHandler();
		}
	}


	private void enablePreviewOnChange() {
		DropDownColorScheme colorScheme = findPreference(DropDownColorScheme.NAME);
		if (colorScheme != null) {
			colorScheme.setOnChangeListener(this::onThemeChange);
		}

		EnhancedDropDownPreference[] items = {
			findPreference(DropDownLayoutType.NAME),
			findPreference(DropDownAlignment.NAME),
			findPreference(DropDownWidth.NAME),
			findPreference(DropDownNumpadShape.NAME),
			findPreference(DropDownNumpadFnKeyScale.NAME),
			findPreference(DropDownNumpadKeyFontSize.NAME),
			findPreference(DropDownSuggestionFontSize.NAME),
		};

		for (EnhancedDropDownPreference item : items) {
			if (item != null) {
				item.setOnChangeListener(this::previewDropDownChange);
			}
		}

		SwitchPreferenceCompat[] switches = {
			findPreference("pref_arrow_keys_visible"),
			findPreference("pref_status_icon"),
		};

		for ( SwitchPreferenceCompat sw : switches) {
			if (sw != null) {
				sw.setOnPreferenceChangeListener(this::previewSwitchChange);
			}
		}
	}


	protected void onThemeChange(String s) {
		previewDropDownChange(null);
	}


	private void previewDropDownChange(String s) {
		preview.resume();
	}


	private boolean previewSwitchChange(Preference p, Object o) {
		previewDropDownChange(null);
		return true;
	}
}
