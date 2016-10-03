package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.onedrive.container.BaseContainer;

import java.time.ZonedDateTime;

/**
 * https://dev.onedrive.com/facets/filesysteminfo_facet.htm
 * TODO: enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class FileSystemInfoFacet {
	@Getter protected final ZonedDateTime createdDateTime;
	@Getter protected final ZonedDateTime lastModifiedDateTime;

	protected FileSystemInfoFacet(ZonedDateTime createdDateTime, ZonedDateTime lastModifiedDateTime) {
		this.createdDateTime = createdDateTime;
		this.lastModifiedDateTime = lastModifiedDateTime;
	}

	@Nullable
	public static FileSystemInfoFacet parse(JSONObject json) {
		if (json == null) return null;

		return new FileSystemInfoFacet(
				BaseContainer.parseDateTime(json.getString("createdDateTime")),
				BaseContainer.parseDateTime(json.getString("lastModifiedDateTime"))
		);
	}
}
