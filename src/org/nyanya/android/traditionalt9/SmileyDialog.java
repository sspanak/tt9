package org.nyanya.android.traditionalt9;

import android.content.Context;
import android.view.View;

public class SmileyDialog extends AbsSymDialog {
	
    private static final String[] symbols = {
    	//lol wiki http://en.wikipedia.org/wiki/List_of_emoticons
    	":-)", ":)", ":o)", ":]", ":3", ":c)", ":>", "=]", "=)", ":}",
    	":-D", ":D", "8-D", "8D", "X-D", "XD", "=-D", "=D", "B^D",
    	">:[", ":-(", ":(", ":c", ":-<", ":<", ":-[", ":[", ":{",
    	":'-(", ":'(", ":'-)", ":')", ":@", "D:<", "D:", "D8", "D;", "D=", "DX", 
    	"v.v", "D-':", ">:O", ":-O", ":O", ":O", "o_O", "o_0", "o.O", "8-0",
    	":*", ";-)", ";)", ";-]", ";]", ";D", 
    	">:P", ":-P", ":P", "XP", "xp", ":-p", ":p", "=p", ":-b", ":b",
    	">:\\", ">:/", ":-/", ":-.", ":/", ":\\", "=/", "=\\", ":L", "=L", ":S", ">.<",
    	":|", ":-|", ":$", ":-X", ":X", ":-#", ":#", "O:-)", "0:-3", "0:3", "0:-)", "0:)", 
    	">:)", ">;)", ">:-)", ">_>", "<_<", "\\o/", "<3", "</3",
    	"=-3", "=3",
    };
    
    private static final int MAX_PAGE = (int)Math.ceil(symbols.length / 10.0);
    
	public SmileyDialog(Context c, View mv) {
    	super(c, mv);
	}

	@Override
	protected String getSymbol(int index) {
		return symbols[index];
	}

	@Override
	protected String getTitleText() {
		return "Smiley";
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
