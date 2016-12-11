package org.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.onedrive.container.IdentitySet;

import static lombok.AccessLevel.PRIVATE;

/**
 * <a href="https://dev.onedrive.com/facets/shared_facet.htm">https://dev.onedrive.com/facets/shared_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SharedFacet {
	@Getter @Setter(PRIVATE) @Nullable protected IdentitySet owner;
	@Getter @Setter(PRIVATE) @Nullable protected String scope;
}
