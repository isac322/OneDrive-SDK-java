package org.onedrive.container.facet;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/location_facet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class LocationFacet {
	@Getter protected final double altitude;
	@Getter protected final double latitude;
	@Getter protected final double longitude;

	protected LocationFacet(double altitude, double latitude, double longitude) {
		this.altitude = altitude;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Nullable
	public static LocationFacet parse(JSONObject json) {
		if (json == null) return null;

		return new LocationFacet(
				json.getDouble("altitude"),
				json.getDouble("latitude"),
				json.getDouble("longitude"));
	}
}
