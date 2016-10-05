package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import lombok.Getter;

/**
 * https://dev.onedrive.com/facets/folder_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FolderFacet {
	@Getter @NotNull protected final long childCount;

	@JsonCreator
	protected FolderFacet(@JsonProperty("childCount") Long childCount) {
		if (childCount == null) {
			throw new RuntimeException("\"childCount\" filed can not be null");
		}
		this.childCount = childCount;
	}
}
