package org.onedrive.container.facet;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/folder_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FolderFacet {
	@Getter @NotNull protected final long childCount;

	protected FolderFacet(long childCount) {
		this.childCount = childCount;
	}

	@Nullable
	public static FolderFacet parse(JSONObject json) {
		if (json == null) return null;

		return new FolderFacet(json.getLong("childCount"));
	}
}
