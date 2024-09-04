package io.github.sspanak.tt9.preferences;

import io.github.sspanak.tt9.db.customWords.LogcatExporter;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.ui.WebViewActivity;

public class LogsActivity extends WebViewActivity {
	@Override
	protected String getMimeType() {
		return "text/plain";
	}

	@Override
	protected String getText() {
		boolean includeSystemLogs = new SettingsStore(this).getSystemLogs();
		String logs = LogcatExporter.getLogs(includeSystemLogs).replace("\n", "\n\n");
		return logs.isEmpty() ? "No Logs" : logs;
	}
}
