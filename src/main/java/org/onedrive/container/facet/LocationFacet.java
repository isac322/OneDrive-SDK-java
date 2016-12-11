package org.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

/**
 * <a href="https://dev.onedrive.com/facets/location_facet.htm">https://dev.onedrive.com/facets/location_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class LocationFacet {
	@Getter @Setter(PRIVATE) @Nullable protected Double altitude;
	@Getter @Setter(PRIVATE) @Nullable protected Double latitude;
	@Getter @Setter(PRIVATE) @Nullable protected Double longitude;
}
