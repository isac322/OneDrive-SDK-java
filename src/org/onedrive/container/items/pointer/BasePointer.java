package org.onedrive.container.items.pointer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
abstract public class BasePointer {
	@Nullable abstract public String getDriveId();

	@NotNull abstract public URI toURI() throws URISyntaxException;

	@NotNull abstract public String toApi();

	@NotNull abstract public String toASCIIApi();

	@NotNull @Override public String toString() {
		return toApi();
	}

	@NotNull abstract public String toJson();

	@NotNull abstract public String resolveOperator(@NotNull Operator op);
}
