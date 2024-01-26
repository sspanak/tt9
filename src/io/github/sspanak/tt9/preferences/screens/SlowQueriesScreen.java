package io.github.sspanak.tt9.preferences.screens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

import androidx.preference.Preference;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.db.SlowQueryStats;
import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.ui.UI;

public class SlowQueriesScreen extends BaseScreenFragment {
	private final static String RESET_BUTTON = "pref_slow_queries_reset_stats";
	private final static String SUMMARY_CONTAINER = "summary_container";
	private final static String QUERY_LIST_CONTAINER = "query_list_container";

	public SlowQueriesScreen() { init(); }
	public SlowQueriesScreen(PreferencesActivity activity) { init(activity); }

	@Override protected int getTitle() { return R.string.pref_category_slow_queries; }
	@Override protected int getXml() { return R.xml.prefs_screen_slow_queries; }

	@Override
	protected void onCreate() {
		printSummary();
		printSlowQueries();
		enableLogsCopy();

		Preference resetButton = findPreference(RESET_BUTTON);
		if (resetButton != null) {
			resetButton.setOnPreferenceClickListener((Preference p) -> {
				SlowQueryStats.clear();
				printSummary();
				printSlowQueries();
				return true;
			});
		}
	}

	private void printSummary() {
		Preference logsContainer = findPreference(SUMMARY_CONTAINER);
		if (logsContainer != null) {
			logsContainer.setSummary(Logger.isDebugLevel() ? SlowQueryStats.getSummary() : "Debugging disabled");
		}
	}

	private void printSlowQueries() {
		Preference queryListContainer = findPreference(QUERY_LIST_CONTAINER);
		if (queryListContainer != null) {
			String message;
			if (!Logger.isDebugLevel()) {
				message = "Debugging disabled";
			} else {
				message = SlowQueryStats.getList();
				message = message.isEmpty() ? "No slow queries." : message;
			}
			queryListContainer.setSummary(message);
		}
	}


	private void enableLogsCopy() {
		Preference queryListContainer = findPreference(QUERY_LIST_CONTAINER);
		if (activity == null || queryListContainer == null) {
			return;
		}

		ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		queryListContainer.setOnPreferenceClickListener((Preference p) -> {
			clipboard.setPrimaryClip(ClipData.newPlainText("TT9 debug log", p.getSummary()));
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
				UI.toast(activity, "Logs copied.");
			}
			return true;
		});
	}
}
