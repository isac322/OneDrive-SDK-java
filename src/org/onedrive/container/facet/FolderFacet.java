package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/folder_facet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class FolderFacet {
	@Getter protected final long childCount;

	protected FolderFacet(long childCount) {
		this.childCount = childCount;
	}

	@Nullable
	public static FolderFacet parse(JSONObject json) {
		if (json == null) return null;

		return new FolderFacet(json.getLong("childCount"));
	}
}
