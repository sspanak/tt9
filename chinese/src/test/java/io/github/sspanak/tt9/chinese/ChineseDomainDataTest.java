package io.github.sspanak.tt9.chinese;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/** Checks the actual Groovy build output through the Java reader, against the maintained TSVs. */
public class ChineseDomainDataTest {
	private static final Set<String> EXPECTED_DOMAINS = Set.of("medical", "finance", "technology", "acg", "psychology", "biochemistry", "engineering");
	private static ChineseDomainIndex index;
	private static final List<SourceRow> sourceRows = new ArrayList<>();
	private static final Map<String, Expected> expected = new LinkedHashMap<>();
	private static final Map<String, Set<String>> keysBySequence = new HashMap<>();
	private static final Map<String, Integer> domainCounts = new HashMap<>();
	private static int allDomains;

	@BeforeClass public static void readProductionDataAndIndependentTsvReference() throws Exception {
		Path binary = Path.of(requiredProperty("tt9.chineseDomainIndex"));
		Path sources = Path.of(requiredProperty("tt9.chineseDomainsSource"));
		assertTrue("The domain build task must generate its production binary", Files.isRegularFile(binary));
		assertTrue("The domain TSV source directory must exist", Files.isDirectory(sources));
		try (InputStream stream = Files.newInputStream(binary)) { index = ChineseDomainIndex.load(stream); }
		Set<String> catalogIds = index.getDomains().stream().map(domain -> domain.id).collect(Collectors.toSet());
		assertEquals(EXPECTED_DOMAINS, catalogIds);
		try (Stream<Path> files = Files.list(sources)) {
			Set<String> sourceIds = files.map(path -> path.getFileName().toString()).filter(name -> name.endsWith(".tsv"))
				.map(name -> name.substring(0, name.length() - 4)).collect(Collectors.toSet());
			assertEquals("Every maintained TSV must be present in the generated catalog", sourceIds, catalogIds);
		}
		allDomains = index.maskFor(catalogIds);
		for (ChineseDomainIndex.Domain domain : index.getDomains()) {
			Path file = sources.resolve(domain.id + ".tsv");
			int mask = index.maskFor(Set.of(domain.id)), count = 0, lineNumber = 0;
			assertEquals("One stable domain ID must select exactly one bit", 1, Integer.bitCount(mask));
			Set<String> seen = new HashSet<>();
			for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
				lineNumber++;
				if (line.isEmpty() || line.startsWith("#")) continue;
				String location = file.getFileName() + ":" + lineNumber;
				String[] fields = line.split("\t", -1);
				assertEquals(location, 3, fields.length);
				ChineseUserWord parsed = ChineseUserWord.create(fields[0], fields[1]);
				assertEquals(location + " word", fields[0], parsed.word());
				assertEquals(location + " explicit reading", fields[1], parsed.reading());
				int weight = Integer.parseInt(fields[2]);
				assertEquals(location + " canonical weight", fields[2], Integer.toString(weight));
				assertTrue(location + " weight range", weight >= 0 && weight <= 9999);
				String key = key(parsed.word(), parsed.reading());
				assertTrue(location + " duplicate word/reading within one domain", seen.add(key));
				sourceRows.add(new SourceRow(location, key, parsed, weight, mask));
				Expected reference = expected.computeIfAbsent(key, ignored -> new Expected());
				reference.mask |= mask;
				reference.weight = Math.max(reference.weight, weight);
				keysBySequence.computeIfAbsent(parsed.sequence(), ignored -> new HashSet<>()).add(key);
				count++;
			}
			assertTrue("A catalog domain must contain source rows: " + domain.id, count > 0);
			domainCounts.put(domain.id, count);
		}
	}

	@Test public void productionCatalogCountsAndAttributionMatchEveryMaintainedDomain() {
		assertEquals(7, index.getDomains().size());
		for (ChineseDomainIndex.Domain domain : index.getDomains()) {
			assertFalse(domain.id + " English label", domain.labelEn.isBlank());
			assertFalse(domain.id + " Chinese label", domain.labelZh.isBlank());
			assertFalse(domain.id + " license", domain.license.isBlank());
			assertFalse(domain.id + " source attribution", domain.source.isBlank());
			assertEquals(domain.id + " TSV rows", domainCounts.get(domain.id).intValue(), domain.wordCount);
			assertEquals(domain.id + " count API", domainCounts.get(domain.id).intValue(), index.count(domain.id));
		}
		assertEquals("All source domain memberships are retained", sourceRows.size(), index.getDomains().stream().mapToInt(domain -> domain.wordCount).sum());
		assertTrue("Production data must exercise shared word/readings", expected.size() < sourceRows.size());
	}

	@Test public void everyProductionTsvRowRoundTripsWithMergedMasksAndMaximumWeight() {
		Map<String, Map<String, ChineseDomainIndex.Entry>> results = new HashMap<>();
		for (Map.Entry<String, Set<String>> group : keysBySequence.entrySet()) {
			assertTrue("Exact groups must fit the public lookup cap: " + group.getKey(), group.getValue().size() <= ChineseDomainIndex.MAX_RESULTS);
			Map<String, ChineseDomainIndex.Entry> actual = byIdentity(index.lookupExact(group.getKey(), allDomains, ChineseDomainIndex.MAX_RESULTS));
			assertEquals("Exact group " + group.getKey(), group.getValue(), actual.keySet());
			results.put(group.getKey(), actual);
		}
		for (SourceRow row : sourceRows) {
			ChineseDomainIndex.Entry actual = results.get(row.parsed.sequence()).get(row.key);
			Expected merged = expected.get(row.key);
			assertNotNull(row.location, actual);
			assertEquals(row.location + " word", row.parsed.word(), actual.word);
			assertEquals(row.location + " explicit reading", row.parsed.reading(), actual.reading);
			assertEquals(row.location + " sequence", row.parsed.sequence(), actual.sequence);
			assertEquals(row.location + " compact storage form", row.parsed.storedWord(), actual.raw());
			assertEquals(row.location + " merged domains", merged.mask, actual.domainMask);
			assertEquals(row.location + " maximum source weight", merged.weight, actual.weight);
			assertTrue(row.location + " includes this source domain", (actual.domainMask & row.mask) != 0);
			assertTrue(row.location + " retains at least this source weight", actual.weight >= row.weight);
		}
	}

	@Test public void zeroMaskAndIndividualDomainLookupsNeverLeakAnotherDomainsEntries() {
		for (String sequence : keysBySequence.keySet()) {
			assertTrue("Inactive exact " + sequence, index.lookupExact(sequence, 0, ChineseDomainIndex.MAX_RESULTS).isEmpty());
			assertTrue("Inactive prefix " + sequence, index.lookupPrefix(sequence, 0, ChineseDomainIndex.MAX_RESULTS).isEmpty());
		}
		for (ChineseDomainIndex.Domain domain : index.getDomains()) {
			int mask = index.maskFor(Set.of(domain.id));
			for (Map.Entry<String, Set<String>> group : keysBySequence.entrySet()) {
				Set<String> wanted = group.getValue().stream().filter(key -> (expected.get(key).mask & mask) != 0).collect(Collectors.toSet());
				List<ChineseDomainIndex.Entry> found = index.lookupExact(group.getKey(), mask, ChineseDomainIndex.MAX_RESULTS);
				assertEquals(domain.id + " exact " + group.getKey(), wanted, byIdentity(found).keySet());
				for (ChineseDomainIndex.Entry entry : found) assertTrue(domain.id + " active membership", (entry.domainMask & mask) != 0);
			}
			String prefix = sourceRows.stream().filter(row -> row.mask == mask).findFirst().orElseThrow().parsed.sequence();
			prefix = prefix.substring(0, prefix.length() - 1);
			List<ChineseDomainIndex.Entry> completions = index.lookupPrefix(prefix, mask, ChineseDomainIndex.MAX_RESULTS);
			assertFalse(domain.id + " production prefix sample", completions.isEmpty());
			for (ChineseDomainIndex.Entry entry : completions) {
				assertTrue(domain.id + " prefix membership", (entry.domainMask & mask) != 0);
				Expected reference = expected.get(key(entry.word, entry.reading));
				assertNotNull(domain.id + " prefix source row", reference);
				assertEquals(reference.mask, entry.domainMask);
			}
		}
	}

	private static String requiredProperty(String name) {
		String value = System.getProperty(name);
		assertNotNull("The feature's JVM test task must supply " + name, value);
		return value;
	}
	private static String key(String word, String reading) { return word + '\0' + reading; }
	private static Map<String, ChineseDomainIndex.Entry> byIdentity(List<ChineseDomainIndex.Entry> entries) {
		Map<String, ChineseDomainIndex.Entry> result = new HashMap<>();
		for (ChineseDomainIndex.Entry entry : entries) assertNull("No duplicate output word/reading", result.put(key(entry.word, entry.reading), entry));
		return result;
	}
	private record SourceRow(String location, String key, ChineseUserWord parsed, int weight, int mask) {}
	private static final class Expected {
		int weight, mask;
	}
}
