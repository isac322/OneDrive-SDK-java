package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * <a href="https://dev.onedrive.com/facets/photo_facet.htm">https://dev.onedrive.com/facets/photo_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class PhotoFacet {
	@Getter protected final @Nullable String cameraMake;
	@Getter protected final @Nullable String cameraModel;
	@Getter protected final @Nullable Double exposureDenominator;
	@Getter protected final @Nullable Double exposureNumerator;
	@Getter protected final @Nullable Double fNumber;
	@Getter protected final @Nullable Double focalLength;
	@Getter protected final @Nullable Long iso;
	@Getter protected final @Nullable String takenDateTime;

	protected PhotoFacet(@Nullable String cameraMake, @Nullable String cameraModel,
						 @Nullable Double exposureDenominator, @Nullable Double exposureNumerator,
						 @Nullable Double fNumber, @Nullable Double focalLength, @Nullable Long iso,
						 @Nullable String takenDateTime) {
		this.cameraMake = cameraMake;
		this.cameraModel = cameraModel;
		this.exposureDenominator = exposureDenominator;
		this.exposureNumerator = exposureNumerator;
		this.fNumber = fNumber;
		this.focalLength = focalLength;
		this.iso = iso;
		this.takenDateTime = takenDateTime;
	}

	public static PhotoFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String cameraMake = null;
		@Nullable String cameraModel = null;
		@Nullable Double exposureDenominator = null;
		@Nullable Double exposureNumerator = null;
		@Nullable Double fNumber = null;
		@Nullable Double focalLength = null;
		@Nullable Long iso = null;
		@Nullable String takenDateTime = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "cameraMake":
					cameraMake = parser.getText();
					break;
				case "cameraModel":
					cameraModel = parser.getText();
					break;
				case "exposureDenominator":
					exposureDenominator = parser.getDoubleValue();
					break;
				case "exposureNumerator":
					exposureNumerator = parser.getDoubleValue();
					break;
				case "fNumber":
					fNumber = parser.getDoubleValue();
					break;
				case "focalLength":
					focalLength = parser.getDoubleValue();
					break;
				case "iso":
					iso = parser.getLongValue();
					break;
				case "takenDateTime":
					takenDateTime = parser.getText();
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in PhotoFacet : " + currentName);
			}
		}

		return new PhotoFacet(cameraMake, cameraModel, exposureDenominator,
				exposureNumerator, fNumber, focalLength, iso, takenDateTime);
	}
}
