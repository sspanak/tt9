package io.github.sspanak.tt9.settings_legacy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;

import io.github.sspanak.tt9.R;

public class SettingList extends Setting {
	String[] entries;
	int[] entryValues;
	int defaultValue;
	int value;

	public SettingList (Context context, AttributeSet attrs, Object[] isettings) {
		super(context, attrs, isettings);
		// http://stackoverflow.com/a/8488691
		for (int i = 0; i < attrs.getAttributeCount(); i++) {
			String attr = attrs.getAttributeName(i);
			if ("defaultValue".equals(attr)) {
				defaultValue = attrs.getAttributeIntValue(i, -1);
			} else if ("entryValues".equals(attr)) {
				// load string resource
				entryValues = context.getResources().getIntArray(attrs.getAttributeResourceValue(i, 0));
			} else if ("entries".equals(attr)) {
				entries = context.getResources().getStringArray(attrs.getAttributeResourceValue(i, 0));
			}
		}

		widgetID = R.layout.preference_dialog;
		layout = R.layout.setting_widget;
	}

	public void clicked(final Context context) {
		AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
		builderSingle.setTitle(title);

		builderSingle.setNegativeButton(android.R.string.cancel,
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
		builderSingle.setSingleChoiceItems(entries, value,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						value = entryValues[which];
						dialog.dismiss();
					}
				});
		builderSingle.show();
	}
}
