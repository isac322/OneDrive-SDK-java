package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.facet.RemoteItemFacet;
import com.bhyoo.onedrive.container.facet.SearchResultFacet;
import com.bhyoo.onedrive.container.facet.SharePointIdsFacet;
import com.bhyoo.onedrive.container.facet.SharedFacet;
import com.bhyoo.onedrive.container.items.pointer.Operator;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;


public class RemoteFolderItem extends AbstractRemoteItem implements FolderItem {
	protected List<FolderItem> folderChildren;
	protected List<FileItem> fileChildren;
	protected List<DriveItem> allChildren;

	RemoteFolderItem(@NotNull String id, @NotNull String createdDateTime, @Nullable String description,
					 @NotNull String eTag, @NotNull String lastModifiedDateTime, @NotNull String name,
					 @NotNull URI webUrl, @NotNull Client client, @NotNull String cTag, @Nullable ObjectNode deleted,
					 @NotNull ItemReference parentReference, @Nullable SearchResultFacet searchResult,
					 @Nullable SharedFacet shared, @Nullable SharePointIdsFacet sharePointIds, URI webDavUrl,
					 @NotNull RemoteItemFacet remoteItem) {
		super(id, createdDateTime, description, eTag, lastModifiedDateTime, name, webUrl, client, cTag, deleted,
				parentReference, searchResult, shared, sharePointIds, webDavUrl, remoteItem);
	}

	@Override
	public @NotNull FolderItem createFolder(@NotNull String name) throws ErrorResponseException {
		return client.createFolder(remotePointer, name);
	}

	@Override public boolean isRoot() {return false;}

	@Override public boolean isChildrenFetched() {
		assert remoteItem.getFolder() != null;
		return remoteItem.getFolder().getChildCount() == 0
				|| allChildren != null && folderChildren != null && fileChildren != null;
	}

	protected void addChildren(@NotNull DriveItem[] array) {
		for (DriveItem item : array) {
			if (item instanceof FolderItem) {
				folderChildren.add((FolderItem) item);
			}
			else if (item instanceof FileItem) {
				fileChildren.add((FileItem) item);
			}
			else {
				// if child is neither FolderItem nor FileItem nor PackageItem.
				assert item instanceof PackageItem : "Wrong item type";
			}
			allChildren.add(item);
		}
	}

	@Override public void fetchChildren() throws ErrorResponseException {
		allChildren = new ArrayList<>();
		folderChildren = new ArrayList<>();
		fileChildren = new ArrayList<>();

		ResponseFuture responseFuture = client.requestTool()
				.doAsync(HttpMethod.GET, remotePointer.resolveOperator(Operator.CHILDREN))
				.syncUninterruptibly();

		addChildren(client.requestTool()
				.parseDriveItemRecursiveAndHandle(responseFuture.response(), responseFuture.getNow(), HTTP_OK));
	}

	@Override public boolean isSpecial() {return false;}

	@Override public long childCount() {
		assert remoteItem.getFolder() != null;
		return remoteItem.getFolder().getChildCount();
	}

	@Override public @NotNull DriveItem[] allChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return allChildren.toArray(new DriveItem[0]);
	}

	@Override public @NotNull FolderItem[] folderChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return folderChildren.toArray(new FolderItem[0]);
	}

	@Override public @NotNull FileItem[] fileChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return fileChildren.toArray(new FileItem[0]);
	}

	@Override public @NotNull Iterator<DriveItem> iterator() {
		try {
			if (!isChildrenFetched()) fetchChildren();
			return allChildren.iterator();
		}
		catch (ErrorResponseException e) {
			// FIXME: custom exception
			throw new RuntimeException(e);
		}
	}
}
