package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * <a href="https://dev.onedrive.com/facets/filesysteminfo_facet.htm">https://dev.onedrive
 * .com/facets/filesysteminfo_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FileSystemInfoFacet {
	@Getter protected final @NotNull String createdDateTime;
	@Getter protected final @NotNull String lastModifiedDateTime;

	protected FileSystemInfoFacet(@NotNull String createdDateTime, @NotNull String lastModifiedDateTime) {
		this.createdDateTime = createdDateTime;
		this.lastModifiedDateTime = lastModifiedDateTime;
	}

	public static FileSystemInfoFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String createdDateTime = null;
		@Nullable String lastModifiedDateTime = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "createdDateTime":
					createdDateTime = parser.getText();
					break;
				case "lastModifiedDateTime":
					lastModifiedDateTime = parser.getText();
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in FileSystemInfoFacet : " + currentName);
			}
		}

		assert createdDateTime != null : "createdDateTime is null";
		assert lastModifiedDateTime != null : "lastModifiedDateTime is null";

		return new FileSystemInfoFacet(createdDateTime, lastModifiedDateTime);
	}
}
