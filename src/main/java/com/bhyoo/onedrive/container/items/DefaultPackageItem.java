package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.PackageFacet;
import com.bhyoo.onedrive.container.facet.PackageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href='https://dev.onedrive.com/facets/package_facet.htm'>https://dev.onedrive.com/facets/package_facet.htm</a>
 * <p>
 * Because there is only one package type item in OneDrive now, this class inherits {@link DefaultFileItem}.
 * <p>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = DefaultPackageItem.class, converter = DefaultPackageItem.PointerInjector.class)
public class DefaultPackageItem extends AbstractDriveItem implements PackageItem {
	@Setter(PRIVATE) @JsonProperty("package") @NotNull private PackageFacet packages;

	@Override public PackageType getType() {
		return packages.getType();
	}


	static class PointerInjector extends AbstractDriveItem.PointerInjector<DefaultPackageItem> {}
}
