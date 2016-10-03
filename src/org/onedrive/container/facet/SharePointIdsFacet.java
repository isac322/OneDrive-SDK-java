package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/sharepointIds_facet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class SharePointIdsFacet {
	@Getter protected final String siteId;
	@Getter protected final String webId;
	@Getter protected final String listId;
	@Getter protected final long listItemId;
	@Getter protected final String listItemUniqueId;

	protected SharePointIdsFacet(String siteId, String webId, String listId, long listItemId, String listItemUniqueId) {
		this.siteId = siteId;
		this.webId = webId;
		this.listId = listId;
		this.listItemId = listItemId;
		this.listItemUniqueId = listItemUniqueId;
	}

	@Nullable
	public static SharePointIdsFacet parse(JSONObject json) {
		if (json == null) return null;

		return new SharePointIdsFacet(
				json.getString("siteId"),
				json.getString("webId"),
				json.getString("listId"),
				json.getLong("listItemId"),
				json.getString("listItemUniqueId")
		);
	}
}
