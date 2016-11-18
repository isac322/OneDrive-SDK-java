package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * <a href="https://dev.onedrive.com/facets/jumpinfo_facet.htm">https://dev.onedrive.com/facets/jumpinfo_facet.htm</a>
 * {@// TODO: Enhance javadoc}
 * {@// TODO: merge with BaseItem if possible }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SpecialFolderFacet {
	@Getter protected final String name;

	@JsonCreator
	protected SpecialFolderFacet(@JsonProperty("name") String name) {
		this.name = name;
	}
}
