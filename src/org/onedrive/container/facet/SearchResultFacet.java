package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.NotNull;
import lombok.Getter;

/**
 * https://dev.onedrive.com/facets/searchresult_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SearchResultFacet {
	@Getter @NotNull protected final String onClickTelemetryUrl;

	@JsonCreator
	protected SearchResultFacet(@JsonProperty("onClickTelemetryUrl") String onClickTelemetryUrl) {
		this.onClickTelemetryUrl = onClickTelemetryUrl;
	}
}
