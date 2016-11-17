package org.onedrive.container.items.pointer;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class IdPointer extends BasePointer {
	@Getter @Nullable private final String driveId;
	@Getter @NotNull private final String id;
	@NotNull private final String path;


	public IdPointer(@NotNull String id) {
		this.id = id;
		this.driveId = null;
		this.path = "/drive/items/" + id;
	}

	public IdPointer(@NotNull String id, @Nullable String driveId) {
		this.id = id;
		this.driveId = driveId;
		this.path = "/drives/" + driveId + "/items/" + id;
	}

	@NotNull
	@Override
	public String resolveOperator(@NotNull String operator) {
		return path + '/' + operator;
	}

	@NotNull
	@Override
	public String toJson() {
		return "{\"id\":\"" + path + "\"}";
	}

	@NotNull
	@Override
	public URI toURI() throws URISyntaxException {
		return new URI("https", null, path, null);
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
