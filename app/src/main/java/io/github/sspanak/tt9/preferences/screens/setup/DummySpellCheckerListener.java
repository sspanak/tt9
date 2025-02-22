package io.github.sspanak.tt9.preferences.screens.setup;

import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;

class DummySpellCheckerListener implements SpellCheckerSession.SpellCheckerSessionListener {
	public void onGetSuggestions(SuggestionsInfo[] results){}
	public void onGetSentenceSuggestions(SentenceSuggestionsInfo[] results){}
}
