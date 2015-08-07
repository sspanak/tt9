package org.nyanya.android.traditionalt9.settings;

// http://stackoverflow.com/a/8488691

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;

import org.xmlpull.v1.XmlPullParser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class CustomInflater {
	public static ArrayList<Setting> inflate(Context context, int xmlFileResId, Object[] isettings)
			throws Exception {
		ArrayList<Setting> settings = new ArrayList<Setting>();

		XmlResourceParser parser = context.getResources().getXml(xmlFileResId);
		int token;
		while ((token = parser.next()) != XmlPullParser.END_DOCUMENT) {
			if (token == XmlPullParser.START_TAG) {
				if (!parser.getName().equals("Settings")) {
					//prepend package
					Class aClass = Class.forName("org.nyanya.android.traditionalt9.settings."+parser.getName());
					Class<?>[] params = new Class[]{Context.class, AttributeSet.class, isettings.getClass()};
					Constructor<?> constructor = aClass.getConstructor(params);
					try {
						settings.add((Setting) constructor.newInstance(context, parser, isettings));
					} catch (InstantiationException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return settings;
	}
}
