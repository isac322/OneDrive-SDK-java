package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc
// TODO: make AbstractRemoteItem and inherit it

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = RemoteFileItem.class, converter = RemoteFileItem.PointerInjector.class)
public class RemoteFileItem extends DefaultFileItem implements RemoteItem {
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) @NotNull protected RemoteItemFacet remoteItem;
	@Getter(onMethod = @__(@Override)) @Setter(PRIVATE) @NotNull protected IdPointer remotePointer;

	public @NotNull String getRemoteDriveID() {
		return remoteItem.getParentReference().driveId;
	}

	public @NotNull String getRemoteID() {
		return remoteItem.getId();
	}


	@Override public @NotNull FileItem fetchRemoteItem() throws ErrorResponseException {
		return (FileItem) client.getItem(remotePointer);
	}


	static class PointerInjector extends AbstractDriveItem.PointerInjector<RemoteFileItem> {
		@Override public RemoteFileItem convert(RemoteFileItem value) {
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
