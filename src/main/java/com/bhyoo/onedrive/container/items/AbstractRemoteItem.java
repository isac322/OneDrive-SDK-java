package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
import com.bhyoo.onedrive.container.facet.SearchResultFacet;
import com.bhyoo.onedrive.container.facet.SharePointIdsFacet;
import com.bhyoo.onedrive.container.facet.SharedFacet;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public abstract class AbstractRemoteItem extends AbstractDriveItem implements RemoteItem {
	@Getter(onMethod = @__(@Override)) protected @NotNull RemoteItemFacet remoteItem;
	@Getter(onMethod = @__(@Override)) protected @NotNull IdPointer remotePointer;

	AbstractRemoteItem(@NotNull String id, @NotNull String createdDateTime, @Nullable String description,
					   @NotNull String eTag, @NotNull String lastModifiedDateTime, @NotNull String name,
					   @NotNull URI webUrl, @NotNull Client client, @NotNull String cTag, @Nullable ObjectNode deleted,
					   @NotNull ItemReference parentReference, @Nullable SearchResultFacet searchResult,
					   @Nullable SharedFacet shared, @Nullable SharePointIdsFacet sharePointIds, URI webDavUrl,
					   @NotNull RemoteItemFacet remoteItem) {
		super(id, remoteItem.getCreator(), createdDateTime, description, eTag, remoteItem.getLastModifier(),
				lastModifiedDateTime, name, webUrl, client, cTag, deleted, remoteItem.getFileSystemInfo(),
				parentReference, searchResult, shared, sharePointIds, remoteItem.getSize(), webDavUrl);
		this.remoteItem = remoteItem;

		createPointers();
	}

	@Override
	protected void createPointers() {
		if (parentReference.pathPointer != null) {
			this.pathPointer = parentReference.pathPointer.resolve(name);
		}
		this.idPointer = new IdPointer(id, parentReference.driveId);
		this.remotePointer = new IdPointer(remoteItem.getId(), remoteItem.getParentReference().driveId);
	}

	public @NotNull String getRemoteDriveID() {return remoteItem.getParentReference().driveId;}

	public @NotNull String getRemoteID() {return remoteItem.getId();}


	@Override public @NotNull DriveItem fetchRemoteItem() throws ErrorResponseException {
		return client.getItem(remotePointer);
	}
}
