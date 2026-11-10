package io.github.sspanak.tt9.chinese;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.github.sspanak.tt9.extensions.ChinesePack;
import io.github.sspanak.tt9.util.Logger;

/** Optional data, activation and bounded learning. Load the catalog only on a worker thread. */
final class ChineseDomains {
	private static final String ENABLED = "pref_chinese_domain_packs";
	private static final String LEARNED = "pref_chinese_domain_learned";
	private static final int MAX_LEARNED = 512;
	private static ChineseDomainIndex cached;
	private static long packGeneration = -1;
	private static boolean attempted;

	private ChineseDomains() {}

	static synchronized ChineseDomainIndex index(Context context) {
		long current = ChinesePack.getGeneration();
		if (current != packGeneration) { cached = null; attempted = false; packGeneration = current; }
		if (!attempted) {
			attempted = true;
			try (InputStream stream = ChinesePack.open(context, ChineseDomainIndex.FILE_NAME)) { cached = ChineseDomainIndex.load(stream); }
			catch (IOException exception) { Logger.w("ChineseDomains", "Domain data unavailable: " + exception.getMessage()); }
		}
		return cached;
	}

	static Set<String> enabled(Context context) { return readSet(context, ENABLED); }

	static void setEnabled(Context context, String id, boolean enabled) {
		Set<String> values = new HashSet<>(enabled(context));
		if (enabled) values.add(id); else values.remove(id);
		preferences(context).edit().putStringSet(ENABLED, values).apply();
	}

	static Map<String, Integer> learning(Context context) {
		Map<String, Integer> result = new LinkedHashMap<>();
		for (String entry : readSet(context, LEARNED)) {
			int split = entry.lastIndexOf('\t');
			if (split <= 0) continue;
			try {
				int count = Integer.parseInt(entry.substring(split + 1));
				if (count > 0 && count <= 1_000_000) result.put(entry.substring(0, split), count);
			} catch (NumberFormatException ignored) { }
			if (result.size() >= MAX_LEARNED) break;
		}
		return result;
	}

	/** Never inserts a domain entry into custom_words or changes a factory dictionary position. */
	static synchronized void learn(Context context, String raw) {
		Map<String, Integer> counts = learning(context);
		if (!counts.containsKey(raw) && counts.size() >= MAX_LEARNED) {
			String least = Collections.min(counts.keySet(), (left, right) -> {
				int compared = Integer.compare(counts.get(left), counts.get(right));
				return compared != 0 ? compared : left.compareTo(right);
			});
			counts.remove(least);
		}
		counts.put(raw, Math.min(1_000_000, counts.getOrDefault(raw, 0) + 1));
		Set<String> values = new HashSet<>();
		counts.forEach((word, count) -> values.add(word + "\t" + count));
		preferences(context).edit().putStringSet(LEARNED, values).apply();
	}

	private static SharedPreferences preferences(Context context) { return PreferenceManager.getDefaultSharedPreferences(context); }
	private static Set<String> readSet(Context context, String key) {
		try { return new HashSet<>(preferences(context).getStringSet(key, Collections.emptySet())); }
		catch (ClassCastException ignored) { return Collections.emptySet(); }
	}
}
