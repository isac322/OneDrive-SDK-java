package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.PackageType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PackageItem extends DriveItem {
	// TODO: check don't need `@JsonProperty("package")` for serialize
	@JsonIgnore PackageType getType();
}
