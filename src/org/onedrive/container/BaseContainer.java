package org.onedrive.container;

import com.sun.istack.internal.NotNull;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 * @author isac322
 */
abstract public class BaseContainer {
	/**
	 * Concat all member field as one {@code String}.
	 * Only for debug.
	 * @return all member field.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		for (Field field : this.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object object = field.get(this);
				if (object != null) {
					buffer.append(field.getName()).append(" : ").append(object).append(", ");
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return buffer.toString();
	}

	public static ZonedDateTime parseDateTime(@NotNull String timestamp) {
		return ZonedDateTime.ofInstant(Instant.parse(timestamp), ZoneId.systemDefault());
	}
}
