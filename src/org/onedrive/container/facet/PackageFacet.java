package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/location_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class PackageFacet {
	@Getter protected final String type;

	protected PackageFacet(String type) {
		this.type = type;
	}

	@Nullable
	public static PackageFacet parse(JSONObject json) {
		if (json == null) return null;

		return new PackageFacet(json.getString("type"));
	}
}
