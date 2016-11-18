package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * <a href="https://dev.onedrive.com/facets/package_facet.htm">https://dev.onedrive.com/facets/package_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 * {@// TODO: merge with BaseItem if possible }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class PackageFacet {
	@Getter @Nullable protected final String type;

	@JsonCreator
	protected PackageFacet(@Nullable @JsonProperty("type") String type) {
		this.type = type;
	}
}
