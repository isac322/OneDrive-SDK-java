package org.onedrive.container;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
abstract public class BaseContainer {
	public static ZonedDateTime parseDateTime(String timestamp) {
		if (timestamp == null) return null;
		return ZonedDateTime.ofInstant(Instant.parse(timestamp), ZoneId.systemDefault());
	}

	/**
	 * Concat all member field as one {@code String}.
	 * Only for debug.
	 *
	 * @return all member field.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		for (Field field : this.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object object = field.get(this);
				if (object != null && !(object instanceof Map)) {
					buffer.append(field.getName()).append(" : ").append(object).append(", ");
				}
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
	}
}
