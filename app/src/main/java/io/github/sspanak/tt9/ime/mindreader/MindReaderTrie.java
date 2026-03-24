package io.github.sspanak.tt9.ime.mindreader;

import android.util.SparseArray;

import androidx.annotation.NonNull;

class MindReaderTrie {
	private static class Node {
		SparseArray<Node> children = null; // lazy: allocated on first insert
		int tokenId = -1; // -1 = not found
	}

	private final Node root = new Node();

	void add(@NonNull String word, int tokenId) {
		Node node = root;

		// reversed insert
		for (int i = word.length(); i > 0; ) {
			int cp = word.codePointBefore(i);
			i -= Character.charCount(cp);

			if (node.children == null) {
				node.children = new SparseArray<>();
			}
			Node child = node.children.get(cp);
			if (child == null) {
				child = new Node();
				node.children.put(cp, child);
			}
			node = child;
		}

		node.tokenId = tokenId;
	}

	/**
	 * Return the longest matching word ending at position `end` (exclusive)
	 */
	String getLongestWord(@NonNull String text, int end, String[] tokens) {
		Node node = root;
		String result = null;

		for (int i = end; i > 0; ) {
			int cp = text.codePointBefore(i);
			i -= Character.charCount(cp);

			if (node.children == null) break;
			node = node.children.get(cp);
			if (node == null) break;

			if (node.tokenId != -1) {
				result = tokens[node.tokenId];
			}
		}

		return result;
	}
}
