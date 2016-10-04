package org.onedrive.container.facet;

import com.sun.istack.internal.NotNull;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/image_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class ImageFacet {
	@Getter @NotNull protected final long width;
	@Getter @NotNull protected final long height;

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
