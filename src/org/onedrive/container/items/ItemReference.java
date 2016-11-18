package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.container.items.pointer.PathPointer;

/**
 * <a href="https://dev.onedrive.com/resources/itemReference.htm">https://dev.onedrive.com/resources/itemReference
 * .htm</a>
 * {@// TODO: Enhance javadoc }
 * {@// TODO: is there any way to merge with {@link org.onedrive.container.items.pointer.BasePointer}? cause it's
 * conflict in behavior }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ItemReference {
	@Getter @NotNull protected final String driveId;
	@Getter @Nullable protected final String id;
	@Getter(onMethod = @__(@JsonIgnore)) @Nullable protected final PathPointer pathPointer;
	@Getter @Nullable @JsonProperty("path") protected final String rawPath;

	@JsonCreator
	protected ItemReference(@JsonProperty("driveId") @NotNull String driveId,
							@JsonProperty("id") @Nullable String id,
							@JsonProperty("path") @Nullable String asciiPath) throws IllegalArgumentException {
		this.driveId = driveId;
		this.id = id;
		this.rawPath = asciiPath;

		if (asciiPath != null)
			this.pathPointer = new PathPointer(asciiPath, driveId);
		else
			this.pathPointer = null;
	}

	protected ItemReference(@NotNull String driveId, @Nullable String id, @Nullable PathPointer pathPointer) {
		this.driveId = driveId;
		this.id = id;
		this.pathPointer = pathPointer;

		if (pathPointer != null)
			this.rawPath = pathPointer.toASCIIApi();
		else
			this.rawPath = null;
	}
}
