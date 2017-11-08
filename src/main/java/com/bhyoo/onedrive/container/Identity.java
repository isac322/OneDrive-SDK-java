package com.bhyoo.onedrive.container;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

// TODO: Enhance javadoc

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = {"id"})
@JsonDeserialize(converter = Identity.IdentityConverter.class)
public class Identity {
	private static ConcurrentHashMap<String, Identity> identitySet = new ConcurrentHashMap<>();
	@Getter @Setter(AccessLevel.PRIVATE) @NotNull protected String id;
	@Getter @Setter(AccessLevel.PRIVATE) @Nullable protected String displayName;
	@Getter @Setter(AccessLevel.PRIVATE) @Nullable protected ObjectNode thumbnails;

	@Override
	public String toString() {
		return displayName + ", " + id;
	}

	static class IdentityConverter extends StdConverter<Identity, Identity> {
		@Override public Identity convert(Identity value) {
			Identity identity = identitySet.get(value.id);

			if (identity != null) return identity;

			identitySet.put(value.id, value);
			return value;
		}
	}
}
