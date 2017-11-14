package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

// TODO: make AbstractRemoteItem and inherit it

@JsonDeserialize(as = RemoteFolderItem.class, converter = RemoteFolderItem.PointerInjector.class)
public class RemoteFolderItem extends DefaultFolderItem implements RemoteItem {
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) @NotNull protected RemoteItemFacet remoteItem;
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) @NotNull protected IdPointer remotePointer;

	public @NotNull String getRemoteDriveID() {
		return remoteItem.getParentReference().driveId;
	}

	public @NotNull String getRemoteID() {
		return remoteItem.getId();
	}

	@Override public @NotNull FolderItem fetchRemoteItem() throws ErrorResponseException {
		return (FolderItem) client.getItem(new IdPointer(remoteItem.getId(), remoteItem.getParentReference().driveId));
	}


	static class PointerInjector extends AbstractDriveItem.PointerInjector<RemoteFolderItem> {
		@Override public RemoteFolderItem convert(RemoteFolderItem value) {
			if (value.parentReference.pathPointer != null) {
				value.pathPointer = value.parentReference.pathPointer.resolve(value.name);
			}
			value.idPointer = new IdPointer(value.id, value.parentReference.driveId);
			value.remotePointer =
					new IdPointer(value.remoteItem.getId(), value.remoteItem.getParentReference().driveId);

			return value;
		}
	}
}
