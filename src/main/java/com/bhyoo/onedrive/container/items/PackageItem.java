package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.PackageType;

public interface PackageItem extends DriveItem {
	PackageType getType();
}
