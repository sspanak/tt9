package org.nyanya.android.traditionalt9;

import android.content.Context;
import android.view.View;

public class SmileyDialog extends AbsSymDialog {

	private static final String[] symbols = {
		// lol wiki http://en.wikipedia.org/wiki/List_of_emoticons
		":-)", ":o)", ":]", ":3", ":c)", ":>", "=]", "=)", ":}", ":-D",
		"8-D", "X-D", "=-D", "B^D", "<:-)", ">:-[", ":-(", ":-<", ":o(", ":{",
		":'-(", ":'-)", ":@", "D:<", "D8", "v.v","D-':", ">:O", ":-O", "o_0",
		":*", ";-)", ";-D", ">:-P", ":-P", "X-P", "=p", ">:-/", ":-/", ":-.",
		":S", ">.<", ":-|", ":$", ":-X", ":-#", ":-%", ":С", ":-E", ":-*",
		"0:-3", "0:-)", ">;-)", ">:-)", ">_>", "*<|:-)", "\\o/", "<3", "</3", "=-3", };

	private static final int MAX_PAGE = (int) Math.ceil(symbols.length / 10.0);

	public SmileyDialog(Context c, View mv) {
		super(c, mv);

	}

	@Override
	String[] getContentDescription() {
		return context.getResources().getStringArray(R.array.smileyContentDescription);
	}

	@Override
	protected String getSymbol(int index) {
		return symbols[index];
	}

	@Override
	protected String getTitleText() {
		return context.getString(R.string.smiley_insert);
	}

	@Override
	protected int getSymbolSize() {
		return symbols.length;
	}

	@Override
	protected int getMaxPage() {
		return MAX_PAGE;
	}

}
