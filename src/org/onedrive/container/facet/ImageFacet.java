package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * <a href="https://dev.onedrive.com/facets/image_facet.htm">https://dev.onedrive.com/facets/image_facet.htm</a>
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ImageFacet {
	@Getter protected final long width;
	@Getter protected final long height;

	@JsonCreator
	protected ImageFacet(@JsonProperty("width") Long width,
						 @JsonProperty("height") Long height) throws IllegalArgumentException {
		if (width == null) {
			throw new IllegalArgumentException("\"width\" field can not be null");
		}
		if (height == null) {
			throw new IllegalArgumentException("\"height\" field can not be null");
		}
		this.width = width;
		this.height = height;
	}
}
