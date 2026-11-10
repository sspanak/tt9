package io.github.sspanak.tt9.chinese;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import io.github.sspanak.tt9.db.DataStore;
import io.github.sspanak.tt9.extensions.ChinesePack;
import io.github.sspanak.tt9.ime.modes.predictions.IdeogramPredictions;
import io.github.sspanak.tt9.languages.exceptions.InvalidLanguageCharactersException;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;
import io.github.sspanak.tt9.util.Logger;
import io.github.sspanak.tt9.util.TextTools;
import io.github.sspanak.tt9.util.SupremeExecutor;

/** Optional Chinese-only queries and ranking; all dictionary work stays off the input thread. */
public class ChinesePredictions extends IdeogramPredictions {
	private ChinesePrefixIndex prefixes;
	private CommonHanCharacters commonCharacters;
	private boolean prefixesAttempted;
	private boolean commonCharactersAttempted;
	private ArrayList<String> phoneticWords = new ArrayList<>();
	private final AtomicLong generation = new AtomicLong();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private long cachedPackGeneration = -1;
	private boolean completionsAllowed = true;
	private Map<String, ChineseDomainIndex.Entry> domainReadings = Collections.emptyMap();
	private Map<String, ChineseDomainIndex.Entry> lastDomainReadings = Collections.emptyMap();
	private ChineseDomainIndex domainCatalog;
	private ChineseDomainIndex lastDomainCatalog;

	public ChinesePredictions(SettingsStore settings) { super(settings); }
	public ChinesePredictions setCompletionsAllowed(boolean allowed) { completionsAllowed = allowed; return this; }

	@Override public void reset() {
		generation.incrementAndGet();
		phoneticWords.clear();
		domainReadings = lastDomainReadings = Collections.emptyMap();
		domainCatalog = lastDomainCatalog = null;
		super.reset();
	}

	@Override public void load() {
		containsGeneratedWords = false;
		final long request = generation.incrementAndGet();
		if (digitSequence.isEmpty()) { super.load(); return; }
		final long packGeneration = ChinesePack.getGeneration();
		final Set<String> enabled = ChineseDomains.enabled(settings.getContext());
		if (!digitSequence.matches("[2-9]+")) {
			DataStore.getWords(dbWords -> publish(dbWords, isRetryAllowed(), request, Collections.emptyMap(), null, enabled, packGeneration),
				language, digitSequence, onlyExactMatches, stem, orderWordsByLength, minWords, maxWords);
			return;
		}
		final String digits = digitSequence, selectedReading = stem;
		final int fuzzy = settings.getChineseFuzzyOptions();
		List<String> sequences = PinyinFuzzy.sequences(digits, selectedReading, fuzzy);
		// Fuzzy filtering uses its own compact spelling check; the ordinary stem SQL only matches exact reading prefixes.
		DataStore.getWordVariants(raw -> {
			if (request != generation.get()) return;
			Map<String, ChineseDomainIndex.Entry> domainMatches = new HashMap<>();
			ChineseDomainIndex catalog = enabled.isEmpty() ? null : ChineseDomains.index(settings.getContext());
			int active = catalog == null ? 0 : catalog.maskFor(enabled);
			ArrayList<ChineseDomainIndex.Entry> domainExact = new ArrayList<>(), domainFuzzy = new ArrayList<>();
			if (active != 0) {
				for (String sequence : sequences) {
					for (ChineseDomainIndex.Entry entry : catalog.lookupExact(sequence, active, ChineseDomainIndex.MAX_RESULTS)) {
						if (domainMatches.size() >= ChineseDomainIndex.MAX_RESULTS) break;
						if (!matchesReading(entry, selectedReading) || !PinyinFuzzy.matches(digits, selectedReading, entry.reading, fuzzy)
							|| !allowed(entry.raw()) || DataStore.isCustomWord(language, entry.raw())) continue;
						if (domainMatches.putIfAbsent(entry.raw(), entry) == null) {
							(entry.sequence.equals(digits) ? domainExact : domainFuzzy).add(entry);
						}
					}
				}
			}
			ArrayList<String> exact = new ArrayList<>(), alternatives = new ArrayList<>();
			ArrayList<String> unfinished = new ArrayList<>();
			for (String word : raw) {
				String reading = reading(word);
				if (!PinyinFuzzy.matches(digits, selectedReading, reading, fuzzy)) unfinished.add(word);
				else if (PinyinSyllables.getSequence(reading).equals(digits)) exact.add(word); else alternatives.add(word);
			}
			Map<String, Integer> learning = active == 0 ? Collections.emptyMap() : ChineseDomains.learning(settings.getContext());
			if (!domainExact.isEmpty()) exact = ChineseDomainRanking.prioritize(exact, domainExact,
				entry -> learning.getOrDefault(entry.raw(), 0), word -> protectedBase(word, digits), ChineseDomainIndex.MAX_RESULTS);
			if (!domainFuzzy.isEmpty()) alternatives = ChineseDomainRanking.prioritize(alternatives, domainFuzzy,
				entry -> learning.getOrDefault(entry.raw(), 0), word -> false, ChineseDomainIndex.MAX_RESULTS);
			int insertion = Math.min(8, exact.size());
			exact.addAll(insertion, alternatives);
			if (exact.isEmpty()) {
				exact.addAll(unfinished);
				if (active != 0) {
					for (ChineseDomainIndex.Entry entry : catalog.lookupPrefix(digits, active, 64)) {
						if (!matchesReading(entry, selectedReading) || !allowed(entry.raw()) || DataStore.isCustomWord(language, entry.raw())) continue;
						domainMatches.put(entry.raw(), entry);
						if (!exact.contains(entry.raw())) exact.add(entry.raw());
					}
				}
			}
			publish(exact, false, request, domainMatches, catalog, enabled, packGeneration);
		}, language, sequences, fuzzy == 0 ? selectedReading.replace("'", "") : "", word -> {
			String reading = reading(word);
			return !reading.isEmpty() && PinyinFuzzy.matches(digits, selectedReading, reading, fuzzy)
				&& PinyinSyllables.matchesSelectedReading(word, selectedReading) && allowed(word);
		}, word -> {
			String reading = reading(word);
			return !reading.isEmpty() && PinyinFuzzy.matchesPrefix(digits, selectedReading, reading)
				&& PinyinSyllables.matchesSelectedReading(word, selectedReading) && allowed(word);
		});
	}

