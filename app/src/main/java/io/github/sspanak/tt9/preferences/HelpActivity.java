package io.github.sspanak.tt9.preferences;

import android.util.TypedValue;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;

import io.github.sspanak.tt9.ui.WebViewActivity;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.SystemSettings;

public class HelpActivity extends WebViewActivity {
	public HelpActivity() {
		transparentBackground = true;
	}

	@Override
	protected String getMimeType() {
		return "text/html";
	}

	@Override
	protected String getText() {
		try {
			BufferedReader reader = getHelpFileReader();
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			return builder.toString()
				.replaceFirst("color: default", getTextColor())
				.replaceFirst("color: accent", getLinkColor());
		} catch (Exception e) {
			Logger.e(getClass().getSimpleName(), "Failed opening the help HTML document.");
			return "";
		}
	}

	private BufferedReader getHelpFileReader() throws IOException {
		String systemLanguage = SystemSettings.getLocale().replaceFirst("_\\w+$", "");
		HelpFile file = new HelpFile(this, systemLanguage);
		file = file.exists() ? file : new HelpFile(this);
		return file.getReader();
	}

	private String getTextColor() {
		return colorToHex(new TextView(this).getTextColors().getDefaultColor());
	}

	private String getLinkColor() {
		final TypedValue value = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.colorAccent, value, true);
		return colorToHex(value.data);
	}

	private String colorToHex(int color) {
		String textColor = String.format("%06x", color);
		textColor = textColor.length() == 8 ? textColor.substring(2) : textColor;
		return "color: #" + textColor;
	}
}
