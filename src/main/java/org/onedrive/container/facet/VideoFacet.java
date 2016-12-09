package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * <a href="https://dev.onedrive.com/facets/video_facet.htm">https://dev.onedrive.com/facets/video_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class VideoFacet {
	@Getter protected final long bitrate;
	@Getter protected final long duration;
	@Getter protected final long height;
	@Getter protected final long width;

	@JsonCreator
	protected VideoFacet(@JsonProperty("bitrate") Long bitrate,
						 @JsonProperty("duration") Long duration,
						 @JsonProperty("height") Long height,
						 @JsonProperty("width") Long width) {
		assert bitrate != null : "bitrate field is null in VideoFacet!!";
		assert duration != null : "duration field is null in VideoFacet!!";
		assert height != null : "height field is null in VideoFacet!!";
		assert width != null : "width field is null in VideoFacet!!";

		this.bitrate = bitrate;
		this.duration = duration;
		this.height = height;
		this.width = width;
	}
}
