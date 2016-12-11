package org.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

/**
 * <a href="https://dev.onedrive.com/facets/filesysteminfo_facet.htm">https://dev.onedrive
 * .com/facets/filesysteminfo_facet.htm</a>
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileSystemInfoFacet {
	@Getter @Setter(PRIVATE) @NotNull protected String createdDateTime;
	@Getter @Setter(PRIVATE) @NotNull protected String lastModifiedDateTime;
}
