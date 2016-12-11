package org.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

/**
 * <a href="https://dev.onedrive.com/facets/photo_facet.htm">https://dev.onedrive.com/facets/photo_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class PhotoFacet {
	/**
	 * Always ensure local date time.
	 */
	@Getter @Setter(PRIVATE) @Nullable protected String takenDateTime;
	@Getter @Setter(PRIVATE) @Nullable protected String cameraMake;
	@Getter @Setter(PRIVATE) @Nullable protected String cameraModel;
	@Getter @Setter(PRIVATE) @Nullable protected Double fNumber;
	@Getter @Setter(PRIVATE) @Nullable protected Double exposureDenominator;
	@Getter @Setter(PRIVATE) @Nullable protected Double exposureNumerator;
	@Getter @Setter(PRIVATE) @Nullable protected Double focalLength;
	@Getter @Setter(PRIVATE) @Nullable protected Long iso;
}
