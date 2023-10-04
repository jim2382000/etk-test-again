package net.entellitrak.aea.gl.api.java.ref;

import java.lang.ref.Cleaner;

public final class CleanerUtil {

	private CleanerUtil() {}

	private static final Cleaner CLEANER = Cleaner.create();

	public static Cleaner getSharedCleaner() {
		return CLEANER;
	}
}
