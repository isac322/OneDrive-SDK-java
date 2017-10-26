package com.bhyoo.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc }
// TODO: is there any way to merge with {@link BasePointer}? cause it's conflict
// in behavior

/**
 * <a href="https://dev.onedrive.com/resources/itemReference.htm">https://dev.onedrive.com/resources/itemReference
 * .htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@ToString
@JsonDeserialize(converter = ItemReference.PointerInjector.class)
public class ItemReference {
	@Setter(PRIVATE) @Getter @NotNull protected String driveId;
	@Setter(PRIVATE) @Getter @Nullable protected String id;
	@Getter @JsonIgnore @Nullable protected PathPointer pathPointer;
	@Setter(PRIVATE) @Getter @Nullable @JsonProperty("path") protected String rawPath;

	// used by jackson deserialize
	@SuppressWarnings("unused") ItemReference() {}

	ItemReference(@NotNull String driveId, @Nullable String id, @Nullable PathPointer pathPointer) {
		this.driveId = driveId;
		this.id = id;
		this.pathPointer = pathPointer;

		if (pathPointer != null)
			this.rawPath = pathPointer.toASCIIApi();
		else
			this.rawPath = null;
	}

	static class PointerInjector extends StdConverter<ItemReference, ItemReference> {
		@Override public ItemReference convert(ItemReference value) {
			if (value.rawPath != null)
				value.pathPointer = new PathPointer(value.rawPath, value.driveId);
			else
				value.pathPointer = null;
			return value;
		}
	}
}
