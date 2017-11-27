package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.facet.*;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.container.items.pointer.Operator;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.bhyoo.onedrive.container.pager.DriveItemPager.DriveItemPage;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InternalException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static java.net.HttpURLConnection.HTTP_OK;

public class DefaultFolderItem extends AbstractDriveItem implements FolderItem {
	protected @NotNull FolderFacet folder;
	protected @Nullable SpecialFolderFacet specialFolder;
	protected @Nullable ObjectNode root;
	protected @Nullable URI nextLink;
	protected @Nullable AbstractDriveItem[] children;
	protected List<FolderItem> folderChildren;
	protected List<FileItem> fileChildren;
	protected List<DriveItem> allChildren;

	DefaultFolderItem(@NotNull String id, @Nullable IdentitySet creator, @NotNull String createdDateTime,
					  @Nullable String description, @Nullable String eTag, @Nullable IdentitySet lastModifier,
					  @NotNull String lastModifiedDateTime, @NotNull String name, @NotNull URI webUrl,
					  @NotNull Client client, @Nullable String cTag, @Nullable ObjectNode deleted,
					  FileSystemInfoFacet fileSystemInfo, @NotNull ItemReference parentReference,
					  @Nullable SearchResultFacet searchResult, @Nullable SharedFacet shared,
					  @Nullable SharePointIdsFacet sharePointIds, @NotNull Long size, URI webDavUrl,
					  @NotNull FolderFacet folder, @Nullable SpecialFolderFacet specialFolder,
					  @Nullable ObjectNode root, @Nullable URI nextLink, @Nullable AbstractDriveItem[] children) {
		super(id, creator, createdDateTime, description, eTag, lastModifier, lastModifiedDateTime, name, webUrl,
				client, cTag, deleted, fileSystemInfo, parentReference, searchResult, shared, sharePointIds, size,
				webDavUrl);
		this.folder = folder;
		this.specialFolder = specialFolder;
		this.root = root;
		this.nextLink = nextLink;
		this.children = children;

/* currently do not use this code because of onedrive's bug (https://github.com/OneDrive/onedrive-api-docs/issues/732)
		if (children != null || folder.getChildCount() == 0) {
			folderChildren = new ArrayList<>();
			fileChildren = new ArrayList<>();
			allChildren = new ArrayList<>();
		}

		if (children != null) {
			addChildren(children);
			parseChildren(nextLink);
		}
*/

		createPointers();

		if (children != null) {
			try {
				fetchChildren();
			}
			catch (ErrorResponseException e) {
				// FIXME: handle it!
				e.printStackTrace();
			}
		}
		else if (folder.getChildCount() == 0) {
			folderChildren = new ArrayList<>();
			fileChildren = new ArrayList<>();
			allChildren = new ArrayList<>();
		}
	}

	@Override
	protected void createPointers() {
		if (isRoot()) {
			pathPointer = new PathPointer("/", getDriveId());
		}
		else {
			assert parentReference.pathPointer != null : "`parentReference.pathPointer` is null on FolderItem";
			assert parentReference.rawPath != null : "`parentReference.rawPath` is null on FolderItem";
			pathPointer = parentReference.pathPointer.resolve(name);
		}
		idPointer = new IdPointer(id, getDriveId());
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
				assert item instanceof PackageItem || item instanceof RemoteItem : "Wrong item type";
			}
			allChildren.add(item);
		}
	}

	protected void parseChildren(@Nullable URI nextLink) {
		while (nextLink != null) {
			ResponseFuture responseFuture = client.requestTool()
					.doAsync(GET, nextLink)
					.syncUninterruptibly();

			try {
				DriveItemPage itemPage = client.requestTool()
						.parseDriveItemPageAndHandle(responseFuture.response(), responseFuture.getNow(), HTTP_OK);

				nextLink = itemPage.getNextLink();
				addChildren(itemPage.getValue());
			}
			catch (ErrorResponseException e) {
				// FIXME: handle it!
				e.printStackTrace();
			}
		}
	}


	@Override
	public void fetchChildren() throws ErrorResponseException {
		allChildren = new ArrayList<>();
		folderChildren = new ArrayList<>();
		fileChildren = new ArrayList<>();

		ResponseFuture responseFuture = client.requestTool()
				.doAsync(GET, idPointer.resolveOperator(Operator.CHILDREN))
				.syncUninterruptibly();

		addChildren(client.requestTool()
				.parseDriveItemRecursiveAndHandle(responseFuture.response(), responseFuture.getNow(), HTTP_OK));
	}


	// TODO: Implement '@name.conflictBehavior'

	@Override
	public @NotNull FolderItem createFolder(@NotNull String name) throws ErrorResponseException {
		return client.createFolder(this.id, name);
	}


	/**
	 * Update data itself with {@code newItem}.<br>
	 * This will <b>set all children {@code List} null</b>. so very next call that related with children may take a
	 * while to
	 * fetch children data.
	 *
	 * @param newItem New object that contains new data to update.
	 *
	 * @throws IllegalArgumentException It's because of construction of {@link IdPointer} or {@link PathPointer}.
	 * @throws InternalException        if parameter {@code parentReference} is null even if this isn't root directory.
	 */
	@Override
	protected void refreshBy(@NotNull AbstractDriveItem newItem) {
		super.refreshBy(newItem);

		DefaultFolderItem item = (DefaultFolderItem) newItem;

		this.folder = item.folder;
		this.specialFolder = item.specialFolder;
		this.root = item.root;

		folderChildren = null;
		fileChildren = null;
		allChildren = null;

		if (isRoot()) {
			pathPointer = new PathPointer("/", getDriveId());
		}
	}


	/*
	*************************************************************
	*
	* Custom Getter
	*
	*************************************************************
	 */


	@Override
	public boolean isRoot() {return root != null;}

	@Override
	public boolean isChildrenFetched() {
		return folder.getChildCount() == 0 || allChildren != null && folderChildren != null && fileChildren != null;
	}

	@Override
	public boolean isSpecial() {return specialFolder != null;}

	@Override
	public long childCount() {return folder.getChildCount();}


	@Override
	public final @NotNull DriveItem[] allChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return allChildren.toArray(new DriveItem[0]);
	}

	@Override
	public @NotNull FolderItem[] folderChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return folderChildren.toArray(new FolderItem[0]);
	}

	@Override
	public @NotNull FileItem[] fileChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return fileChildren.toArray(new FileItem[0]);
	}


	/*
	*************************************************************
	*
	* Custom Iterator
	*
	*************************************************************
	 */


	@Override
	public @NotNull Iterator<DriveItem> iterator() {
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
