package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/jumpinfo_facet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
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
