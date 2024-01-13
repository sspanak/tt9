package io.github.sspanak.tt9.db.objectbox;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class SequenceStat {
	private final String sequence;
	private final int sequenceLength;
	private int siblings = 0;
	private final Set<String> children = new HashSet<>();
	private final Set<String> grandChildren = new HashSet<>();

	public SequenceStat(@NonNull String sequence) {
		this.sequence = sequence;
		sequenceLength = sequence.length();
	}

	public void addSibling(String sibling) {
		if (sibling.equals(sequence)) {
			siblings++;
		}
	}

	public void addChild(String child) {
		int childLength = child.length();

		if (child.startsWith(sequence)) {
			if (childLength == sequenceLength + 1) {
				children.add(child);
			} else if (childLength == sequenceLength + 2) {
				grandChildren.add(child);
			}
		}
	}

	public int childrenCount() {
		return children.size();
	}

	public int grandChildrenCount() {
		return grandChildren.size();
	}

	public int totalChildrenCount() {
		return childrenCount() + grandChildrenCount();
	}

	public String getSequence() {
		return sequence;
	}

	@NonNull
	@Override
	public String toString() {
		return "Sequence: " + sequence + ", siblings: " + siblings + ", children: " + childrenCount() + ", grandchildren: " + grandChildrenCount();
	}
}
