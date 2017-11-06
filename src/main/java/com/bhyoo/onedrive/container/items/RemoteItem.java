package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = RemoteItem.class, converter = RemoteItem.PointerInjector.class)
public class RemoteItem extends BaseItem {
	@Getter @Setter(PRIVATE) @NotNull protected RemoteItemFacet remoteItem;

	@NotNull
	@JsonIgnore
	public String getRemoteDriveID() {
		return remoteItem.getParentReference().driveId;
	}

	@NotNull
	@JsonIgnore
	public String getRemoteID() {
		return remoteItem.getId();
	}

	@NotNull
	@JsonIgnore
	public BaseItem fetchRemoteItem() throws ErrorResponseException {
		return client.requestTool().getItem("/drives/" + getRemoteDriveID() + "/items/" + getRemoteID());
	}


	static class PointerInjector extends BaseItem.PointerInjector<RemoteItem> {
		@Override public RemoteItem convert(RemoteItem value) {
			if (value.parentReference.pathPointer != null) {
				value.pathPointer = value.parentReference.pathPointer.resolve(value.name);
			}
			value.idPointer = new IdPointer(value.id, value.parentReference.driveId);

			return value;
		}
	}
}
