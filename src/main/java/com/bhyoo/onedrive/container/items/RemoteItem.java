package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

public interface RemoteItem extends DriveItem {
	@JsonIgnore @NotNull String getRemoteDriveID();

	@JsonIgnore @NotNull String getRemoteID();

	@JsonIgnore @NotNull IdPointer getRemotePointer();

	@JsonIgnore @NotNull DriveItem fetchRemoteItem() throws ErrorResponseException;

	@NotNull RemoteItemFacet getRemoteItem();
}
