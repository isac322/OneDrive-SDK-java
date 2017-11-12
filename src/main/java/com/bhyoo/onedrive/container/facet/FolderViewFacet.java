package com.bhyoo.onedrive.container.facet;


// TODO: Enhance javadoc

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

/**
 * <a href="https://dev.onedrive.com/facets/folder_facet.htm">https://dev.onedrive.com/facets/folder_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class FolderViewFacet {
	@Getter @Setter(PRIVATE) protected @NotNull SortType sortBy;
	@Getter @Setter(PRIVATE) protected @NotNull SortOrderType sortOrder;
	@Getter @Setter(PRIVATE) protected @NotNull ViewType viewType;
}
