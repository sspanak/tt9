package io.github.sspanak.tt9.ui;

import androidx.annotation.Nullable;

import java.util.HashMap;

import io.github.sspanak.tt9.R;
import io.github.sspanak.tt9.ime.modes.InputMode;
import io.github.sspanak.tt9.ime.modes.InputModeKind;
import io.github.sspanak.tt9.languages.Language;
import io.github.sspanak.tt9.preferences.settings.SettingsStore;

public class StatusIcon {
	private static final HashMap<String, Integer> ICONS = new HashMap<>();
	public final int resourceId;


	public StatusIcon(@Nullable SettingsStore settings, @Nullable InputMode mode, @Nullable Language language, int textCase) {
		if (ICONS.isEmpty()) {
			generateIconsCache();
		}
		resourceId = resolveResourcePerMode(settings, mode, language, textCase);
	}


	/**
	 * Generates a cache of icons for different input modes and languages.
	 * WARNING: This method is auto-generated and should not be modified manually.
	 * To update the icons, run the `updateStatusIcons` gradle task.
	 */
	private void generateIconsCache() { ICONS.put("ic_lang_alefbet", R.drawable.ic_lang_alefbet);ICONS.put("ic_lang_alfabeta_lo", R.drawable.ic_lang_alfabeta_lo);ICONS.put("ic_lang_alfabeta_up", R.drawable.ic_lang_alfabeta_up);ICONS.put("ic_lang_alifba", R.drawable.ic_lang_alifba);ICONS.put("ic_lang_ar", R.drawable.ic_lang_ar);ICONS.put("ic_lang_bg_cp", R.drawable.ic_lang_bg_cp);ICONS.put("ic_lang_bg_lo", R.drawable.ic_lang_bg_lo);ICONS.put("ic_lang_bg_up", R.drawable.ic_lang_bg_up);ICONS.put("ic_lang_br_cp", R.drawable.ic_lang_br_cp);ICONS.put("ic_lang_br_lo", R.drawable.ic_lang_br_lo);ICONS.put("ic_lang_br_up", R.drawable.ic_lang_br_up);ICONS.put("ic_lang_ca_cp", R.drawable.ic_lang_ca_cp);ICONS.put("ic_lang_ca_lo", R.drawable.ic_lang_ca_lo);ICONS.put("ic_lang_ca_up", R.drawable.ic_lang_ca_up);ICONS.put("ic_lang_cyrillic_lo", R.drawable.ic_lang_cyrillic_lo);ICONS.put("ic_lang_cyrillic_up", R.drawable.ic_lang_cyrillic_up);ICONS.put("ic_lang_cz_cp", R.drawable.ic_lang_cz_cp);ICONS.put("ic_lang_cz_lo", R.drawable.ic_lang_cz_lo);ICONS.put("ic_lang_cz_up", R.drawable.ic_lang_cz_up);ICONS.put("ic_lang_da_cp", R.drawable.ic_lang_da_cp);ICONS.put("ic_lang_da_lo", R.drawable.ic_lang_da_lo);ICONS.put("ic_lang_da_up", R.drawable.ic_lang_da_up);ICONS.put("ic_lang_de_cp", R.drawable.ic_lang_de_cp);ICONS.put("ic_lang_de_lo", R.drawable.ic_lang_de_lo);ICONS.put("ic_lang_de_up", R.drawable.ic_lang_de_up);ICONS.put("ic_lang_el_cp", R.drawable.ic_lang_el_cp);ICONS.put("ic_lang_el_lo", R.drawable.ic_lang_el_lo);ICONS.put("ic_lang_el_up", R.drawable.ic_lang_el_up);ICONS.put("ic_lang_en_cp", R.drawable.ic_lang_en_cp);ICONS.put("ic_lang_en_lo", R.drawable.ic_lang_en_lo);ICONS.put("ic_lang_en_up", R.drawable.ic_lang_en_up);ICONS.put("ic_lang_es_cp", R.drawable.ic_lang_es_cp);ICONS.put("ic_lang_es_lo", R.drawable.ic_lang_es_lo);ICONS.put("ic_lang_es_up", R.drawable.ic_lang_es_up);ICONS.put("ic_lang_et_cp", R.drawable.ic_lang_et_cp);ICONS.put("ic_lang_et_lo", R.drawable.ic_lang_et_lo);ICONS.put("ic_lang_et_up", R.drawable.ic_lang_et_up);ICONS.put("ic_lang_fa", R.drawable.ic_lang_fa);ICONS.put("ic_lang_fr_cp", R.drawable.ic_lang_fr_cp);ICONS.put("ic_lang_fr_lo", R.drawable.ic_lang_fr_lo);ICONS.put("ic_lang_fr_up", R.drawable.ic_lang_fr_up);ICONS.put("ic_lang_ga_cp", R.drawable.ic_lang_ga_cp);ICONS.put("ic_lang_ga_lo", R.drawable.ic_lang_ga_lo);ICONS.put("ic_lang_ga_up", R.drawable.ic_lang_ga_up);ICONS.put("ic_lang_gu", R.drawable.ic_lang_gu);ICONS.put("ic_lang_gu_abc", R.drawable.ic_lang_gu_abc);ICONS.put("ic_lang_he", R.drawable.ic_lang_he);ICONS.put("ic_lang_hi", R.drawable.ic_lang_hi);ICONS.put("ic_lang_hi_abc", R.drawable.ic_lang_hi_abc);ICONS.put("ic_lang_hn_cp", R.drawable.ic_lang_hn_cp);ICONS.put("ic_lang_hn_lo", R.drawable.ic_lang_hn_lo);ICONS.put("ic_lang_hn_up", R.drawable.ic_lang_hn_up);ICONS.put("ic_lang_hr_cp", R.drawable.ic_lang_hr_cp);ICONS.put("ic_lang_hr_lo", R.drawable.ic_lang_hr_lo);ICONS.put("ic_lang_hr_up", R.drawable.ic_lang_hr_up);ICONS.put("ic_lang_id_cp", R.drawable.ic_lang_id_cp);ICONS.put("ic_lang_id_lo", R.drawable.ic_lang_id_lo);ICONS.put("ic_lang_id_up", R.drawable.ic_lang_id_up);ICONS.put("ic_lang_it_cp", R.drawable.ic_lang_it_cp);ICONS.put("ic_lang_it_lo", R.drawable.ic_lang_it_lo);ICONS.put("ic_lang_it_up", R.drawable.ic_lang_it_up);ICONS.put("ic_lang_ji", R.drawable.ic_lang_ji);ICONS.put("ic_lang_kanji", R.drawable.ic_lang_kanji);ICONS.put("ic_lang_ko", R.drawable.ic_lang_ko);ICONS.put("ic_lang_latin_lo", R.drawable.ic_lang_latin_lo);ICONS.put("ic_lang_latin_up", R.drawable.ic_lang_latin_up);ICONS.put("ic_lang_lt_cp", R.drawable.ic_lang_lt_cp);ICONS.put("ic_lang_lt_lo", R.drawable.ic_lang_lt_lo);ICONS.put("ic_lang_lt_up", R.drawable.ic_lang_lt_up);ICONS.put("ic_lang_lv_cp", R.drawable.ic_lang_lv_cp);ICONS.put("ic_lang_lv_lo", R.drawable.ic_lang_lv_lo);ICONS.put("ic_lang_lv_up", R.drawable.ic_lang_lv_up);ICONS.put("ic_lang_mg_cp", R.drawable.ic_lang_mg_cp);ICONS.put("ic_lang_mg_lo", R.drawable.ic_lang_mg_lo);ICONS.put("ic_lang_mg_up", R.drawable.ic_lang_mg_up);ICONS.put("ic_lang_nl_cp", R.drawable.ic_lang_nl_cp);ICONS.put("ic_lang_nl_lo", R.drawable.ic_lang_nl_lo);ICONS.put("ic_lang_nl_up", R.drawable.ic_lang_nl_up);ICONS.put("ic_lang_no_cp", R.drawable.ic_lang_no_cp);ICONS.put("ic_lang_no_lo", R.drawable.ic_lang_no_lo);ICONS.put("ic_lang_no_up", R.drawable.ic_lang_no_up);ICONS.put("ic_lang_pl_cp", R.drawable.ic_lang_pl_cp);ICONS.put("ic_lang_pl_lo", R.drawable.ic_lang_pl_lo);ICONS.put("ic_lang_pl_up", R.drawable.ic_lang_pl_up);ICONS.put("ic_lang_pt_cp", R.drawable.ic_lang_pt_cp);ICONS.put("ic_lang_pt_lo", R.drawable.ic_lang_pt_lo);ICONS.put("ic_lang_pt_up", R.drawable.ic_lang_pt_up);ICONS.put("ic_lang_ro_cp", R.drawable.ic_lang_ro_cp);ICONS.put("ic_lang_ro_lo", R.drawable.ic_lang_ro_lo);ICONS.put("ic_lang_ro_up", R.drawable.ic_lang_ro_up);ICONS.put("ic_lang_ru_cp", R.drawable.ic_lang_ru_cp);ICONS.put("ic_lang_ru_lo", R.drawable.ic_lang_ru_lo);ICONS.put("ic_lang_ru_up", R.drawable.ic_lang_ru_up);ICONS.put("ic_lang_sk_cp", R.drawable.ic_lang_sk_cp);ICONS.put("ic_lang_sk_lo", R.drawable.ic_lang_sk_lo);ICONS.put("ic_lang_sk_up", R.drawable.ic_lang_sk_up);ICONS.put("ic_lang_sl_cp", R.drawable.ic_lang_sl_cp);ICONS.put("ic_lang_sl_lo", R.drawable.ic_lang_sl_lo);ICONS.put("ic_lang_sl_up", R.drawable.ic_lang_sl_up);ICONS.put("ic_lang_sr_cp", R.drawable.ic_lang_sr_cp);ICONS.put("ic_lang_sr_lo", R.drawable.ic_lang_sr_lo);ICONS.put("ic_lang_sr_up", R.drawable.ic_lang_sr_up);ICONS.put("ic_lang_su_cp", R.drawable.ic_lang_su_cp);ICONS.put("ic_lang_su_lo", R.drawable.ic_lang_su_lo);ICONS.put("ic_lang_su_up", R.drawable.ic_lang_su_up);ICONS.put("ic_lang_sv_cp", R.drawable.ic_lang_sv_cp);ICONS.put("ic_lang_sv_lo", R.drawable.ic_lang_sv_lo);ICONS.put("ic_lang_sv_up", R.drawable.ic_lang_sv_up);ICONS.put("ic_lang_sw_cp", R.drawable.ic_lang_sw_cp);ICONS.put("ic_lang_sw_lo", R.drawable.ic_lang_sw_lo);ICONS.put("ic_lang_sw_up", R.drawable.ic_lang_sw_up);ICONS.put("ic_lang_th", R.drawable.ic_lang_th);ICONS.put("ic_lang_th_abc", R.drawable.ic_lang_th_abc);ICONS.put("ic_lang_tifinagh", R.drawable.ic_lang_tifinagh);ICONS.put("ic_lang_tm_cp", R.drawable.ic_lang_tm_cp);ICONS.put("ic_lang_tm_lo", R.drawable.ic_lang_tm_lo);ICONS.put("ic_lang_tm_tifinagh", R.drawable.ic_lang_tm_tifinagh);ICONS.put("ic_lang_tm_up", R.drawable.ic_lang_tm_up);ICONS.put("ic_lang_tr_cp", R.drawable.ic_lang_tr_cp);ICONS.put("ic_lang_tr_lo", R.drawable.ic_lang_tr_lo);ICONS.put("ic_lang_tr_up", R.drawable.ic_lang_tr_up);ICONS.put("ic_lang_uk_cp", R.drawable.ic_lang_uk_cp);ICONS.put("ic_lang_uk_lo", R.drawable.ic_lang_uk_lo);ICONS.put("ic_lang_uk_up", R.drawable.ic_lang_uk_up);ICONS.put("ic_lang_vi_cp", R.drawable.ic_lang_vi_cp);ICONS.put("ic_lang_vi_lo", R.drawable.ic_lang_vi_lo);ICONS.put("ic_lang_vi_up", R.drawable.ic_lang_vi_up);ICONS.put("ic_lang_zh_pinyin", R.drawable.ic_lang_zh_pinyin); }


