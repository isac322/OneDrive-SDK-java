package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.container.items.pointer.PathPointer;

/**
 * https://dev.onedrive.com/resources/itemReference.htm
 * {@// TODO: Enhance javadoc }
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
							@JsonProperty("path") @Nullable String asciiPath) {
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
