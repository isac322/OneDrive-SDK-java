package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/searchresult_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SearchResultFacet {
	@Getter protected final String onClickTelemetryUrl;

	protected SearchResultFacet(String onClickTelemetryUrl) {
		this.onClickTelemetryUrl = onClickTelemetryUrl;
	}

	@Nullable
	public static SearchResultFacet parse(JSONObject json) {
		if (json == null) return null;

		return new SearchResultFacet(json.getString("onClickTelemetryUrl"));
	}
}
