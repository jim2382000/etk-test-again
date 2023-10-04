package net.entellitrak.aea.gl.api.java.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public final class DateUtil {

	private DateUtil() {
	}

	public static Date toDate(final LocalDate localDate) {
		return Optional.ofNullable(localDate)
				.map(local -> Date.from(local.atStartOfDay(ZoneId.systemDefault()).toInstant()))
				.orElse(null);
	}

	public static Date toDate(final LocalDateTime localDateTime) {
		return Optional.ofNullable(localDateTime)
				.map(local -> Date.from(local.atZone(ZoneId.systemDefault()).toInstant()))
				.orElse(null);
	}
}
