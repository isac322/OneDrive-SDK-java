package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * <a href="https://dev.onedrive.com/facets/folder_facet.htm">https://dev.onedrive.com/facets/folder_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FolderFacet {
	@Getter protected final long childCount;

	@JsonCreator
	protected FolderFacet(@JsonProperty("childCount") Long childCount) {
		assert childCount != null : "childCount field is null in FolderFacet!!";

		this.childCount = childCount;
	}
}
