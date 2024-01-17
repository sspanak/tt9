package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import io.github.sspanak.tt9.languages.Language;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Index;

@Entity
public class SequenceRange {
	@Id public long id;
	@Index public String sequence;
	public short langId;
	public int start;
	public int end;

	public static SequenceRange create(@NonNull Language language, @NonNull String sequence, int start) {
		SequenceRange range = new SequenceRange();
		range.langId = (short)language.getId();
		range.sequence = sequence;
		range.start = start;

		return range;
	}

	public void endAt(int position) {
		end = position;
	}

	@NonNull
	@Override
	public String toString() {
		return langId + " | " + sequence + " -> " + " [" + start + ", " + end + "]";
	}
}
