package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/video_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class VideoFacet {
	@Getter protected final long bitrate;
	@Getter protected final long duration;
	@Getter protected final long height;
	@Getter protected final long width;

	protected VideoFacet(long bitrate, long duration, long height, long width) {
		this.bitrate = bitrate;
		this.duration = duration;
		this.height = height;
		this.width = width;
	}

	@Nullable
	public static VideoFacet parse(JSONObject json) {
		if (json == null) return null;

		return new VideoFacet(
				json.getLong("bitrate"),
				json.getLong("duration"),
				json.getLong("height"),
				json.getLong("width"));
	}
}
