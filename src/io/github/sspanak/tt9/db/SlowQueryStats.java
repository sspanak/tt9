package io.github.sspanak.tt9.db;

import java.util.HashMap;

import io.github.sspanak.tt9.Logger;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.SettingsStore;

public class SlowQueryStats {
	private static final String LOG_TAG = SlowQueryStats.class.getSimpleName();
	private static long totalQueries = 0;
	private static long totalQueryTime = 0;
	private static final HashMap<String, Integer> slowQueries = new HashMap<>();
	private static final HashMap<String, String> resultCache = new HashMap<>();


	private static String generateKey(Language language, String sequence, String wordFilter, int minimumWords, int maximumWords) {
		return language.getId() + "_" + sequence + "_" + wordFilter + "_" + minimumWords + "_" + maximumWords;
	}

	public static void add(SettingsStore settings, Language language, String sequence, String wordFilter, int minimumWords, int maximumWords, int time, String positionsList) {
		long start = System.currentTimeMillis();

		totalQueries++;
		totalQueryTime += time;
		if (time < settings.getSlowQueryTime()) {
			return;
		}

		String key = generateKey(language, sequence, wordFilter, minimumWords, maximumWords);
		slowQueries.put(key, time);
		resultCache.put(key, positionsList);

		Logger.d(LOG_TAG, "Slow query stats collected in: " + (System.currentTimeMillis() - start) + " ms.");
	}

	public static String getCachedIfSlow(SettingsStore settings, Language language, String sequence, String wordFilter, int minimumWords, int maximumWords) {
		String key = generateKey(language, sequence, wordFilter, minimumWords, maximumWords);
		Integer queryTime = slowQueries.get(key);
		boolean isSlow = queryTime != null && queryTime >= settings.getSlowQueryTime();

		return isSlow ? resultCache.get(key) : null;
	}

	public static String getSummary() {
		long slowQueryTotalTime = 0;
		for (int time : slowQueries.values()) {
			slowQueryTotalTime += time;
		}

		long averageTime = totalQueries == 0 ? 0 : totalQueryTime / totalQueries;
		long slowAverageTime = slowQueries.size() == 0 ? 0 : slowQueryTotalTime / slowQueries.size();

		return  "Queries: " + totalQueries + ". Average time: " + averageTime + " ms." +
			"\nSlow: " + slowQueries.size() + ". Average time: " + slowAverageTime + " ms.";
	}

	public static String getList() {
		StringBuilder sb = new StringBuilder();
		for (String key : slowQueries.keySet()) {
			sb.append(key).append(": ").append(slowQueries.get(key)).append(" ms\n");
		}

		return sb.toString();
	}


	public static void clear() {
		totalQueries = 0;
		totalQueryTime = 0;
		slowQueries.clear();
		resultCache.clear();
	}
}
