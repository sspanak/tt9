package io.github.sspanak.tt9.chinese;

import io.github.sspanak.tt9.ime.modes.helpers.PinyinSequenceEditor;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class PinyinEditReadingTest {
	@Test public void secondUpOnAnIncompleteReadingKeepsOnlyTypedLetters() {
		for (String selected : List.of("PI'KE", "PI'LE", "PI'KA", "QI'KA", "QI'KE", "QI'LE", "RI'KE", "RI'LE")) {
			PinyinSequenceEditor editor = PinyinEditReading.create("745", selected, 0);
			assertEquals("745", editor.getDigits());
			assertEquals(selected.replace("'", "").substring(0, 3), editor.getReading());
			assertEquals(3, editor.getCursor());
			assertTrue(editor.hasResolvedReading());
		}
	}

	@Test public void incompleteReadingCanFinishWithoutAppendingItsCompletion() {
		PinyinSequenceEditor editor = PinyinEditReading.create("745", "RI'LE", 0);
		PinyinEditState state = new PinyinEditState();
		state.publish(editor.getReading());
		assertTrue(state.requestFinish(true, editor.hasResolvedReading()));
		assertEquals("RIL", editor.getReading());
		assertEquals("745", editor.getDigits());
		assertTrue(PinyinFuzzy.matchesPrefix(editor.getDigits(), editor.getReading(), "RILE"));
	}

	@Test public void appendingTheMissingLetterResolvesAnExplicitMultiSyllableReading() {
		PinyinSequenceEditor editor = PinyinEditReading.create("745", "RI'LE", 0);
		editor.insertDigit(3);
		PinyinEditState state = new PinyinEditState();
		state.beginQuery();
		assertFalse(state.requestFinish(true, editor.hasResolvedReading()));
		editor.resolveReading(List.of("PI'KE", "RI'LE"));
		assertEquals("RILE", editor.getReading());
		assertEquals("RI'LE", editor.getResolvedReading());
		assertEquals("7453", editor.getDigits());
		assertTrue(state.completeQuery(editor.hasResolvedReading()));
	}

	@Test public void deletingThePartialSyllableCanResolveThePreviousCompleteSyllable() {
		PinyinSequenceEditor editor = PinyinEditReading.create("745", "RI'LE", 0);
		editor.deleteBeforeCursor();
		assertFalse(editor.hasResolvedReading());
		editor.resolveReading(List.of("PI", "QI", "RI"));
		assertEquals("RI", editor.getReading());
		assertEquals("74", editor.getDigits());
		assertTrue(editor.hasResolvedReading());
	}

	@Test public void unmatchedEditingDoesNotAcceptTheLongerCompletionOrLeaveAnUncancelableFinish() {
		PinyinSequenceEditor editor = PinyinEditReading.create("745", "RI'LE", 0);
		editor.insertDigit(9);
		PinyinEditState state = new PinyinEditState();
		state.beginQuery();
		assertFalse(state.requestFinish(true, editor.hasResolvedReading()));
		editor.resolveReading(List.of("RI'LE", "RI'LE'ZI"));
		assertFalse(editor.hasResolvedReading());
		assertFalse(state.completeQuery(false));
		assertEquals("RIL9", editor.getReading());
		state.beginReturn(); // Back restores the separately retained original sequence/selection.
		assertFalse(state.isFinishPending());
		PinyinSequenceEditor restored = PinyinEditReading.create("745", "RI'LE", 0);
		assertEquals("745", restored.getDigits());
		assertEquals("RIL", restored.getReading());
	}

	@Test public void repeatedIncompleteReentryPreservesTheSequence() {
		for (int i = 0; i < 4; i++) {
			PinyinSequenceEditor editor = PinyinEditReading.create("745", "RI'LE", 0);
			editor.moveCursor(-1);
			editor.moveCursor(1);
			assertEquals("RIL", editor.getReading());
			assertEquals("745", editor.getDigits());
		}
	}

	@Test public void exactAndEnabledFullFuzzyReadingsKeepExistingBehavior() {
		assertEquals("XIAN", PinyinEditReading.create("9426", "XIAN", 0).getReading());
		assertEquals("XIAN", PinyinEditReading.create("9426", "XI'AN", 0).getReading());
		PinyinSequenceEditor fuzzy = PinyinEditReading.create("64", "LI", PinyinFuzzy.NL);
		assertEquals("LI", fuzzy.getReading());
		assertEquals("54", fuzzy.getDigits());
	}

	@Test public void staleAndUnrelatedSelectionsRemainRejected() {
		for (String selected : List.of("Z", "XI", "NI'HAO", "ZHONG")) {
			assertThrows(IllegalArgumentException.class, () -> PinyinEditReading.create("9426", selected, 0));
		}
		assertThrows(IllegalArgumentException.class, () -> PinyinEditReading.create("64", "LI", 0));
	}

	@Test public void anIncompleteFuzzyCompletionCannotInventAnUntypedDifferentPrefix() {
		assertThrows(IllegalArgumentException.class, () -> PinyinEditReading.create("644", "LI'HAO", PinyinFuzzy.NL));
	}

	@Test public void addingACompletionKeepsItsExplicitBoundaryForConversion() {
		PinyinSequenceEditor editor = PinyinEditReading.create("942", "XI'AN", 0);
		assertEquals("XIA", editor.getReading());
		editor.insertDigit(6);
		editor.resolveReading(List.of("XI'AN"));
		assertEquals("XIAN", editor.getReading());
		assertEquals("XI'AN", editor.getResolvedReading());
		assertTrue(PinyinSyllables.matchesSelectedReading("XIAN西安", editor.getResolvedReading()));
		assertFalse(PinyinSyllables.matchesSelectedReading("XIAN现", editor.getResolvedReading()));
	}

	@Test public void unresolvedEditsCannotExposeAnOlderResolvedReadingForConversion() {
		PinyinSequenceEditor editor = PinyinEditReading.create("745", "RI'LE", 0);
		editor.insertDigit(9);
		assertEquals("", editor.getResolvedReading());
		editor.resolveReading(List.of("RI'LE"));
		assertEquals("", editor.getResolvedReading());
	}
}
