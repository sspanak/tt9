package org.nyanya.android.traditionalt9.settings;

// https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.nyanya.android.traditionalt9.R;

import java.util.ArrayList;

public class SettingAdapter extends ArrayAdapter<Setting> {
	public SettingAdapter(Context context, ArrayList<Setting> settings) {
		super(context, 0, settings);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the data item for this position
		Setting setting = getItem(position);
		final LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		// Check if an existing view is being reused, otherwise inflate the view
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.setting_widget, parent, false);
		}
		setting.setView(convertView);
		// Lookup view for data population
		((TextView) convertView.findViewById(R.id.title)).setText(setting.title);
		View sv = convertView.findViewById(R.id.summary);
		if (setting.summary != null && sv != null) {
			((TextView) sv).setText(setting.summary);
			sv.setVisibility(View.VISIBLE);
		}
		else if (sv != null) { sv.setVisibility(View.GONE); }

		final ViewGroup widgetFrame = (ViewGroup) convertView.findViewById(R.id.widget_frame);
		if (setting.widgetID != 0) {
			widgetFrame.removeAllViews();
			layoutInflater.inflate(setting.widgetID, widgetFrame);
			widgetFrame.setVisibility(View.VISIBLE);
		}
		else {
			// hide the widget area
			widgetFrame.setVisibility(View.GONE);
		}
		setting.init();
		// Return the completed view to render on screen
		return convertView;
	}
}
