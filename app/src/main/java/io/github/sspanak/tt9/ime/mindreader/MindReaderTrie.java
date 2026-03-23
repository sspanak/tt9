package io.github.sspanak.tt9.ime.mindreader;

import androidx.annotation.NonNull;

import java.util.HashMap;

class MindReaderTrie {
	private static class Node {
		HashMap<Integer, Node> children = new HashMap<>();
		int tokenId = -1; // -1 = not found
	}

	private final Node root = new Node();

	void add(@NonNull String word, int tokenId) {
		Node node = root;

		// reversed insert
		for (int i = word.length(); i > 0; ) {
			int cp = word.codePointBefore(i);
			i -= Character.charCount(cp);

			node = node.children.computeIfAbsent(cp, k -> new Node());
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

			node = node.children.get(cp);
			if (node == null) break;

			if (node.tokenId != -1) {
				result = tokens[node.tokenId];
			}
		}

		return result;
	}
}
