package io.github.sspanak.tt9.chinese;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PinyinEditStateTest {
	@Test public void rapidSecondUpWaitsForTheWholeSpellingQueryAndItsRender() {
		PinyinEditState state = new PinyinEditState();
		state.beginSpellings();
		assertTrue(state.isTransitionPending());
		state.suggestionsShown(-1); // A previous Han frame cannot release the queued second Up.
		assertTrue(state.isTransitionPending());
		state.predictionsReady();
		assertTrue(state.isTransitionPending());
		state.suggestionsShown(state.getReadyRevision());
		assertFalse(state.isTransitionPending());
	}

	@Test public void aRenderedOlderQueryCannotAcknowledgeTheNewSpellingRequest() {
		PinyinEditState state = new PinyinEditState();
		state.beginSpellings();
		state.predictionsReady();
		long oldFrame = state.getReadyRevision();
		state.beginSpellings();
		state.predictionsReady();
		state.suggestionsShown(oldFrame);
		assertTrue(state.isTransitionPending());
		state.suggestionsShown(state.getReadyRevision());
		assertFalse(state.isTransitionPending());
	}

	@Test public void immediateReturnWaitsEvenWhenTheEditingPreviewWasNeverRendered() {
		PinyinEditState state = new PinyinEditState();
		state.publish("XIAN");
		state.beginReturn();
		assertFalse(state.isPreview("XI'AN")); // The old phonetic frame has different text.
		assertTrue(state.isTransitionPending()); // Transition state still protects the key queue.
		state.predictionsReady();
		state.suggestionsShown(state.getReadyRevision());
		assertFalse(state.isTransitionPending());
	}

	@Test public void ordinaryXianIsNotAnOldPreviewAfterHanHasActuallyBeenShown() {
		PinyinEditState state = new PinyinEditState();
		state.publish("XIAN");
		state.beginReturn();
		state.suggestionsShown(state.getReadyRevision()); // An already queued preview render is not the Han result.
		assertTrue(state.isPreview("XIAN"));
		state.predictionsReady();
		assertTrue(state.isPreview("XIAN")); // Fast keys stay protected until the UI update.
		state.suggestionsShown(state.getReadyRevision());
		assertFalse(state.isPreview("XIAN")); // This spelling can now be selected normally.
	}

	@Test public void aValidEmptyResultAlsoEndsThePreviewTransition() {
		PinyinEditState state = new PinyinEditState();
		state.publish("XIAN");
		state.beginReturn();
		state.predictionsReady();
		state.suggestionsShown(state.getReadyRevision());
		assertFalse(state.isPreview("XIAN"));
		assertFalse(state.isFinishPending());
	}

	@Test public void immediateDownWaitsForInsertedDigitResolutionAndFinishesOnce() {
		PinyinEditState state = new PinyinEditState();
		state.publish("X4AN");
		state.beginQuery();
		assertFalse(state.requestFinish(true, false));
		assertTrue(state.isFinishPending());
		assertTrue(state.completeQuery(true));
		assertFalse(state.isFinishPending());
		assertFalse(state.isQueryPending());
		assertFalse(state.completeQuery(true));
	}

	@Test public void noMatchReleasesDeferredKeysAndKeepsThePreviewEditable() {
		PinyinEditState state = new PinyinEditState();
		state.publish("XIE54");
		state.beginQuery();
		assertFalse(state.requestFinish(true, false));
		assertFalse(state.completeQuery(false));
		state.publish("XIE54");
		assertFalse(state.isFinishPending());
		assertTrue(state.isPreview("XIE54"));
		assertFalse(state.requestFinish(true, false));
		assertFalse(state.isFinishPending());
	}

	@Test public void timeoutCanReleaseAWaitingFinishWithoutInventingAReading() {
		PinyinEditState state = new PinyinEditState();
		state.publish("XI4N");
		state.beginQuery();
		state.requestFinish(true, false);
		assertFalse(state.completeQuery(false));
		assertFalse(state.isFinishPending());
		// A late result can resolve the text, but cannot replay the expired finish request.
		assertFalse(state.completeQuery(true));
	}

	@Test public void resolvingWithoutAFinishRequestDoesNotLeaveEditing() {
		PinyinEditState state = new PinyinEditState();
		state.publish("X4AN");
		state.beginQuery();
		assertFalse(state.completeQuery(true));
		state.publish("XIAN");
		assertTrue(state.isPreview("XIAN"));
	}

	@Test public void cancellingWhileWaitingClearsTheIntentAndAllowsOriginalReadingReturn() {
		PinyinEditState state = new PinyinEditState();
		state.publish("XIE54");
		state.beginQuery();
		state.requestFinish(true, false);
		state.beginReturn();
		assertFalse(state.isFinishPending());
		assertFalse(state.completeQuery(true));
		state.predictionsReady();
		state.suggestionsShown(state.getReadyRevision());
		assertFalse(state.isPreview("XIE54"));
	}

	@Test public void deletingTheEntireReadingCanFinishWithoutALookup() {
		PinyinEditState state = new PinyinEditState();
		state.publish("");
		assertTrue(state.requestFinish(false, false));
		assertFalse(state.isFinishPending());
	}

	@Test public void repeatedHanSpellingEditCyclesDoNotReusePreviewIdentity() {
		PinyinEditState state = new PinyinEditState();
		for (int i = 0; i < 50; i++) {
			assertFalse(state.isPreview("XIAN")); // Normal spelling list before the next edit.
			state.publish("XIAN");
			assertTrue(state.requestFinish(true, true));
			state.beginReturn();
			state.predictionsReady();
			state.suggestionsShown(state.getReadyRevision());
			assertFalse(state.isPreview("XIAN"));
		}
	}

	@Test public void aNewEditCannotAcknowledgeAnOlderReplacementRender() {
		PinyinEditState state = new PinyinEditState();
		state.publish("XIAN");
		state.beginReturn();
		state.predictionsReady();
		state.publish("XIAO");
		state.suggestionsShown(state.getReadyRevision());
		assertTrue(state.isPreview("XIAO"));
	}

	@Test public void resetReleasesAllPreviewAndQueryState() {
		PinyinEditState state = new PinyinEditState();
		state.publish("X4AN");
		state.beginQuery();
		state.requestFinish(true, false);
		state.reset();
		assertFalse(state.isPreview("X4AN"));
		assertFalse(state.isFinishPending());
		assertFalse(state.isQueryPending());
	}
}
