package io.github.sspanak.tt9.preferences.screens.deleteWords;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import io.github.sspanak.tt9.db.entities.CustomWord;

/**
 * One page of custom words matching a search term.
 * @param offset the position of the first word on the page among all matching words
 * @param totalMatching the number of all words matching the search term
 */
record CustomWordsPage(@NonNull String searchTerm, int offset, long totalMatching, @NonNull ArrayList<CustomWord> words) {}
