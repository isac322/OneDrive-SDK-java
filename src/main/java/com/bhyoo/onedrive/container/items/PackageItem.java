package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.PackageType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PackageItem extends DriveItem {
	PackageType getType();
}
