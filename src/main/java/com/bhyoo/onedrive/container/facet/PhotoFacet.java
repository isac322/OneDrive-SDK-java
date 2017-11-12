package com.bhyoo.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/photo_facet.htm">https://dev.onedrive.com/facets/photo_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class PhotoFacet {
	/**
	 * Always ensure local date time.
	 */
	@Getter @Setter(PRIVATE) protected @Nullable String cameraMake;
	@Getter @Setter(PRIVATE) protected @Nullable String cameraModel;
	@Getter @Setter(PRIVATE) protected @Nullable Double exposureDenominator;
	@Getter @Setter(PRIVATE) protected @Nullable Double exposureNumerator;
	@Getter @Setter(PRIVATE) protected @Nullable Double fNumber;
	@Getter @Setter(PRIVATE) protected @Nullable Double focalLength;
	@Getter @Setter(PRIVATE) protected @Nullable Long iso;
	@Getter @Setter(PRIVATE) protected @Nullable String takenDateTime;
}
