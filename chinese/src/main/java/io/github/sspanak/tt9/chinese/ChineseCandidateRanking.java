package io.github.sspanak.tt9.chinese;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/** Stable tiers for exact input, optional phrase completions, and secondary characters. */
public final class ChineseCandidateRanking {
	private ChineseCandidateRanking() {}

	/**
	 * Factory weights only identify ordinary primary characters; the database order still
	 * decides their order. Words promoted above the strongest factory character retain
	 * that learned position, including multiple promoted rare characters.
	 */
	public static ArrayList<String> merge(List<String> words, List<String> completions,
		ToIntFunction<String> factoryFrequency, Predicate<String> primary) {
		if (words.isEmpty() || completions.isEmpty()) return new ArrayList<>(words);
		int strongest = 0, strongestFrequency = 0;
		for (int i = 0; i < words.size(); i++) {
			int frequency = factoryFrequency.applyAsInt(words.get(i));
			if (frequency > strongestFrequency) { strongest = i; strongestFrequency = frequency; }
		}
		LinkedHashSet<String> result = new LinkedHashSet<>();
		ArrayList<String> secondary = new ArrayList<>();
		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			if (i <= strongest || primary.test(word)) result.add(word);
			else secondary.add(word);
		}
		result.addAll(completions);
		result.addAll(secondary);
		return new ArrayList<>(result);
	}
}
