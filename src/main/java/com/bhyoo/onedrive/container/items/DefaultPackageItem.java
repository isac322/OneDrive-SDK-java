package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.facet.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * <a href='https://dev.onedrive.com/facets/package_facet.htm'>https://dev.onedrive.com/facets/package_facet.htm</a>
 * <p>
 * Because there is only one package type item in OneDrive now, this class inherits {@link DefaultFileItem}.
 * <p>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class DefaultPackageItem extends AbstractDriveItem implements PackageItem {
	@NotNull private PackageFacet packages;

	DefaultPackageItem(@NotNull String id, @NotNull IdentitySet creator, @NotNull String createdDateTime,
					   @Nullable String description, @NotNull String eTag, @NotNull IdentitySet lastModifier,
					   @NotNull String lastModifiedDateTime, @NotNull String name, @NotNull URI webUrl,
					   @NotNull Client client, @NotNull String cTag, @Nullable ObjectNode deleted,
					   FileSystemInfoFacet fileSystemInfo, @NotNull ItemReference parentReference,
					   @Nullable SearchResultFacet searchResult, @Nullable SharedFacet shared,
					   @Nullable SharePointIdsFacet sharePointIds, @NotNull Long size, URI webDavUrl,
					   @NotNull PackageFacet packages) {
		super(id, creator, createdDateTime, description, eTag, lastModifier, lastModifiedDateTime, name, webUrl,
				client, cTag, deleted, fileSystemInfo, parentReference, searchResult, shared, sharePointIds, size,
				webDavUrl);
		this.packages = packages;

		createPointers();
	}

	@Override public PackageType getType() {
		return packages.getType();
	}
}
