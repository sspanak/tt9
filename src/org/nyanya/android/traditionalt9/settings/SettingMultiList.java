package org.nyanya.android.traditionalt9.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.nyanya.android.traditionalt9.LangHelper;
import org.nyanya.android.traditionalt9.R;
import org.nyanya.android.traditionalt9.T9DB;
import org.nyanya.android.traditionalt9.T9DB.DBSettings.SETTING;

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
						T9DB.getInstance(context).storeSettingInt(SETTING.get(id), LangHelper.shrinkLangs(buildSelection()));
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

	private int[] buildSelection(){
		int count = 0;
		for (boolean b: selectedEntries) {if (b) count++;}
		int[] selection = new int[count];
		count = 0;
		for (int x=0;x<selectedEntries.length;x++) {
			if (selectedEntries[x]) {
				selection[count] = entryValues[x];
				count++;
			}
		}
		if (selection.length < 1)
			return new int[] {entryValues[0]};
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
