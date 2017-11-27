package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
		@NotNull String createdDateTime = null;
		@NotNull String lastModifiedDateTime = null;

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
					throw new IllegalStateException(
							"Unknown attribute detected in FileSystemInfoFacet : " + currentName);
			}
		}

		return new FileSystemInfoFacet(createdDateTime, lastModifiedDateTime);
	}
}
