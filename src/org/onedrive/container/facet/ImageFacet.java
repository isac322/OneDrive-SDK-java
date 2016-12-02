package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * <a href="https://dev.onedrive.com/facets/image_facet.htm">https://dev.onedrive.com/facets/image_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
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
