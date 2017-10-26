package com.bhyoo.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/sharepointIds_facet.htm">https://dev.onedrive
 * .com/facets/sharepointIds_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class SharePointIdsFacet {
	@Getter @Setter(PRIVATE) @Nullable protected String siteId;
	@Getter @Setter(PRIVATE) @Nullable protected String webId;
	@Getter @Setter(PRIVATE) @Nullable protected String listId;
	@Getter @Setter(PRIVATE) @Nullable protected Long listItemId;
	@Getter @Setter(PRIVATE) @Nullable protected String listItemUniqueId;
}
