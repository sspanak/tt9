package io.github.sspanak.tt9.preferences.custom;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DropDownPreference;
import androidx.preference.Preference;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.sys.DeviceInfo;

abstract public class EnhancedDropDownPreference extends DropDownPreference {
	@NonNull protected final LinkedHashMap<String, String> values = new LinkedHashMap<>();

	public EnhancedDropDownPreference(@NonNull Context context) { super(context); init(context); }
	public EnhancedDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
	public EnhancedDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(context); }
	public EnhancedDropDownPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); init(context); }


	protected void init(@NonNull Context c) {
		setKey(getName());
		int title = getDisplayTitle();
		if (title != 0) {
			setTitle(title);
		}
	}


	@Override
	public void onAttached() {
		super.onAttached();
		setOnPreferenceChangeListener(this::onChange);
	}


	protected boolean onChange(Preference preference, Object newKey) {
		if (preference instanceof EnhancedDropDownPreference) {
			((DropDownPreference) preference).setValue(newKey.toString());
			preview();
		}

		return true;
	}


	final public void add(int key, String value) {
		add(String.valueOf(key), value);
	}


	final public EnhancedDropDownPreference add(int key, int resId) {
		add(String.valueOf(key), getContext().getString(resId));
		return this;
	}


	final public EnhancedDropDownPreference add(String key, String value) {
		values.put(key, value);
		return this;
	}


	final public EnhancedDropDownPreference add(String key, int resId) {
		add(key, getContext().getString(resId));
		return this;
	}


	final public EnhancedDropDownPreference commitOptions() {
		ArrayList<String> keys = new ArrayList<>(values.keySet());

		setEntryValues(keys.toArray(new CharSequence[0]));
		setEntries(this.values.values().toArray(new CharSequence[0]));

		return this;
	}


	final public void commitPercentRange(int start, int end, int step) {
		if (start < end && step > 0) {
			for (int i = start; i <= end; i += step) {
				add(i, i + " ％");
			}
		}
		commitOptions();
	}


	final public EnhancedDropDownPreference preview() {
		setSummary(values.get(getValue()));
		return this;
	}


	public EnhancedDropDownPreference sort() {
		if (!DeviceInfo.AT_LEAST_ANDROID_7 || values.size() <= 1) {
			return this;
		}

		LinkedHashMap<String, String> sorted = new LinkedHashMap<>();
		values.entrySet().stream()
			.sorted(LinkedHashMap.Entry.comparingByValue())
			.forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));

		values.clear();
		values.putAll(sorted);

		return this;
	}


	/**
	 * Populate the preference with options and labels based on the current settings.
	 */
	abstract public EnhancedDropDownPreference populate(@NonNull SettingsStore settings);


	/**
	 * The key used to identify this preference in the settings store.
	 */
	abstract protected String getName();


	/**
	 * The title displayed on the preference screen. Use "0" to skip setting a title (the default).
	 */
	protected int getDisplayTitle() {
		return 0;
	}
}