	@Override protected void onDbWords(ArrayList<String> dbWords, boolean retry) {
		publish(dbWords, retry, generation.get());
	}

	private void publish(ArrayList<String> dbWords, boolean retry, long request) {
		publish(dbWords, retry, request, Collections.emptyMap(), null,
			ChineseDomains.enabled(settings.getContext()), ChinesePack.getGeneration());
	}

	private void publish(ArrayList<String> dbWords, boolean retry, long request,
		Map<String, ChineseDomainIndex.Entry> domains, ChineseDomainIndex catalog, Set<String> enabled, long packGeneration) {
		if (request != generation.get() || packGeneration != ChinesePack.getGeneration()) return;
		refreshCaches();
		String digits = digitSequence;
		if (completionsAllowed && digits.length() == 1 && stem.isEmpty()) {
			ChinesePrefixIndex index = prefixes();
			if (index != null) {
				ArrayList<String> frequentCharacters = new ArrayList<>();
				for (ChinesePrefixIndex.Entry entry : index.lookupFirstKey(digits, stem, ChinesePrefixIndex.MAX_FIRST_KEY_RESULTS)) {
					String raw = entry.transcription + entry.word;
					if (allowed(raw)) frequentCharacters.add(raw);
				}
				// Preserve promoted/learned leading rows and personal words; frequent prefix characters
				// supplement exact vowel readings without scanning all dictionary rows on every key.
				dbWords = dbWords.isEmpty() ? frequentCharacters : ChineseCandidateRanking.merge(dbWords, frequentCharacters,
					index::singleCharacterFrequency, raw -> DataStore.isCustomWord(language, raw));
			}
		}
		ArrayList<String> phoneticSnapshot = new ArrayList<>(dbWords);
		ArrayList<String> completed = new ArrayList<>(dbWords);
		if (completionsAllowed && digits.length() > 1 && settings.getChinesePrefixCompletion() && !dbWords.isEmpty()) {
			ChinesePrefixIndex index = prefixes();
			if (index != null) {
				String first = nativeWord(dbWords.get(0));
				LinkedHashSet<String> unique = new LinkedHashSet<>(dbWords);
				ArrayList<String> additions = new ArrayList<>();
				for (ChinesePrefixIndex.Entry entry : index.lookup(first, 12)) {
					String raw = entry.transcription + entry.word;
					if (allowed(raw) && unique.add(raw)) additions.add(raw);
				}
				CommonHanCharacters common = commonCharacters();
				completed = ChineseCandidateRanking.merge(dbWords, additions,
					raw -> PinyinSyllables.getSequence(reading(raw)).equals(digits) ? index.singleCharacterFrequency(raw) : 0,
					raw -> {
						String word = nativeWord(raw);
						boolean exact = PinyinSyllables.getSequence(reading(raw)).equals(digits);
						if (!exact) return false;
						if (word.codePointCount(0, word.length()) > 1) return true;
						boolean commonCharacter = common == null || common.containsOnlyCommonHan(word);
						return commonCharacter && (!index.hasSingleCharacterFrequencies() || index.singleCharacterFrequency(raw) > 0)
							|| DataStore.isCustomWord(language, raw);
					});
			}
		}
		ArrayList<String> rankedSnapshot = completed;
		mainHandler.post(() -> {
			if (request != generation.get() || packGeneration != ChinesePack.getGeneration()
				|| !enabled.equals(ChineseDomains.enabled(settings.getContext()))) return;
			phoneticWords = phoneticSnapshot;
			lastDomainReadings = domainReadings;
			lastDomainCatalog = domainCatalog;
			domainReadings = domains;
			domainCatalog = catalog;
			super.onDbWords(rankedSnapshot, retry);
		});
	}

