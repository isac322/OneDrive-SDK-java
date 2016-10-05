package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.Nullable;
import lombok.Getter;

/**
 * https://dev.onedrive.com/facets/location_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class LocationFacet {
	@Getter @Nullable protected final Double altitude;
	@Getter @Nullable protected final Double latitude;
	@Getter @Nullable protected final Double longitude;

	@JsonCreator
	protected LocationFacet(@JsonProperty("altitude") Double altitude,
							@JsonProperty("latitude") Double latitude,
							@JsonProperty("longitude") Double longitude) {
		this.altitude = altitude;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
