package net.micropact.aea.utility;

import java.util.Optional;

import com.entellitrak.ExecutionContext;
import com.entellitrak.user.User;

import net.micropact.aea.core.cache.AeaCoreConfiguration;

public final class UserUtility{

	private UserUtility() {}

	public static User getCurrentUserOrServiceAccount(final ExecutionContext etk) {
		return Optional.ofNullable(etk.getCurrentUser())
				.orElseGet(() -> AeaCoreConfiguration.getServiceAccount(etk));
	}
}
