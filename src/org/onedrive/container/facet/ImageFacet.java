package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * https://dev.onedrive.com/facets/image_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ImageFacet {
	@Getter protected final long width;
	@Getter protected final long height;

	@JsonCreator
	protected ImageFacet(@JsonProperty("width") Long width,
						 @JsonProperty("height") Long height) {
		if (width == null) {
			throw new RuntimeException("\"width\" filed can not be null");
		}
		if (height == null) {
			throw new RuntimeException("\"height\" filed can not be null");
		}
		this.width = width;
		this.height = height;
	}
}
