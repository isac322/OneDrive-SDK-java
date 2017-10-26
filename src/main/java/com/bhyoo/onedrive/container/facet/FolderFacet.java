package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/folder_facet.htm">https://dev.onedrive.com/facets/folder_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FolderFacet {
	@Getter protected final long childCount;

	@JsonCreator
	protected FolderFacet(@JsonProperty("childCount") Long childCount) {
		assert childCount != null : "childCount field is null in FolderFacet!!";

		this.childCount = childCount;
	}
}
