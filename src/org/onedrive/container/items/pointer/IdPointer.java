package org.onedrive.container.items.pointer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.utils.RequestTool;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class IdPointer extends BasePointer {
	@NotNull private static final IllegalArgumentException DOESNT_MATCH =
			new IllegalArgumentException("`id` isn't match with regex \"[a-fA-F0-9!]+\"");
	private static final Pattern idPattern = Pattern.compile("[a-fA-F0-9!]+");
	@Getter @Nullable private final String driveId;
	@Getter @NotNull private final String id;
	@NotNull private final String path;


	public IdPointer(@NotNull String id) {
		if (!idPattern.matcher(id).matches())
			throw DOESNT_MATCH;

		this.id = id;
		this.driveId = null;
		this.path = "/drive/items/" + id;
	}

	public IdPointer(@NotNull String id, @Nullable String driveId) {
		if (!idPattern.matcher(id).matches())
			throw DOESNT_MATCH;

		this.id = id;
		this.driveId = driveId;
		this.path = "/drives/" + driveId + "/items/" + id;
	}

	@NotNull
	@Override
	public String toJson() {
		return "{\"id\":\"" + path + "\"}";
	}

	@NotNull
	@Override
	public String resolveOperator(@NotNull Operator op) {
		return path + '/' + op.getString();
	}

	@NotNull
	@Override
	public URI toURI() throws URISyntaxException {
		return new URI(RequestTool.SCHEME, RequestTool.HOST, path, null);
	}

	@NotNull
	@Override
	public String toApi() {
		return path;
	}

	@NotNull
	@Override
	public String toASCIIApi() {
		return path;
	}
}