	@Override public void stripNativeWords() {
		if (!areThereDbWords) return;
		TreeSet<String> choices = new TreeSet<>();
		for (String raw : phoneticWords) {
			ChineseDomainIndex.Entry domain = domainReadings.get(raw);
			if (domain != null) { choices.add(domain.reading); continue; }
			List<String> syllables = PinyinSyllables.choicesForStoredWord(raw);
			if (syllables.isEmpty() && nativeWord(raw).isEmpty()) choices.add(reading(raw));
			else choices.addAll(syllables);
		}
		words = new ArrayList<>(choices);
	}

	@Override public void onAcceptIdeogram(String word) throws InvalidLanguageCharactersException {
		String reading = getTranscription(word).replace("'", "");
		if (reading.isEmpty()) return;
		String raw = reading + word;
		ChineseDomainIndex.Entry domain = domainReadings.get(raw);
		ChineseDomainIndex catalog = domainCatalog;
		if (domain == null) { domain = lastDomainReadings.get(raw); catalog = lastDomainCatalog; }
		if (domain != null) {
			final ChineseDomainIndex origin = catalog;
			final int mask = domain.domainMask;
			final long packGeneration = ChinesePack.getGeneration();
			if (settings.getPredictTopWords() && origin != null) SupremeExecutor.execute(() -> {
				if (packGeneration == ChinesePack.getGeneration() && (mask & origin.maskFor(ChineseDomains.enabled(settings.getContext()))) != 0) {
					ChineseDomains.learn(settings.getContext(), raw);
				}
			});
			return;
		}
		super.onAccept(raw, PinyinSyllables.getSequence(reading));
	}

	private boolean protectedBase(String raw, String digits) {
		if (DataStore.isCustomWord(language, raw)) return true;
		String word = nativeWord(raw);
		if (word.codePointCount(0, word.length()) != 1 || !PinyinSyllables.getSequence(reading(raw)).equals(digits)) return false;
		CommonHanCharacters common = commonCharacters();
		ChinesePrefixIndex index = prefixes();
		return (common == null || common.containsOnlyCommonHan(word))
			&& (index == null || !index.hasSingleCharacterFrequencies() || index.singleCharacterFrequency(raw) > 0);
	}

	private static boolean matchesReading(ChineseDomainIndex.Entry entry, String selected) {
		if (selected == null || selected.isEmpty()) return true;
		if (selected.indexOf('\'') >= 0 || PinyinSyllables.isSyllable(selected)) {
			return entry.reading.equals(selected) || entry.reading.startsWith(selected + "'");
		}
		return PinyinSyllables.compact(entry.reading).startsWith(PinyinSyllables.compact(selected));
	}

	private boolean allowed(String raw) {
		if (!settings.getChineseCommonOnly()) return true;
		refreshCaches();
		CommonHanCharacters common = commonCharacters();
		return common == null || common.containsOnlyCommonHan(nativeWord(raw)) || DataStore.isCustomWord(language, raw);
	}

	private ChinesePrefixIndex prefixes() {
		refreshCaches();
		if (!prefixesAttempted) {
			prefixesAttempted = true;
			try (InputStream stream = ChinesePack.open(settings.getContext(), ChinesePrefixIndex.FILE_NAME)) { prefixes = ChinesePrefixIndex.load(stream); }
			catch (IOException e) { Logger.w("ChinesePredictions", "Prefix index unavailable: " + e.getMessage()); }
		}
		return prefixes;
	}

	private CommonHanCharacters commonCharacters() {
		if (!commonCharactersAttempted) {
			commonCharactersAttempted = true;
			try (InputStream stream = ChinesePack.open(settings.getContext(), CommonHanCharacters.FILE_NAME)) { commonCharacters = CommonHanCharacters.load(stream); }
			catch (IOException e) { Logger.w("ChinesePredictions", "Common-character table unavailable: " + e.getMessage()); }
		}
		return commonCharacters;
	}

	private void refreshCaches() {
		long current = ChinesePack.getGeneration();
		if (cachedPackGeneration != current) {
			prefixes = null;
			commonCharacters = null;
			prefixesAttempted = false;
			commonCharactersAttempted = false;
			cachedPackGeneration = current;
		}
	}

	private static String reading(String raw) { return raw.substring(0, TextTools.lastIndexOfLatin(raw) + 1); }
	private static String nativeWord(String raw) { return raw.substring(TextTools.lastIndexOfLatin(raw) + 1); }
}
