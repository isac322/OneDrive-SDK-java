package org.onedrive.container.items.pointer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
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

	abstract public @NotNull String resolveOperator(@NotNull Operator op);
}
