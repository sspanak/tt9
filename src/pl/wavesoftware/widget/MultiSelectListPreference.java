package pl.wavesoftware.widget;
// https://gist.github.com/cardil/4754571/07b4b6ffd37b440bbdec2cafa1ab7411c5ad3873
// modified to work specifically for this service
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Log;

public class MultiSelectListPreference extends ListPreference {

	private String separator;
	private static final String DEFAULT_SEPARATOR = "\u0001\u0007\u001D\u0007\u0001";
	private boolean[] entryChecked;

	public MultiSelectListPreference(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		entryChecked = new boolean[getEntries().length];
		separator = DEFAULT_SEPARATOR;
	}

	public MultiSelectListPreference(Context context) {
		this(context, null);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		CharSequence[] entries = getEntries();
		CharSequence[] entryValues = getEntryValues();
		if (entries == null || entryValues == null
				|| entries.length != entryValues.length) {
			throw new IllegalStateException(
					"MultiSelectListPreference requires an entries array and an entryValues "
							+ "array which are both the same length");
		}

		restoreCheckedEntries();
		OnMultiChoiceClickListener listener = new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean val) {
				entryChecked[which] = val;
			}
		};
		builder.setMultiChoiceItems(entries, entryChecked, listener);
	}

	private CharSequence[] unpack(CharSequence val) {
		if (val == null || "".equals(val)) {
			return new CharSequence[0];
		} else {
			return ((String) val).split(separator);
		}
	}

	// added method
	public static int[] defaultunpack2Int(CharSequence val) {
		if (val == null || "".equals(val)) {
			//Log.w("MultiSelectPref.defaultunpack", "val is null or empty");
			return new int[] {0}; //default pref
		} else {
			String[] sa = ((String) val).split(DEFAULT_SEPARATOR);
			if (sa.length < 1) {
				Log.w("MultiSelectPref.defaultunpack", "split is less than 1");
				return new int[] {0}; //default pref
			}
			int[] ia = new int[sa.length];
			for (int x=0;x<sa.length;x++) {
				ia[x] = Integer.valueOf(sa[x]);
			}
			return ia;
		}
	}

	/**
	 * Gets the entries values that are selected
	 *
	 * @return the selected entries values
	 */
	public CharSequence[] getCheckedValues() {
		return unpack(getValue());
	}

	private void restoreCheckedEntries() {
		CharSequence[] entryValues = getEntryValues();

		// Explode the string read in sharedpreferences
		CharSequence[] vals = unpack(getValue());

		if (vals != null) {
			List<CharSequence> valuesList = Arrays.asList(vals);
			for (int i = 0; i < entryValues.length; i++) {
				CharSequence entry = entryValues[i];
				entryChecked[i] = valuesList.contains(entry);
			}
		}
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		List<CharSequence> values = new ArrayList<CharSequence>();

		CharSequence[] entryValues = getEntryValues();
		if (positiveResult && entryValues != null) {
			for (int i = 0; i < entryValues.length; i++) {
				if (entryChecked[i] == true) {
					String val = (String) entryValues[i];
					values.add(val);
				}
			}

			String value = join(values, separator);
			setSummary(prepareSummary(values));
			setValueAndEvent(value);
		}
	}

	//modified to make sure some sane thing gets stored
	private void setValueAndEvent(String value) {
		if (callChangeListener(unpack(value))) {
			if (value == null || value.length() < 1) {
				setValue("0"); //default
			} else {
				setValue(value);
			}
		}
	}

	private CharSequence prepareSummary(List<CharSequence> joined) {
		List<String> titles = new ArrayList<String>();
		CharSequence[] entryTitle = getEntries();
		CharSequence[] entryValues = getEntryValues();
		int ix = 0;
		for (CharSequence value : entryValues) {
			if (joined.contains(value)) {
				titles.add((String) entryTitle[ix]);
			}
			ix += 1;
		}
		return join(titles, ", ");
	}

	@Override
	protected Object onGetDefaultValue(TypedArray typedArray, int index) {
		return typedArray.getTextArray(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue,
									 Object rawDefaultValue) {
		String value = null;
		CharSequence[] defaultValue;
		if (rawDefaultValue == null) {
			defaultValue = new CharSequence[0];
		} else {
			defaultValue = (CharSequence[]) rawDefaultValue;
		}
		List<CharSequence> joined = Arrays.asList(defaultValue);
		String joinedDefaultValue = join(joined, separator);
		if (restoreValue) {
			value = getPersistedString(joinedDefaultValue);
		} else {
			value = joinedDefaultValue;
		}

		setSummary(prepareSummary(Arrays.asList(unpack(value))));
		setValueAndEvent(value);
	}

	/**
	 * Joins array of object to single string by separator
	 *
	 * Credits to kurellajunior on this post
	 * http://snippets.dzone.com/posts/show/91
	 *
	 * @param iterable
	 *            any kind of iterable ex.: <code>["a", "b", "c"]</code>
	 * @param separator
	 *            separetes entries ex.: <code>","</code>
	 * @return joined string ex.: <code>"a,b,c"</code>
	 */
	protected static String join(Iterable<?> iterable, String separator) {
		Iterator<?> oIter;
		if (iterable == null || (!(oIter = iterable.iterator()).hasNext()))
			return "";
		StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
		while (oIter.hasNext())
			oBuilder.append(separator).append(oIter.next());
		return oBuilder.toString();
	}

}
