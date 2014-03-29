package org.nyanya.android.traditionalt9;

import android.content.Context;
import android.view.View;

public class SymbolDialog extends AbsSymDialog {

	private static final char[] symbols = {
		'.', ',', '!', '?', '$', '&', '%', '#', '@', '"', '\'',	':', ';', '(', ')', '/', '\\',
		'-', '+', '=', '*', '<', '>', '[', ']', '{', '}', '^', '|', '_', '~', '`' }; // 32
	private static final int MAX_PAGE = (int) Math.ceil(symbols.length / 10.0);

	public SymbolDialog(Context c, View mv) {
		super(c, mv);
	}

	@Override
	String[] getContentDescription() {
		return null;
	}

	@Override
	protected String getSymbol(int index) {
		return String.valueOf(symbols[index]);
	}

	@Override
	protected String getTitleText() {
		return context.getString(R.string.symbol_insert);
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
