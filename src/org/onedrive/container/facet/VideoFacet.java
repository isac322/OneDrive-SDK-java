package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.onedrive.network.legacy.HttpsRequest;

/**
 * <a href="https://dev.onedrive.com/facets/video_facet.htm">https://dev.onedrive.com/facets/video_facet.htm</a>
 * {@// TODO: Enhance javadoc}
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
						 @JsonProperty("width") Long width) throws IllegalArgumentException {
		if (bitrate == null || duration == null || height == null || width == null) {
			throw new IllegalArgumentException(HttpsRequest.NETWORK_ERR_MSG);
		}
		this.bitrate = bitrate;
		this.duration = duration;
		this.height = height;
		this.width = width;
	}
}
