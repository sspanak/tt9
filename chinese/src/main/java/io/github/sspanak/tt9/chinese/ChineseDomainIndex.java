package io.github.sspanak.tt9.chinese;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Optional vocabulary with a sorted T9 index; strings are decoded only for returned entries. */
public final class ChineseDomainIndex {
	public static final String FILE_NAME = "chinese-domains.bin";
	public static final int MAX_BYTES = 4 * 1024 * 1024;
	public static final int MAX_ENTRIES = 50000;
	public static final int MAX_RESULTS = 128;
	public static final int MAX_PREFIX_SCAN = 1024;
	private static final int MAGIC = 0x54543944; // TT9D
	private static final int VERSION = 1;
	private final byte[] data;
	private final int[] offsets;
	private final List<Domain> domains;
	private final int allDomains;

	private ChineseDomainIndex(byte[] data) throws IOException {
		this.data = data;
		Cursor cursor = new Cursor(data, 0);
		if (cursor.integer() != MAGIC || cursor.integer() != VERSION) throw new IOException("Unsupported Chinese domain index");
		int domainCount = cursor.integer();
		if (domainCount < 1 || domainCount > 30) throw new IOException("Invalid Chinese domain count");
		allDomains = (1 << domainCount) - 1;
		ArrayList<Domain> catalog = new ArrayList<>(domainCount);
		Set<String> ids = new HashSet<>();
		for (int i = 0; i < domainCount; i++) {
			String id = cursor.string(true);
			if (!id.matches("[a-z][a-z0-9-]{0,47}") || !ids.add(id)) throw new IOException("Invalid or duplicate Chinese domain ID");
			catalog.add(new Domain(id, metadata(cursor, 256), metadata(cursor, 256), metadata(cursor, 512), metadata(cursor, 4096), 0));
		}
		int count = cursor.integer();
		if (count < 0 || count > MAX_ENTRIES || count > (data.length - cursor.position) / 18) throw new IOException("Invalid Chinese domain entry count");
		offsets = new int[count];
		int[] counts = new int[domainCount];
		Set<String> unique = new HashSet<>();
		String previousSequence = "", previousKey = "";
		int previousWeight = 0;
		for (int i = 0; i < count; i++) {
			offsets[i] = cursor.position;
			String sequence = cursor.string(true);
			int mask = cursor.integer();
			String word = cursor.string(true), reading = cursor.string(true);
			int weight = cursor.integer();
			if (!isSequence(sequence) || mask <= 0 || (mask & ~allDomains) != 0 || weight < 0) throw new IOException("Invalid Chinese domain entry");
			ChineseUserWord parsed;
			try { parsed = ChineseUserWord.create(word, reading); }
			catch (IllegalArgumentException invalid) { throw new IOException("Invalid Chinese domain word or reading", invalid); }
			if (!word.equals(parsed.word()) || !reading.equals(parsed.reading()) || !sequence.equals(parsed.sequence())) throw new IOException("Noncanonical Chinese domain reading");
			String raw = parsed.storedWord();
			String key = raw + '\0' + reading;
			if (!unique.add(key)) throw new IOException("Duplicate Chinese domain reading");
			int comparison = sequence.compareTo(previousSequence);
			if (comparison < 0 || (comparison == 0 && (weight > previousWeight || weight == previousWeight && key.compareTo(previousKey) < 0))) {
				throw new IOException("Unsorted Chinese domain index");
			}
			previousSequence = sequence;
			previousWeight = weight;
			previousKey = key;
			for (int domain = 0; domain < domainCount; domain++) if ((mask & (1 << domain)) != 0) counts[domain]++;
		}
		if (cursor.position != data.length) throw new IOException("Unexpected Chinese domain index data");
		for (int i = 0; i < domainCount; i++) {
			Domain domain = catalog.get(i);
			catalog.set(i, new Domain(domain.id, domain.labelEn, domain.labelZh, domain.license, domain.source, counts[i]));
		}
		domains = Collections.unmodifiableList(catalog);
	}

	public static ChineseDomainIndex load(InputStream stream) throws IOException {
		if (stream == null) throw new IOException("Missing Chinese domain index");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[16384];
		for (int size; (size = stream.read(buffer)) != -1;) {
			if (output.size() + size > MAX_BYTES) throw new IOException("Chinese domain index is too large");
			output.write(buffer, 0, size);
		}
		return new ChineseDomainIndex(output.toByteArray());
	}

	public List<Domain> getDomains() { return domains; }

	/** Preferences retain stable IDs, so catalog reordering cannot enable a different vocabulary. */
	public int maskFor(Set<String> ids) {
		if (ids == null || ids.isEmpty()) return 0;
		int mask = 0;
		for (int i = 0; i < domains.size(); i++) if (ids.contains(domains.get(i).id)) mask |= 1 << i;
		return mask;
	}

	public int count(Domain domain) { return domain == null ? 0 : count(domain.id); }
	public int count(String id) {
		for (Domain domain : domains) if (domain.id.equals(id)) return domain.wordCount;
		return 0;
	}

