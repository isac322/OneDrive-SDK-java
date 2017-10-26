package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
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
@JsonDeserialize(as = RemoteFolderItem.class, converter = RemoteFolderItem.PointerInjector.class)
public class RemoteFolderItem extends FolderItem {
	@Setter(PRIVATE) @Getter @NotNull protected RemoteItemFacet remoteItem;

	@NotNull
	@JsonIgnore
	public String getRealDriveID() {
		return remoteItem.getParentReference().driveId;
	}

	@NotNull
	@JsonIgnore
	public String getRealID() {
		return remoteItem.getId();
	}

	@Override
	protected void fetchChildren() throws ErrorResponseException {
		// children of remote folder can be obtained only by real drive's id and real id.
		_fetchChildren(String.format("/drives/%s/items/%s/children", getRealDriveID(), getRealID()));
	}


	static class PointerInjector extends FolderItem.PointerInjector<RemoteFolderItem> {}
}
