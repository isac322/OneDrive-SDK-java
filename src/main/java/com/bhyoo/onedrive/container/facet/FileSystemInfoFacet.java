package com.bhyoo.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/filesysteminfo_facet.htm">https://dev.onedrive
 * .com/facets/filesysteminfo_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FileSystemInfoFacet {
	@Getter @Setter(PRIVATE) @NotNull protected String createdDateTime;
	@Getter @Setter(PRIVATE) @NotNull protected String lastModifiedDateTime;
}
