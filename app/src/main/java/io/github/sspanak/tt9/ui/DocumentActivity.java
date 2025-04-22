package io.github.sspanak.tt9.ui;

import java.io.BufferedReader;
import java.io.IOException;

import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.colors.AccentSystemColor;
import io.github.sspanak.tt9.util.colors.TextSystemColor;

abstract public class DocumentActivity extends WebViewActivity {
	public DocumentActivity() {
		transparentBackground = true;
	}

	@Override
	protected String getText() {
		try {
			BufferedReader reader = getDocumentReader();
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			return builder.toString()
				.replaceFirst("color: default;", (new TextSystemColor(this)).toCssColor())
				.replaceFirst("color: accent;", (new AccentSystemColor(this)).toCssColor());
		} catch (Exception e) {
			Logger.e(getClass().getSimpleName(), "Failed opening the HTML document.");
			return "";
		}
	}

	@Override
	protected String getMimeType() {
		return "text/html";
	}

	abstract protected BufferedReader getDocumentReader() throws IOException;
}
