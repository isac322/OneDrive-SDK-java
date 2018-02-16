package com.bhyoo.onedrive.container.items.pointer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
abstract public class BasePointer {
	abstract public @Nullable String getDriveId();

	abstract public @NotNull URI toURI() throws URISyntaxException;

	abstract public @NotNull String toApi();

	abstract public @NotNull String toASCIIApi();

	@Override public @NotNull String toString() {
		return toApi();
	}

	abstract public @NotNull String toJson();

	/**
	 * <strong>DO NOT USE</strong><br>
	 * Internal use only
	 *
	 * @param op operator object
	 *
	 * @return resolved API
	 */
	abstract public @NotNull String resolveOperator(@NotNull Operator op);
}
