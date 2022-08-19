package io.github.sspanak.tt9.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;

import io.github.sspanak.tt9.LangHelper;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.preferences.T9Preferences;

public class SettingMultiList extends SettingList {
	boolean[] selectedEntries = new boolean[0];

	public SettingMultiList (Context context, AttributeSet attrs, Object[] isettings) {
		super(context, attrs, isettings);
		selectedEntries = new boolean[entries.length];
		for (LangHelper.LANGUAGE l : LangHelper.buildLangs((Integer)isettings[1])) {
			selectedEntries[l.index] = true;
		}
		summary = buildItems();
	}

	public void clicked(final Context context) {
		AlertDialog.Builder builderMulti = new AlertDialog.Builder(context);
		builderMulti.setTitle(title);

		builderMulti.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builderMulti.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (id.equals("pref_lang_support")) {
							T9Preferences.getInstance(context).setEnabledLanguageFromMaskBits(buildSelection());
						}
						summary = buildItems();
						dialog.dismiss();
						((TextView)view.findViewById(R.id.summary)).setText(summary);
					}
				});
		builderMulti.setMultiChoiceItems(entries, selectedEntries,
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean opt) {
						selectedEntries[which] = opt;
					}
				});
		builderMulti.show();
	}

	private ArrayList<Integer> buildSelection(){
		ArrayList<Integer> selection = new ArrayList<>();
		for (int x=0;x<selectedEntries.length;x++) {
			if (selectedEntries[x]) {
				selection.add(entryValues[x]);
			}
		}

		if (selection.size() < 1) {
			selection.add(entryValues[0]);
		}
		return selection;
	}
	private String buildItems() {
		StringBuilder sb = new StringBuilder();
		for (int x=0;x<selectedEntries.length;x++) {
			if (selectedEntries[x]) {
				sb.append(entries[x]);
				sb.append((", "));
			}
		}
		if (sb.length() > 1)
			sb.setLength(sb.length()-2);
		return sb.toString();
	}
}
