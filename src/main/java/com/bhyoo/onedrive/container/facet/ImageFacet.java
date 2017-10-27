package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/image_facet.htm">https://dev.onedrive.com/facets/image_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class ImageFacet {
	@Getter protected final long width;
	@Getter protected final long height;

	@JsonCreator
	protected ImageFacet(@JsonProperty("width") Long width,
						 @JsonProperty("height") Long height) {
		assert width != null : "width field is null in ImageFacet!!";
		assert height != null : "height field is null in ImageFacet!!";

		this.width = width;
		this.height = height;
	}
}
