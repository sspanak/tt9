package io.github.sspanak.tt9.ui.main.keys;

import android.content.Context;
import android.util.AttributeSet;

import io.github.sspanak.tt9.commands.CmdNextLanguage;

public class SoftKeyNextLanguage extends BaseSoftKeyWithIcons {
	private final CmdNextLanguage nextLanguage = new CmdNextLanguage();

	public SoftKeyNextLanguage(Context context) { super(context); }
	public SoftKeyNextLanguage(Context context, AttributeSet attrs) { super(context, attrs); }
	public SoftKeyNextLanguage(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

	@Override protected boolean handleRelease() { return nextLanguage.run(tt9); }
	@Override protected String getAccessibilityText() { return nextLanguage.getName(tt9); }
	@Override protected int getCentralIcon() { return nextLanguage.getIcon(); }

	@Override
	public void render() {
		setEnabled(nextLanguage.isAvailable(tt9));
		super.render();
	}
}
