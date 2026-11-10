package io.github.sspanak.tt9.chinese;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/** Factory phrase continuations, indexed by their native Han prefix. */
public final class ChinesePrefixIndex {
	public static final String FILE_NAME = "zh-simplified-prefixes.bin";
	public static final int MAX_DIRECT_RESULTS = 16;
	public static final int MAX_FIRST_KEY_RESULTS = 32;
	public static final int MIN_FREQUENCY = 5000;
	public static final int MIN_PRIMARY_CHARACTER_FREQUENCY = 5800;
	private static final int MAGIC = 0x54543950; // TT9P
	private static final int VERSION = 2;
	private static final int MAX_FILE_SIZE = 8 * 1024 * 1024;
	private final byte[] data;
	private final int[] phraseOffsets;
	private final int[] prefixOffsets;
	private final int[] singleCharacterOffsets;
	private final List<List<Entry>> firstKeyCharacters = new ArrayList<>(8);

	private ChinesePrefixIndex(byte[] data) throws IOException {
		this.data = data;
		for (int i = 0; i < 8; i++) firstKeyCharacters.add(new ArrayList<>());
		Cursor cursor = new Cursor(data, 0);
		int magic = cursor.integer(), version = cursor.integer();
		if (magic != MAGIC || version < 1 || version > VERSION) {
			throw new IOException("Unsupported Chinese prefix index");
		}
		int phraseCount = cursor.integer();
		int prefixCount = cursor.integer();
		if (phraseCount < 0 || prefixCount < 0 || phraseCount > data.length / 8 || prefixCount > data.length / 3) {
			throw new IOException("Invalid Chinese prefix index counts");
		}
		phraseOffsets = new int[phraseCount];
		prefixOffsets = new int[prefixCount];
		for (int i = 0; i < phraseCount; i++) {
			phraseOffsets[i] = cursor.position;
			cursor.skipString();
			cursor.skipString();
			cursor.integer();
		}
		for (int i = 0; i < prefixCount; i++) {
			prefixOffsets[i] = cursor.position;
			cursor.skipString();
			int count = cursor.unsignedByte();
			if (count > MAX_DIRECT_RESULTS) throw new IOException("Too many Chinese prefix results");
			for (int j = 0; j < count; j++) {
				int phrase = cursor.integer();
				if (phrase < 0 || phrase >= phraseCount) throw new IOException("Invalid Chinese prefix result");
			}
		}
		int singleCharacterCount = version >= 2 ? cursor.integer() : 0;
		if (singleCharacterCount < 0 || singleCharacterCount > data.length / 6) throw new IOException("Invalid Chinese character-frequency count");
		singleCharacterOffsets = new int[singleCharacterCount];
		String previous = "";
		for (int i = 0; i < singleCharacterCount; i++) {
			singleCharacterOffsets[i] = cursor.position;
			String raw = cursor.string();
			int frequency = cursor.integer();
			if (raw.compareTo(previous) <= 0 || frequency < MIN_PRIMARY_CHARACTER_FREQUENCY) throw new IOException("Invalid Chinese character frequencies");
			indexFirstKeyCharacter(raw, frequency);
			previous = raw;
		}
		if (cursor.position != data.length) throw new IOException("Unexpected Chinese prefix index data");
	}

	/** Corpus weights of frequent single-character readings, found without scanning the dictionary. */
	public int singleCharacterFrequency(String raw) {
		if (raw == null) return 0;
		int low = 0, high = singleCharacterOffsets.length - 1;
		try {
			while (low <= high) {
				int middle = (low + high) >>> 1;
				Cursor cursor = new Cursor(data, singleCharacterOffsets[middle]);
				int comparison = cursor.string().compareTo(raw);
				if (comparison < 0) low = middle + 1;
				else if (comparison > 0) high = middle - 1;
				else return cursor.integer();
			}
		} catch (IOException impossible) {
			throw new IllegalStateException("Validated Chinese character frequencies changed", impossible);
		}
		return 0;
	}

	public boolean hasSingleCharacterFrequencies() { return singleCharacterOffsets.length != 0; }

	/** Frequent characters for a first key, even when an exact vowel reading already exists. */
	public List<Entry> lookupFirstKey(String digits, String selectedReading, int limit) {
		if (digits == null || digits.length() != 1 || digits.charAt(0) < '2' || digits.charAt(0) > '9'
			|| selectedReading != null && !selectedReading.isEmpty() || limit <= 0) return Collections.emptyList();
		List<Entry> bucket = firstKeyCharacters.get(digits.charAt(0) - '2');
		return new ArrayList<>(bucket.subList(0, Math.min(limit, bucket.size())));
	}

