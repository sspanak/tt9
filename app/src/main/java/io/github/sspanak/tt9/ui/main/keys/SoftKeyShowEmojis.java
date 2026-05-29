package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdShowEmojis;

public class SoftKeyShowEmojis extends BaseSoftKeyWithIcons {
	private final CmdShowEmojis showEmojis = new CmdShowEmojis();

	public SoftKeyShowEmojis(Context context) { super(context); }
	public SoftKeyShowEmojis(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyShowEmojis(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected boolean handleRelease() { return showEmojis.run(tt9); }
	@Override protected String getAccessibilityText() { return showEmojis.getName(tt9); }
	@Override protected int getCentralIcon() { return showEmojis.getIcon(); }

	@Override
	public void render() {
		setEnabled(showEmojis.isAvailable(tt9));
		super.render();
	}
}
