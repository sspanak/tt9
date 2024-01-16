package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

import io.github.sspanak.tt9.languages.Language;

public class SequenceIndex {
	private final HashMap<String, SequenceRange> index = new HashMap<>();
	private final Language language;
	private String lastSequence = "";

	public SequenceIndex(@NonNull Language language) {
		this.language = language;
	}

	public void add(@NonNull String sequence) {
		// @todo: implement ...
	}

	public void append(@NonNull String sequence, int startPosition) {
		if (!lastSequence.equals(sequence)) {
			endRange(lastSequence, startPosition - 1);
			startRange(sequence, startPosition);
			lastSequence = sequence;
		}
	}

	@Nullable public SequenceRange find(String sequence) {
		return index.containsKey(sequence) ? index.get(sequence) : null;
	}

	private void startRange(@NonNull String sequence, int startPosition) {
		if (!sequence.isEmpty() && !index.containsKey(sequence)) {
			index.put(sequence, SequenceRange.create(language, sequence, startPosition));
		}
	}

	private void endRange(@NonNull String sequence, int endPosition) {
		if (!index.containsKey(sequence)) {
			return;
		}

		SequenceRange item = index.get(sequence);
		if (item != null) {
			item.endAt(endPosition);
		}
	}

	public String toDebugString(int MAX_ITEMS) {
		int end = Math.min(index.keySet().size(), MAX_ITEMS);

		StringBuilder sb = new StringBuilder();
		int i = MAX_ITEMS;
		for (SequenceRange range : index.values()) {
			sb.append(range).append("\n");
			if (--i == 0) {
				break;
			}
		}

		if (index.keySet().size() > MAX_ITEMS) {
			sb.append("...\n(Total: ").append(index.keySet().size()).append(")");
		}

		return sb.toString();
	}
}

