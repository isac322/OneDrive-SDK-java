package com.bhyoo.onedrive.container;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

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

	@Getter @Setter(PRIVATE) protected @Nullable String displayName;
	@Getter @Setter(PRIVATE) protected @Nullable String email;
	@Getter @Setter(PRIVATE) protected @Nullable String id;
	// TODO: custom class for this variable
	@Getter @Setter(PRIVATE) protected @Nullable ObjectNode thumbnails;

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
