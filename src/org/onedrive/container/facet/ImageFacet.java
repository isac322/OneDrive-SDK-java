package org.onedrive.container.facet;

import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/image_facet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class ImageFacet {
	@Getter protected final long width;
	@Getter protected final long height;

	protected ImageFacet(long width, long height) {
		this.width = width;
		this.height = height;
	}

	public static ImageFacet parse(JSONObject json) {
		if (json == null) return null;

		return new ImageFacet(
				json.getLong("width"),
				json.getLong("height"));
	}
}
