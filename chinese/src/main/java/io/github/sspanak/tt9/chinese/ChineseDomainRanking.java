package io.github.sspanak.tt9.chinese;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/** Domain learning ranks only optional additions; existing factory and personal order is retained. */
public final class ChineseDomainRanking {
	private ChineseDomainRanking() {}

	public static List<ChineseDomainIndex.Entry> additions(List<String> base, List<ChineseDomainIndex.Entry> domains,
		ToIntFunction<ChineseDomainIndex.Entry> learning, int limit) {
		if (limit <= 0 || domains.isEmpty()) return new ArrayList<>();
		ArrayList<Scored> ranked = new ArrayList<>();
		for (ChineseDomainIndex.Entry entry : domains) ranked.add(new Scored(entry, Math.max(0, learning.applyAsInt(entry))));
		ranked.sort(Comparator.comparingInt((Scored item) -> item.learned).reversed()
			.thenComparing(Comparator.comparingInt((Scored item) -> item.entry.weight).reversed())
			.thenComparing(item -> item.entry.raw()).thenComparing(item -> item.entry.reading));
		ArrayList<ChineseDomainIndex.Entry> result = new ArrayList<>();
		Set<String> seen = new HashSet<>(base);
		for (Scored item : ranked) {
			if (seen.add(item.entry.raw())) result.add(item.entry);
			if (result.size() >= Math.min(limit, ChineseDomainIndex.MAX_RESULTS)) break;
		}
		return result;
	}

	public static ArrayList<String> merge(List<String> base, List<ChineseDomainIndex.Entry> domains,
		ToIntFunction<ChineseDomainIndex.Entry> learning, int limit) {
		ArrayList<String> result = new ArrayList<>(base);
		for (ChineseDomainIndex.Entry entry : additions(base, domains, learning, limit)) result.add(entry.raw());
		return result;
	}

	/** An enabled domain can promote existing terms without displacing the first or protected choices. */
	public static ArrayList<String> prioritize(List<String> base, List<ChineseDomainIndex.Entry> domains,
		ToIntFunction<ChineseDomainIndex.Entry> learning, Predicate<String> protectedBase, int limit) {
		if (domains.isEmpty() || limit <= 0) return new ArrayList<>(base);
		ArrayList<String> result = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		if (!base.isEmpty()) { result.add(base.get(0)); seen.add(base.get(0)); }
		for (String raw : base) if (protectedBase.test(raw) && seen.add(raw)) result.add(raw);
		for (ChineseDomainIndex.Entry entry : additions(List.of(), domains, learning, limit)) {
			if (seen.add(entry.raw())) result.add(entry.raw());
		}
		for (String raw : base) if (seen.add(raw)) result.add(raw);
		return result;
	}

	private record Scored(ChineseDomainIndex.Entry entry, int learned) {}
}
