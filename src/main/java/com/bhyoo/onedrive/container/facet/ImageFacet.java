package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/image_facet.htm">https://dev.onedrive.com/facets/image_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class ImageFacet {
	@Getter protected final int width;
	@Getter protected final int height;

	@JsonCreator
	protected ImageFacet(@JsonProperty("width") @NotNull Integer width,
						 @JsonProperty("height") @NotNull Integer height) {
		this.width = width;
		this.height = height;
	}
}
