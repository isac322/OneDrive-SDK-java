package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.DriveType;
import com.bhyoo.onedrive.container.facet.SharePointIdsFacet;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
@EqualsAndHashCode(of = {"id", "driveId"})
@JsonDeserialize(converter = ItemReference.PointerInjector.class)
public class ItemReference {
	@Getter @Setter(PRIVATE) protected @NotNull String driveId;
	@Getter @Setter(PRIVATE) protected @Nullable DriveType driveType;
	@Getter @Setter(PRIVATE) protected @Nullable String id;
	@Getter @Setter(PRIVATE) protected @Nullable String name;
	@Getter @JsonIgnore protected @Nullable PathPointer pathPointer;
	@JsonProperty("path")
	@Getter @Setter(PRIVATE) protected @Nullable String rawPath;
	@Getter @Setter(PRIVATE) protected @Nullable String shareId;
	@Getter @Setter(PRIVATE) protected @Nullable SharePointIdsFacet sharepointIds;

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
