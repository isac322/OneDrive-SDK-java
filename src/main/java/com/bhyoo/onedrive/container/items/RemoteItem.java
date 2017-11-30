package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;

public interface RemoteItem extends DriveItem {
	@NotNull String getRemoteDriveID();

	@NotNull String getRemoteID();

	@NotNull IdPointer getRemotePointer();

	@NotNull DriveItem fetchRemoteItem() throws ErrorResponseException;

	@NotNull RemoteItemFacet getRemoteItem();
}
