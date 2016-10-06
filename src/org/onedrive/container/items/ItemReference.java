package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;

/**
 * https://dev.onedrive.com/resources/itemReference.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ItemReference {
	@Getter @NotNull protected final String driveId;
	@Getter @Nullable protected final String id;
	@Getter @Nullable protected final String path;

	@JsonCreator
	protected ItemReference(@JsonProperty("driveId") String driveId,
							@JsonProperty("id") String id,
							@JsonProperty("path") String path) {
		this.driveId = driveId;
		this.id = id;
		this.path = path;
	}
}
