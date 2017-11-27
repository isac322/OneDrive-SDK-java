package com.bhyoo.onedrive.container;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = "id")
@JsonDeserialize(converter = Identity.IdentityConverter.class)
@ToString(exclude = "thumbnails", doNotUseGetters = true)
public class Identity {
	private static ConcurrentHashMap<String, Identity> identitySet = new ConcurrentHashMap<>();

	@Getter final @Nullable String displayName;
	@Getter final @Nullable String email;
	@Getter final @Nullable String id;
	// TODO: custom class for this variable
	@Getter final @Nullable ObjectNode thumbnails;


	protected Identity(@Nullable String displayName, @Nullable String email,
					   @Nullable String id, @Nullable ObjectNode thumbnails) {
		this.displayName = displayName;
		this.email = email;
		this.id = id;
		this.thumbnails = thumbnails;
	}

	static Identity deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String displayName = null;
		@Nullable String email = null;
		@Nullable String id = null;
		// TODO: custom class for this variable
		@Nullable ObjectNode thumbnails = null;

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
					thumbnails = parser.readValueAs(ObjectNode.class);
					break;
				case "@odata.type":
					// TODO
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in Identity : " + currentName);
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

	static class IdentityConverter extends StdConverter<Identity, Identity> {
		@Override public Identity convert(Identity value) {
			if (value.id != null) {
				Identity identity = identitySet.get(value.id);

				if (identity != null && identity.countNull() <= value.countNull()) return identity;

				identitySet.put(value.id, value);
			}
			return value;
		}
	}
}