	private int resolveResourcePerMode(@Nullable SettingsStore settings, @Nullable InputMode mode, @Nullable Language language, int textCase) {
		if (language == null || mode == null || settings == null || InputModeKind.isPassthrough(mode) || !settings.isStatusIconEnabled()) {
			return 0;
		}

		if (InputModeKind.isHiragana(mode)) {
			return R.drawable.ic_lang_hiragana;
		} else if (InputModeKind.isKatakana(mode)) {
			return R.drawable.ic_lang_katakana;
		} else if (InputModeKind.is123(mode)) {
			return R.drawable.ic_lang_123;
		} else if (InputModeKind.isABC(mode)) {
			return getResourceId(getResourceName(mode, language, textCase), R.drawable.ic_keyboard);
		} else if (InputModeKind.isPredictive(mode)) {
			return getResourceId(getResourceName(mode, language, textCase), R.drawable.ic_keyboard);
		}

		return R.drawable.ic_keyboard;
	}


	@Nullable
	private String getResourceName(@Nullable InputMode mode, @Nullable Language language, int textCase) {
		if (mode == null || language == null) {
			return null;
		}

		final StringBuilder key = new StringBuilder();
		key.append(InputModeKind.isABC(mode) ? language.getIconABC() : language.getIconT9());

		switch (textCase) {
			case InputMode.CASE_UPPER:
				key.append("_up");
				break;
			case InputMode.CASE_LOWER:
				key.append("_lo");
				break;
			case InputMode.CASE_CAPITALIZE:
				key.append("_cp");
				break;
		}

		return key.toString();
	}


	private int getResourceId(@Nullable String key, int defaultValue) {
		Integer resId = null;
		if (key != null && ICONS.containsKey(key)) {
			resId = ICONS.get(key);
		}

		return resId != null ? resId : defaultValue;
	}
}
