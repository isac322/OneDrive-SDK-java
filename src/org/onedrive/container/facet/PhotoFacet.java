package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.onedrive.container.BaseContainer;

import java.time.ZonedDateTime;

/**
 * https://dev.onedrive.com/facets/photo_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class PhotoFacet {
	/**
	 * Always ensure local date time.
	 */
	@Getter @Nullable protected final ZonedDateTime takenDateTime;
	@Getter @Nullable protected final String cameraMake;
	@Getter @Nullable protected final String cameraModel;
	@Getter @Nullable protected final Double fNumber;
	@Getter @Nullable protected final Double exposureDenominator;
	@Getter @Nullable protected final Double exposureNumerator;
	@Getter @Nullable protected final Double focalLength;
	@Getter @Nullable protected final Long iso;

	@JsonCreator
	protected PhotoFacet(@Nullable @JsonProperty("takenDateTime") String takenDateTime,
						 @Nullable @JsonProperty("cameraMake") String cameraMake,
						 @Nullable @JsonProperty("cameraModel") String cameraModel,
						 @Nullable @JsonProperty("fNumber") Double fNumber,
						 @Nullable @JsonProperty("exposureDenominator") Double exposureDenominator,
						 @Nullable @JsonProperty("exposureNumerator") Double exposureNumerator,
						 @Nullable @JsonProperty("focalLength") Double focalLength,
						 @Nullable @JsonProperty("iso") Long iso) {
		this.takenDateTime = takenDateTime == null ? null : BaseContainer.parseDateTime(takenDateTime);
		this.cameraMake = cameraMake;
		this.cameraModel = cameraModel;
		this.fNumber = fNumber;
		this.exposureDenominator = exposureDenominator;
		this.exposureNumerator = exposureNumerator;
		this.focalLength = focalLength;
		this.iso = iso;
	}
}
