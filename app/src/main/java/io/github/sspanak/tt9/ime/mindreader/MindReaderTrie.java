package io.github.sspanak.tt9.ime.mindreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

class MindReaderTrie {
	private static class Node {
		@Nullable HashMap<Integer, Node> children = null;
		int tokenId = -1; // -1 = not found
	}

	@NonNull private final Node root = new Node();

	void add(@NonNull String word, int tokenId) {
		Node node = root;

		// reversed insert
		for (int i = word.length(); i > 0; ) {
			int cp = word.codePointBefore(i);
			i -= Character.charCount(cp);

			node = getNodeChildren(node).computeIfAbsent(cp, k -> new Node());
		}

		node.tokenId = tokenId;
	}

	/**
	 * Return the longest matching word ending at position `end` (exclusive)
	 */
	@Nullable
	String getLongestWord(@NonNull String text, int end, @NonNull String[] tokens) {
		Node node = root;
		String result = null;

		for (int i = end; i > 0; ) {
			int cp = text.codePointBefore(i);
			i -= Character.charCount(cp);

			node = getNodeChildren(node).get(cp);
			if (node == null) break;

			if (node.tokenId != -1) {
				result = tokens[node.tokenId];
			}
		}

		return result;
	}

	/**
	 * Ensures the given node has a non-null children map and returns them.
	 */
	@NonNull
	private HashMap<Integer, Node> getNodeChildren(@NonNull Node node) {
		if (node.children == null) {
			node.children = new HashMap<>();
		}
		return node.children;
	}
}
