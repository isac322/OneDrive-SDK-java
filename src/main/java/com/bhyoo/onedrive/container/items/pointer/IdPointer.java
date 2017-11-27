package com.bhyoo.onedrive.container.items.pointer;

import com.bhyoo.onedrive.client.RequestTool;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static com.bhyoo.onedrive.client.Client.ITEM_ID_PREFIX;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class IdPointer extends BasePointer {
	private static final @NotNull Pattern idPattern = Pattern.compile("[a-zA-Z0-9!]+");

	@Getter protected final @Nullable String driveId;
	@Getter protected final @NotNull String id;
	private final @NotNull String path;


	public IdPointer(@NotNull String id) {
		if (!idPattern.matcher(id).matches())
			throw new IllegalArgumentException("`id` isn't match with regex \"[a-zA-Z0-9!]+\"");

		this.id = id;
		this.driveId = null;
		this.path = ITEM_ID_PREFIX + id;
	}

	public IdPointer(@NotNull String id, @Nullable String driveId) {
		if (!idPattern.matcher(id).matches())
			throw new IllegalArgumentException("`id` isn't match with regex \"[a-zA-Z0-9!]+\"");

		this.id = id;
		this.driveId = driveId;
		this.path = "/drives/" + driveId + "/items/" + id;
	}

	@Override
	public @NotNull String toJson() {
		return "{\"id\":\"" + path + "\"}";
	}

	@Override
	public @NotNull String resolveOperator(@NotNull Operator op) {
		return path + '/' + op;
	}

	@Override
	public @NotNull URI toURI() throws URISyntaxException {
		return new URI(RequestTool.SCHEME, RequestTool.HOST, path, null);
	}

	@Override
	public @NotNull String toApi() {
		return path;
	}

	@Override
	public @NotNull String toASCIIApi() {
		return path;
	}
}
