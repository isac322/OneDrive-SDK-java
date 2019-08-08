package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.container.facet.ThumbnailSet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = "id")
@ToString(exclude = "thumbnails", doNotUseGetters = true)
public class Identity {
	private static final ConcurrentHashMap<String, Identity> identitySet = new ConcurrentHashMap<>();

	@Getter protected final @Nullable String displayName;
	@Getter protected final @Nullable String email;
	@Getter protected final @Nullable String id;
	@Getter protected final @Nullable ThumbnailSet thumbnails;


	protected Identity(@Nullable String displayName, @Nullable String email,
					   @Nullable String id, @Nullable ThumbnailSet thumbnails) {
		this.displayName = displayName;
		this.email = email;
		this.id = id;
		this.thumbnails = thumbnails;
	}

	static Identity deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String displayName = null;
		@Nullable String email = null;
		@Nullable String id = null;
		@Nullable ThumbnailSet thumbnails = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "displayName":
					displayName = parser.getText();
					break;
				case "email":
					email = parser.getText();
					break;
				case "id":
					id = parser.getText();
					break;
				case "thumbnails":
					thumbnails = ThumbnailSet.deserialize(parser);
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in Identity : " + currentName);
			}
		}

		if (id != null) {
			Identity identity = identitySet.get(id);

			Identity value = new Identity(displayName, email, id, thumbnails);

			// FIXME: merge two objects into a bigger object if possible
			if (identity != null && identity.countNull() <= value.countNull()) return identity;

			return identitySet.put(id, value);
		}
		else {
			return new Identity(displayName, email, null, thumbnails);
		}
	}

	private int countNull() {
		return (displayName == null ? 1 : 0) + (email == null ? 1 : 0) +
				(id == null ? 1 : 0) + (thumbnails == null ? 1 : 0);
	}
}
