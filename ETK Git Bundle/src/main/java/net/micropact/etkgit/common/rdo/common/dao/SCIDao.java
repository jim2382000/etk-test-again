package net.micropact.etkgit.common.rdo.common.dao;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.dynamic.SciSourceControlSettings;

public final class SCIDao {
	/**
	 * Private constructor to hide.
	 */
	private SCIDao() { }

	public static String getSettingValue(final ExecutionContext etk, final String settingCode) {
		return getSettings(etk).entrySet().stream()
				.filter(setting -> setting.getKey().equals(settingCode))
				.map(Entry::getValue)
				.findAny()
				.orElse(null);
	}

	public static Map<String, String> getSettings(final ExecutionContext etk) {
		return etk.getDynamicObjectService().getAll(SciSourceControlSettings.class).stream()
				.collect(Collectors.toMap(SciSourceControlSettings::getCode, SciSourceControlSettings::getValue));
	}
}