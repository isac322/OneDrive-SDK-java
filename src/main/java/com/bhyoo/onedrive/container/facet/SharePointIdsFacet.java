package com.bhyoo.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/sharepointIds_facet.htm">https://dev.onedrive
 * .com/facets/sharepointIds_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class SharePointIdsFacet {
	@Getter @Setter(PRIVATE) protected @Nullable String listId;
	@Getter @Setter(PRIVATE) protected @Nullable String listItemId;
	@Getter @Setter(PRIVATE) protected @Nullable String listItemUniqueId;
	@Getter @Setter(PRIVATE) protected @Nullable String siteId;
	@Getter @Setter(PRIVATE) protected @Nullable URI siteUrl;
	@Getter @Setter(PRIVATE) protected @Nullable String webId;
}