	private void indexFirstKeyCharacter(String raw, int frequency) throws IOException {
		int nativeStart = 0;
		while (nativeStart < raw.length() && raw.charAt(nativeStart) >= 'A' && raw.charAt(nativeStart) <= 'Z') nativeStart++;
		if (nativeStart == 0 || nativeStart >= raw.length() || raw.codePointCount(nativeStart, raw.length()) != 1) {
			throw new IOException("Invalid Chinese single-character reading");
		}
		Entry entry = new Entry(raw.substring(nativeStart), raw.substring(0, nativeStart), frequency);
		List<Entry> bucket = firstKeyCharacters.get(entry.sequence.charAt(0) - '2');
		for (int i = 0; i < bucket.size(); i++) {
			if (!bucket.get(i).word.equals(entry.word)) continue;
			if (bucket.get(i).frequency >= frequency) return;
			bucket.remove(i);
			break;
		}
		int position = 0;
		while (position < bucket.size() && bucket.get(position).frequency >= frequency) position++;
		if (position >= MAX_FIRST_KEY_RESULTS) return;
		bucket.add(position, entry);
		if (bucket.size() > MAX_FIRST_KEY_RESULTS) bucket.remove(MAX_FIRST_KEY_RESULTS);
	}

	/** The stream is a raw ZIP entry or its extracted file; ownership remains with the caller. */
	public static ChinesePrefixIndex load(InputStream stream) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[16384];
		int count;
		while ((count = stream.read(buffer)) != -1) {
			if (output.size() + count > MAX_FILE_SIZE) throw new IOException("Chinese prefix index is too large");
			output.write(buffer, 0, count);
		}
		return new ChinesePrefixIndex(output.toByteArray());
	}

	/**
	 * Returns ranked full phrases, with existing short question forms beside their parent phrase.
	 * At most 16 common direct continuations and one level of indexed lookahead are examined.
	 */
	public List<Entry> lookup(String prefix, int limit) {
		if (prefix == null || prefix.isEmpty() || limit <= 0) return Collections.emptyList();
		List<Entry> result = new ArrayList<>();
		HashSet<String> seen = new HashSet<>();
		for (Entry entry : direct(prefix)) {
			if (seen.add(entry.word)) result.add(entry);
			if (result.size() >= limit) break;
			for (Entry question : direct(entry.word)) {
				if (!question.word.startsWith(entry.word)) continue;
				String suffix = question.word.substring(entry.word.length());
				if (suffix.length() == 1 && "吗呢吧".contains(suffix) && seen.add(question.word)) {
					result.add(question);
					if (result.size() >= limit) break;
				}
			}
			if (result.size() >= limit) break;
		}
		return result;
	}

	private List<Entry> direct(String prefix) {
		int low = 0;
		int high = prefixOffsets.length - 1;
		try {
			while (low <= high) {
				int middle = (low + high) >>> 1;
				Cursor cursor = new Cursor(data, prefixOffsets[middle]);
				int comparison = cursor.string().compareTo(prefix);
				if (comparison < 0) {
					low = middle + 1;
				} else if (comparison > 0) {
					high = middle - 1;
				} else {
					int count = cursor.unsignedByte();
					List<Entry> entries = new ArrayList<>(count);
					for (int i = 0; i < count; i++) {
						Cursor phrase = new Cursor(data, phraseOffsets[cursor.integer()]);
						entries.add(new Entry(phrase.string(), phrase.string(), phrase.integer()));
					}
					return entries;
				}
			}
		} catch (IOException impossible) {
			throw new IllegalStateException("Validated Chinese prefix index changed", impossible);
		}
		return Collections.emptyList();
	}

	public static final class Entry {
		public final String word;
		public final String transcription;
		public final String sequence;
		public final int frequency;

		public Entry(String word, String transcription, int frequency) {
			this.word = word;
			this.transcription = transcription;
			this.sequence = PinyinSyllables.getSequence(transcription);
			this.frequency = frequency;
		}
	}

	private static final class Cursor {
		private final byte[] data;
		private int position;

		private Cursor(byte[] data, int position) {
			this.data = data;
			this.position = position;
		}

		private int unsignedByte() throws IOException {
			if (position >= data.length) throw new IOException("Truncated Chinese prefix index");
			return data[position++] & 0xff;
		}

		private int integer() throws IOException {
			return (unsignedByte() << 24) | (unsignedByte() << 16) | (unsignedByte() << 8) | unsignedByte();
		}

		private int stringLength() throws IOException {
			int length = (unsignedByte() << 8) | unsignedByte();
			if (length > data.length - position) throw new IOException("Truncated Chinese prefix string");
			return length;
		}

		private void skipString() throws IOException {
			int length = stringLength();
			position += length;
		}

		private String string() throws IOException {
			int length = stringLength();
			String value = new String(data, position, length, StandardCharsets.UTF_8);
			position += length;
			return value;
		}
	}
}
