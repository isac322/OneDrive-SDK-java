package org.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

/**
 * <a href="https://dev.onedrive.com/facets/jumpinfo_facet.htm">https://dev.onedrive.com/facets/jumpinfo_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 * {@// TODO: merge with BaseItem if possible }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SpecialFolderFacet {
	@Getter @Setter(PRIVATE) @Nullable protected String name;
}
