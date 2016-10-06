package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
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
	protected PhotoFacet(@JsonProperty("takenDateTime") String takenDateTime,
						 @JsonProperty("cameraMake") String cameraMake,
						 @JsonProperty("cameraModel") String cameraModel,
						 @JsonProperty("fNumber") Double fNumber,
						 @JsonProperty("exposureDenominator") Double exposureDenominator,
						 @JsonProperty("exposureNumerator") Double exposureNumerator,
						 @JsonProperty("focalLength") Double focalLength,
						 @JsonProperty("iso") Long iso) {
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