	public List<Entry> lookupExact(String sequence, int activeMask, int limit) {
		int mask = activeMask & allDomains;
		if (!isSequence(sequence) || mask == 0 || limit <= 0) return Collections.emptyList();
		ArrayList<Entry> result = new ArrayList<>();
		for (int i = lowerBound(sequence); i < offsets.length && compareSequence(i, sequence) == 0; i++) {
			Entry entry = entry(i, mask);
			if (entry != null) result.add(entry);
			if (result.size() >= Math.min(limit, MAX_RESULTS)) break;
		}
		return result;
	}

	/** Bounded fallback for an unfinished reading, called only when exact sources have no match. */
	public List<Entry> lookupPrefix(String prefix, int activeMask, int limit) {
		int mask = activeMask & allDomains;
		if (!isSequence(prefix) || mask == 0 || limit <= 0) return Collections.emptyList();
		ArrayList<Entry> result = new ArrayList<>();
		for (int i = lowerBound(prefix), scanned = 0; i < offsets.length && scanned < MAX_PREFIX_SCAN; i++, scanned++) {
			int length = stringLength(offsets[i]);
			if (!startsWithSequence(i, prefix)) break;
			if (length == prefix.length()) continue;
			Entry entry = entry(i, mask);
			if (entry != null) result.add(entry);
			if (result.size() >= Math.min(limit, MAX_RESULTS)) break;
		}
		return result;
	}

	private int lowerBound(String sequence) {
		int low = 0, high = offsets.length;
		while (low < high) {
			int middle = (low + high) >>> 1;
			if (compareSequence(middle, sequence) < 0) low = middle + 1;
			else high = middle;
		}
		return low;
	}

	private int compareSequence(int index, String sequence) {
		int start = offsets[index] + 2, length = stringLength(offsets[index]);
		for (int i = 0; i < Math.min(length, sequence.length()); i++) {
			int comparison = (data[start + i] & 0xff) - sequence.charAt(i);
			if (comparison != 0) return comparison;
		}
		return Integer.compare(length, sequence.length());
	}

	private boolean startsWithSequence(int index, String prefix) {
		int start = offsets[index] + 2;
		if (stringLength(offsets[index]) < prefix.length()) return false;
		for (int i = 0; i < prefix.length(); i++) if (data[start + i] != prefix.charAt(i)) return false;
		return true;
	}

	private int stringLength(int offset) { return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff); }

	private Entry entry(int index, int activeMask) {
		try {
			Cursor cursor = new Cursor(data, offsets[index]);
			String sequence = cursor.string(false);
			int mask = cursor.integer();
			if ((mask & activeMask) == 0) return null;
			return new Entry(cursor.string(false), cursor.string(false), sequence, mask, cursor.integer());
		} catch (IOException impossible) { throw new IllegalStateException("Validated Chinese domain index changed", impossible); }
	}

	private static boolean isSequence(String sequence) {
		if (sequence == null || sequence.isEmpty() || sequence.length() > 32 * 6) return false;
		for (int i = 0; i < sequence.length(); i++) if (sequence.charAt(i) < '2' || sequence.charAt(i) > '9') return false;
		return true;
	}

	private static String metadata(Cursor cursor, int maxLength) throws IOException {
		String value = cursor.string(true);
		if (value.isEmpty() || value.length() > maxLength || !value.equals(value.trim()) || value.chars().anyMatch(c -> c < 32 || c == 127)) throw new IOException("Invalid Chinese domain metadata");
		return value;
	}

	public static final class Domain {
		public final String id, labelEn, labelZh, license, source;
		public final int wordCount;
		private Domain(String id, String labelEn, String labelZh, String license, String source, int wordCount) {
			this.id = id; this.labelEn = labelEn; this.labelZh = labelZh; this.license = license; this.source = source; this.wordCount = wordCount;
		}
	}

	public static final class Entry {
		public final String word, reading, sequence;
		public final int domainMask, weight;
		private final String raw;
		private Entry(String word, String reading, String sequence, int domainMask, int weight) {
			this.word = word; this.reading = reading; this.sequence = sequence; this.domainMask = domainMask; this.weight = weight;
			raw = reading.replace("'", "") + word;
		}
		public String raw() { return raw; }
	}

	private static final class Cursor {
		private final byte[] data;
		private int position;
		private Cursor(byte[] data, int position) { this.data = data; this.position = position; }
		private int unsignedByte() throws IOException {
			if (position >= data.length) throw new IOException("Truncated Chinese domain index");
			return data[position++] & 0xff;
		}
		private int integer() throws IOException { return (unsignedByte() << 24) | (unsignedByte() << 16) | (unsignedByte() << 8) | unsignedByte(); }
		private String string(boolean strict) throws IOException {
			int length = (unsignedByte() << 8) | unsignedByte();
			if (length > data.length - position) throw new IOException("Truncated Chinese domain string");
			String value;
			try {
				value = strict ? StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(data, position, length)).toString() : new String(data, position, length, StandardCharsets.UTF_8);
			} catch (CharacterCodingException invalid) { throw new IOException("Invalid Chinese domain UTF-8", invalid); }
			position += length;
			return value;
		}
	}
}
