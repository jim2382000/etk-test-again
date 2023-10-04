package net.micropact.aea.du.utility.systemPreference;

import java.util.List;

public class SystemPreferencesDTO {

	private final List<SystemPreferenceValue> systemPreferences;

	public SystemPreferencesDTO(final List<SystemPreferenceValue> theSystemPreferences) {
		systemPreferences = theSystemPreferences;
	}

	public List<SystemPreferenceValue> getSystemPreferences() {
		return systemPreferences;
	}
}
