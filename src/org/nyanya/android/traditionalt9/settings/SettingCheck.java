package org.nyanya.android.traditionalt9.settings;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import org.nyanya.android.traditionalt9.R;
import org.nyanya.android.traditionalt9.T9DB;

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
		layout = R.layout.setting_widget;
	}

	@Override
	public void clicked(Context context) {
		value = !value;
		T9DB.getInstance(context).storeSettingInt(T9DB.DBSettings.SETTING.get(id), value ? 1 : 0);
		((CheckBox)view.findViewById(R.id.checkbox)).setChecked(value);
	}

	@Override
	public void init(){
		((CheckBox)view.findViewById(R.id.checkbox)).setChecked(value);
	}
}
