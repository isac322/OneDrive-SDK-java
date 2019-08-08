package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

// TODO: merge with AbstractDriveItem if possible

/**
 * <a href="https://dev.onedrive.com/facets/jumpinfo_facet.htm">https://dev.onedrive.com/facets/jumpinfo_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class SpecialFolderFacet {
	@Getter protected final @Nullable String name;

	protected SpecialFolderFacet(@Nullable String name) {this.name = name;}

	public static SpecialFolderFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String name = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "name":
					name = parser.getText();
					break;
				default:
					Logger.getGlobal().info(
							"Unknown attribute detected in SpecialFolderFacet : " + currentName
					);
			}
		}

		return new SpecialFolderFacet(name);
	}
}
