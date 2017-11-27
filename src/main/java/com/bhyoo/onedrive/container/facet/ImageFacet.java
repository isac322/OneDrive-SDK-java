package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * <a href="https://dev.onedrive.com/facets/image_facet.htm">https://dev.onedrive.com/facets/image_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class ImageFacet {
	@Getter protected final int width;
	@Getter protected final int height;

	protected ImageFacet(@NotNull Integer width, @NotNull Integer height) {
		this.width = width;
		this.height = height;
	}

	public static ImageFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@NotNull Integer width = null, height = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "width":
					width = parser.getIntValue();
					break;
				case "height":
					height = parser.getIntValue();
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in ImageFacet : " + currentName);
			}
		}

		return new ImageFacet(width, height);
	}
}
