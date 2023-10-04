package net.micropact.aea.du.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.SystemPreferenceService;

import net.entellitrak.aea.du.service.ISystemPreferenceMigrationService;
import net.entellitrak.aea.gl.api.java.io.noisyinputstream.INoisyInputStream;
import net.entellitrak.aea.gl.api.java.io.noisyinputstream.INoisyInputStream.IWrapRequest;
import net.micropact.aea.core.gson.GsonUtility;
import net.micropact.aea.du.utility.systemPreference.SystemPreferenceValue;
import net.micropact.aea.du.utility.systemPreference.SystemPreferencesDTO;

/**
 * This class is the private implementation of the
 * {@link ISystemPreferenceMigrationService} interface in the public API.
 *
 * @author zachary.miller
 */
public class SystemPreferenceMigrationService implements ISystemPreferenceMigrationService {

	private final ExecutionContext etk;

	/**
	 * Simple constructor.
	 *
	 * @param executionContext entellitrak execution context
	 */
	public SystemPreferenceMigrationService(final ExecutionContext executionContext) {
		etk = executionContext;
	}

	@Override
	public InputStream exportToStream(final Set<String> preferencesToExport) throws Exception {
		final SystemPreferenceService systemPreferenceService = etk.getSystemPreferenceService();

		final List<SystemPreferenceValue> preferenceValues = systemPreferenceService.getAllPreferences()
				.stream()
				.filter(systemPreference -> preferencesToExport.contains(systemPreference.getName()))
				.map(systemPreference -> new SystemPreferenceValue(systemPreference.getName(), systemPreference.getValue()))
				.sorted(Comparator.comparing(SystemPreferenceValue::getName))
				.collect(Collectors.toList());

		final SystemPreferencesDTO systemPreferencesDto = new SystemPreferencesDTO(preferenceValues);

		final String json = GsonUtility.getStandardPrettyPrintingGson().toJson(systemPreferencesDto);

		return INoisyInputStream.wrap(IWrapRequest.builder(etk)
				.setInputStream(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)))
				.build());
	}

	@Override
	public void importFromStream(final InputStream jsonPreferenceStream) throws Exception {
		try(Reader reader = new InputStreamReader(jsonPreferenceStream)){
			final SystemPreferencesDTO systemPreferenceDto = GsonUtility.getStandardPrettyPrintingGson().fromJson(reader, SystemPreferencesDTO.class);

			updatePreferenceValues(etk, systemPreferenceDto);
		}
	}

	public static void updatePreferenceValues(final ExecutionContext etk,
			final SystemPreferencesDTO systemPreferenceDto) {
		final SystemPreferenceService systemPreferenceService = etk.getSystemPreferenceService();

		systemPreferenceDto.getSystemPreferences()
		.forEach(systemPreferenceValue -> {
			final String preferenceName = systemPreferenceValue.getName();
			final String preferenceValue = systemPreferenceValue.getValue();

			// TODO: Core should update the API so that resetPreference is not used.
			if(preferenceValue == null) {
				systemPreferenceService.resetPreference(preferenceName);
			} else {
				systemPreferenceService.updatePreference(
						preferenceName,
						preferenceValue);
			}
		});
	}
}
