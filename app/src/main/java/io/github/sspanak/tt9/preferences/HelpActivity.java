package io.github.sspanak.tt9.preferences;

import java.io.BufferedReader;
import java.io.IOException;

import io.github.sspanak.tt9.ui.WebViewActivity;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.colors.AccentSystemColor;
import io.github.sspanak.tt9.util.colors.TextSystemColor;
import io.github.sspanak.tt9.util.sys.SystemSettings;

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
				.replaceFirst("color: default;", (new TextSystemColor(this)).toCssColor())
				.replaceFirst("color: accent;", (new AccentSystemColor(this)).toCssColor());
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
}
