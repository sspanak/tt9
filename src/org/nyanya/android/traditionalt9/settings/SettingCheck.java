package org.nyanya.android.traditionalt9.settings;


import android.content.Context;
import android.util.AttributeSet;

import org.nyanya.android.traditionalt9.R;

public class SettingCheck extends Setting {
	boolean value;
	boolean defaultValue;

	public SettingCheck (Context context, AttributeSet attrs, Object[] isettings) {
		super(context, attrs, isettings);
		// http://stackoverflow.com/a/8488691
		for (int i = 0; i < attrs.getAttributeCount(); i++) {
			String attr = attrs.getAttributeName(i);
			if ("defaultValue".equals(attr)) {
				defaultValue = attrs.getAttributeBooleanValue(i, false);
			}
		}
		if (id.equals("pref_mode_notify")){
			if (isettings[2] != null)
				value = isettings[2].equals(1);
			else
				value = defaultValue;
		}
		widgetID = R.layout.checkbox;
	}

	@Override
	public void clicked(Context context) {

	}


}
