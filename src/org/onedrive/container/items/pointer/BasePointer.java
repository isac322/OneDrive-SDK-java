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
	public static final String ACTION_COPY = "action.copy";
	public static final String ACTION_CREATE_LINK = "action.createLink";
	public static final String CHILDREN = "children";
	public static final String CONTENT = "content";
	public static final String VIEW_SEARCH = "view.search";
	public static final String VIEW_DELTA = "view.delta";
	public static final String THUMBNAILS = "thumbnails";

	@Nullable abstract public String getDriveId();

	@NotNull abstract public URI toURI() throws URISyntaxException;

	@NotNull abstract public String toApi();

	@NotNull abstract public String toASCIIApi();

	@NotNull @Override public String toString() {
		return toApi();
	}

	@NotNull abstract public String toJson();

	@NotNull abstract public String resolveOperator(@NotNull String operator);
}
