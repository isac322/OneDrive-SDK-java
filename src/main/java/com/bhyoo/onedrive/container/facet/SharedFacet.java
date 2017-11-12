package com.bhyoo.onedrive.container.facet;

import com.bhyoo.onedrive.container.IdentitySet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/shared_facet.htm">https://dev.onedrive.com/facets/shared_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class SharedFacet {
	@Getter @Setter(PRIVATE) protected @Nullable IdentitySet owner;
	@Getter @Setter(PRIVATE) protected @Nullable ShareScopeType scope;
	@Getter @Setter(PRIVATE) protected @Nullable IdentitySet sharedBy;
	@Getter @Setter(PRIVATE) protected @Nullable String sharedDateTime;
}
