package org.nyanya.android.traditionalt9.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.nyanya.android.traditionalt9.R;

public class Setting {
	String title;
	String summary = null;
	public String id;
	public int widgetID = 0;
	public int layout;
	protected View view;

	public Setting (Context context, AttributeSet attrs, Object[] isettings) {
		// http://stackoverflow.com/a/8488691
		for (int i = 0; i < attrs.getAttributeCount(); i++) {
			String attr = attrs.getAttributeName(i);
			if ("title".equals(attr)) {
				// load string resource
				title = context.getString(attrs.getAttributeResourceValue(i, 0));
			} else if ("summary".equals(attr)) {
				summary = context.getString(attrs.getAttributeResourceValue(i, 0));
			} else if ("id".equals(attr)){
				id = attrs.getAttributeValue(i);
			}
		}
		if (summary == null)
			layout = R.layout.setting;
		else
			layout = R.layout.setting_sum;
	}

	public void clicked(final Context context) {}

	public void setView(View view) {
		this.view = view;
	}
	public void init() {};
}
