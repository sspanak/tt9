package io.github.sspanak.tt9.chinese;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.Set;

import io.github.sspanak.tt9.preferences.PreferencesActivity;
import io.github.sspanak.tt9.preferences.screens.BaseScreenFragment;
import io.github.sspanak.tt9.util.SupremeExecutor;

/** Catalog shipped with the Chinese download, never bundled with the base APK. */
public class ChineseDomainPacksScreen extends BaseScreenFragment {
	public static final String NAME = "ChineseDomainPacks";
	private long queryVersion;

	public ChineseDomainPacksScreen() { super(); }
	public ChineseDomainPacksScreen(@Nullable PreferencesActivity activity) { super(activity); }
	@Override public String getName() { return NAME; }
	@Override protected int getTitle() { return R.string.chinese_domain_packs_title; }
	@Override protected int getXml() { return R.xml.chinese_domain_packs; }
	@Override protected void onCreate() { resetFontSize(false); }
	@Override public void onResume() { super.onResume(); refresh(); }
	@Override public void onDestroyView() { queryVersion++; super.onDestroyView(); }

	private void refresh() {
		if (activity == null) return;
		PreferencesActivity screen = activity;
		long version = ++queryVersion;
		SupremeExecutor.execute(() -> {
			ChineseDomainIndex catalog = ChineseDomains.index(screen);
			Set<String> enabled = ChineseDomains.enabled(screen);
			SupremeExecutor.executeOnMainThread(() -> {
				if (version != queryVersion || !isAdded()) return;
				PreferenceCategory list = findPreference("chinese_domain_list");
				if (list == null) return;
				list.removeAll();
				Preference sources = findPreference("chinese_domain_sources");
				if (sources != null) {
					sources.setEnabled(catalog != null);
					sources.setOnPreferenceClickListener(preference -> { showSources(screen, catalog); return true; });
				}
				if (catalog == null) {
					Preference empty = new Preference(screen);
					empty.setSummary(R.string.chinese_domain_packs_missing);
					empty.setSelectable(false);
					list.addPreference(empty);
				} else {
					boolean chinese = screen.getResources().getConfiguration().getLocales().get(0).getLanguage().equals(Locale.CHINESE.getLanguage());
					for (ChineseDomainIndex.Domain domain : catalog.getDomains()) {
						SwitchPreferenceCompat item = new SwitchPreferenceCompat(screen);
						item.setKey("chinese_domain_" + domain.id);
						item.setPersistent(false);
						item.setTitle(chinese ? domain.labelZh : domain.labelEn);
						item.setSummary(getString(R.string.chinese_domain_pack_detail, domain.wordCount));
						item.setChecked(enabled.contains(domain.id));
						item.setOnPreferenceChangeListener((preference, value) -> {
							ChineseDomains.setEnabled(screen, domain.id, (boolean) value);
							return true;
						});
						list.addPreference(item);
					}
				}
				resetFontSize(true);
			});
		});
	}

	private void showSources(PreferencesActivity screen, ChineseDomainIndex catalog) {
		if (catalog == null) return;
		boolean chinese = screen.getResources().getConfiguration().getLocales().get(0).getLanguage().equals(Locale.CHINESE.getLanguage());
		StringBuilder text = new StringBuilder(getString(R.string.chinese_domain_packs_help));
		for (ChineseDomainIndex.Domain domain : catalog.getDomains()) {
			text.append("\n\n").append(chinese ? domain.labelZh : domain.labelEn)
				.append("\n").append(getString(R.string.chinese_domain_pack_detail, domain.wordCount))
				.append("\n").append(domain.license).append("\n").append(domain.source);
		}
		new MaterialAlertDialogBuilder(screen).setTitle(R.string.chinese_domain_sources)
			.setMessage(text).setPositiveButton(android.R.string.ok, null).show();
	}
}
