package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class ShortSequenceStats extends HashMap<String, SequenceStat> {
	public void add(@NonNull String sequence) {
		int length = sequence.length();
		if (length > 1 && length < 4) {
			SequenceStat stats = get(sequence);
			if (stats != null) {
				stats.addSibling(sequence);
			} else {
				put(sequence, new SequenceStat(sequence));
			}
		}
	}

	public void addChild(@NonNull String child) {
		int childLength = child.length();
		if (childLength < 3 || childLength > 5) {
			return;
		}

		String root2 = child.substring(0, 2);
		String root3 = child.substring(0, 3);

		SequenceStat stats2 = get(root2);
		if (stats2 != null) {
			stats2.addChild(child);
		}

		SequenceStat stats3 = get(root3);
		if (stats3 != null) {
			stats3.addChild(child);
		}
	}

	@NonNull
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (SequenceStat stat : values()) {
			sb.append(stat).append("\n");
		}

		return sb.toString();
	}

	public String getMax() {
		int maxChildren = 0;
		int maxGrandchildren = 0;
		int maxTotal = 0;

		String maxChildrenSeq= "";
		String maxGrandchildrenSeq= "";
		String maxTotalSeq= "";

		for (SequenceStat stat : values()) {
			if (maxChildren < stat.childrenCount()) {
				maxChildrenSeq = stat.getSequence();
				maxChildren = stat.childrenCount();
			}

			if (maxGrandchildren < stat.grandChildrenCount()) {
				maxGrandchildrenSeq = stat.getSequence();
				maxGrandchildren = stat.grandChildrenCount();
			}

			if (maxTotal < stat.totalChildrenCount()) {
				maxTotalSeq = stat.getSequence();
				maxTotal = stat.totalChildrenCount();
			}
		}

		return
			"Max Children Sequence: " + maxChildrenSeq + ", children: " + maxChildren + "\n" +
			"Max Children Sequence: " + maxGrandchildrenSeq + ", grandchildren: " + maxGrandchildren + "\n" +
			"Max Total Children Sequence: " + maxTotalSeq + ", total children: " + maxTotal + "\n";
	}
}

