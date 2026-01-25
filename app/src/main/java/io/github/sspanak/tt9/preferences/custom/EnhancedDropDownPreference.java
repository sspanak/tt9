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
import io.github.sspanak.tt9.util.ConsumerCompat;

abstract public class EnhancedDropDownPreference extends DropDownPreference {
	@NonNull protected final LinkedHashMap<String, String> values = new LinkedHashMap<>();
	@Nullable private ConsumerCompat<String> externalChangeListener = null;

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
		setOnPreferenceChangeListener(this::handleChange);
	}


	/**
	 * Internal change handler. Manages updating the preview and calling external listeners, if any,
	 * and calling onChange() for subclasses.
	 */
	protected final boolean handleChange(Preference preference, Object newValue) {
		if (preference instanceof EnhancedDropDownPreference) {
			((DropDownPreference) preference).setValue(newValue.toString());
			preview();
		}

		if (!onChange(preference, newValue)) {
			return false;
		}

		if (externalChangeListener != null) {
			externalChangeListener.accept(newValue.toString());
		}

		return true;
	}


	/**
	 * Change handler for subclasses to override. Return false to prevent calling external listeners.
	 */
	protected boolean onChange(Preference preference, Object newKey) {
		return true;
	}


	/**
	 * Set an external listener to be called when the preference value changes.
	 */
	public void setOnChangeListener(@Nullable ConsumerCompat<String> listener) {
		externalChangeListener = listener;
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
		commitRange(start, end, step, " ％");
	}


	final public void commitRange(int start, int end, int step, @Nullable String suffix) {
		if (start < end && step > 0) {
			for (int i = start; i <= end; i += step) {
				add(i, suffix != null ? i + suffix : String.valueOf(i));
			}
		}
		commitOptions();
	}


	public boolean isEmpty() {
		return values.isEmpty();
	}


	final public EnhancedDropDownPreference preview() {
		setSummary(values.get(getValue()));
		return this;
	}


	public EnhancedDropDownPreference sort() {
		if (values.size() <= 1) {
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
