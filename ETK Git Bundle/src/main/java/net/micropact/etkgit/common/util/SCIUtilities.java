package net.micropact.etkgit.common.util;

import com.entellitrak.ExecutionContext;

import net.micropact.etkgit.common.rdo.common.dao.SCIDao;

public final class SCIUtilities {
	/**
	 * Private constructor to hide.
	 */
	private SCIUtilities() { }

	public static String getRepositoryUrl(final ExecutionContext etk) {
		return SCIDao.getSettingValue(etk, "repositoryUrl");
	}
}
