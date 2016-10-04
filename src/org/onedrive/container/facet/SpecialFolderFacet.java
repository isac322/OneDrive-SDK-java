package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/jumpinfo_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SpecialFolderFacet {
	@Getter protected final String name;

	protected SpecialFolderFacet(String name) {
		this.name = name;
	}

	@Nullable
	public static SpecialFolderFacet parse(JSONObject json) {
		if (json == null) return null;

		return new SpecialFolderFacet(json.getString("name"));
	}
}
