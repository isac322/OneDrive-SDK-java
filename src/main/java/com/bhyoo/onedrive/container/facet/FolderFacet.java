package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// TODO: Enhance javadoc

/**
 * <a href="https://docs.microsoft.com/ko-kr/onedrive/developer/rest-api/resources/folderview">
 * https://docs.microsoft.com/ko-kr/onedrive/developer/rest-api/resources/folderview
 * </a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FolderFacet {
	@Getter protected final long childCount;
	@Getter protected final FolderViewFacet view;

	@JsonCreator
	protected FolderFacet(@JsonProperty("childCount") Long childCount,
						  @JsonProperty("view") FolderViewFacet view) {
		assert childCount != null : "childCount field is null in FolderFacet!!";

		this.childCount = childCount;
		this.view = view;
	}
}
