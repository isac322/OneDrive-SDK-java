package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * <a href="https://dev.onedrive.com/facets/location_facet.htm">https://dev.onedrive.com/facets/location_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class LocationFacet {
	@Getter protected final @Nullable Double altitude;
	@Getter protected final @Nullable Double latitude;
	@Getter protected final @Nullable Double longitude;

	protected LocationFacet(@Nullable Double altitude, @Nullable Double latitude, @Nullable Double longitude) {
		this.altitude = altitude;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public static LocationFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable Double altitude = null;
		@Nullable Double latitude = null;
		@Nullable Double longitude = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "altitude":
					altitude = parser.getDoubleValue();
					break;
				case "latitude":
					latitude = parser.getDoubleValue();
					break;
				case "longitude":
					longitude = parser.getDoubleValue();
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in LocationFacet : " + currentName);
			}
		}

		return new LocationFacet(altitude, latitude, longitude);
	}
}
