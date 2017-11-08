package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.PackageFacet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href='https://dev.onedrive.com/facets/package_facet.htm'>https://dev.onedrive.com/facets/package_facet.htm</a>
 * <p>
 * Because there is only one package type item in OneDrive now, this class inherits {@link FileItem}.
 * <p>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = PackageItem.class, converter = PackageItem.PointerInjector.class)
public class PackageItem extends DriveItem {
	@Setter(PRIVATE) @JsonProperty("package") @NotNull private PackageFacet packages;

	@JsonIgnore public String getType() {
		return packages.getType();
	}


	static class PointerInjector extends DriveItem.PointerInjector<PackageItem> {}
}
