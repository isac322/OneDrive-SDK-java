package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.container.AsyncJobMonitor;
import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.facet.*;
import com.bhyoo.onedrive.container.items.pointer.BasePointer;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * https://dev.onedrive.com/resources/item.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = "parentReference", callSuper = true)
abstract public class AbstractDriveItem extends AbstractBaseItem implements DriveItem {
	private static final @NotNull IllegalArgumentException ILLEGAL_REFERENCE =
			new IllegalArgumentException("Can not address destination folder. `folder`'s id and path are both null");

	protected @NotNull final Client client;


	// TODO: missing attributes : publication, sharepointIds, activities, content, permissions, thumbnails,


	/**
	 * The {@code cTag} value is modified when content or metadata of any descendant of the folder is changed.
	 */
	@Getter(onMethod = @__(@Override)) protected @Nullable String cTag;

	protected @Nullable String deleted;
	/**
	 * The {@code eTag} value is only modified when the folder's properties are changed, except for properties that are
	 * derived from descendants (like {@code childCount} or {@code lastModifiedDateTime}).
	 */
	@Getter(onMethod = @__(@Override)) protected @Nullable FileSystemInfoFacet fileSystemInfo;
	@Getter(onMethod = @__(@Override)) protected @NotNull ItemReference parentReference;
	@Getter(onMethod = @__(@Override)) protected @Nullable SearchResultFacet searchResult;
	@Getter(onMethod = @__(@Override)) protected @Nullable SharedFacet shared;
	@Getter(onMethod = @__(@Override)) protected @Nullable SharePointIdsFacet sharePointIds;
	@Getter(onMethod = @__(@Override)) protected @Nullable Long size;
	@Getter(onMethod = @__(@Override)) protected @Nullable URI webDavUrl;

	@Getter(onMethod = @__(@Override)) protected @Nullable PathPointer pathPointer;
	@Getter(onMethod = @__(@Override)) protected @NotNull IdPointer idPointer;


	AbstractDriveItem(@NotNull String id, @Nullable IdentitySet creator, @NotNull String createdDateTime,
					  @Nullable String description, @Nullable String eTag, @Nullable IdentitySet lastModifier,
					  @NotNull String lastModifiedDateTime, @NotNull String name, @NotNull URI webUrl,
					  @NotNull Client client, @Nullable String cTag, @Nullable String deleted,
					  @Nullable FileSystemInfoFacet fileSystemInfo, @NotNull ItemReference parentReference,
					  @Nullable SearchResultFacet searchResult, @Nullable SharedFacet shared,
					  @Nullable SharePointIdsFacet sharePointIds, @Nullable Long size, @Nullable URI webDavUrl) {
		super(id, creator, createdDateTime, description, eTag, lastModifier, lastModifiedDateTime, name, webUrl);

		this.client = client;
		this.cTag = cTag;
		this.deleted = deleted;
		this.fileSystemInfo = fileSystemInfo;
		this.parentReference = parentReference;
		this.searchResult = searchResult;
		this.shared = shared;
		this.sharePointIds = sharePointIds;
		this.size = size;
		this.webDavUrl = webDavUrl;
	}

	@SneakyThrows(URISyntaxException.class)
	public static @NotNull AbstractDriveItem deserialize(@NotNull Client client, @NotNull JsonParser parser,
														 boolean autoClose) throws IOException {
		// BaseItem
		@Nullable String id = null;
		@Nullable IdentitySet creator = null;
		@Nullable String createdDateTime = null;
		@Nullable String description = null;
		@Nullable String eTag = null;
		@Nullable IdentitySet lastModifier = null;
		@Nullable String lastModifiedDateTime = null;
		@Nullable String name = null;
		@Nullable URI webUrl = null;

		// DriveItem
		@Nullable String cTag = null;
		@Nullable String deleted = null;
		@Nullable FileSystemInfoFacet fileSystemInfo = null;
		@Nullable ItemReference parentReference = null;
		@Nullable SearchResultFacet searchResult = null;
		@Nullable SharedFacet shared = null;
		@Nullable SharePointIdsFacet sharePointIds = null;
		@Nullable Long size = null;
		@Nullable URI webDavUrl = null;

		// FileItem
		@Nullable AudioFacet audio = null;
		@Nullable FileFacet file = null;
		@Nullable ImageFacet image = null;
		@Nullable LocationFacet location = null;
		@Nullable PhotoFacet photo = null;
		@Nullable VideoFacet video = null;

		// FolderItem
		@Nullable FolderFacet folder = null;
		@Nullable SpecialFolderFacet specialFolder = null;
		boolean root = false;
		@Nullable URI nextLink = null;
		@Nullable AbstractDriveItem[] children = null;

		// PackageItem
		@Nullable PackageFacet packages = null;

		// RemoteItem
		@Nullable RemoteItemFacet remoteItem = null;


		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "id":
					id = parser.getText();
					break;
				case "createdBy":
					creator = IdentitySet.deserialize(parser);
					break;
				case "createdDateTime":
					createdDateTime = parser.getText();
					break;
				case "description":
					description = parser.getText();
					break;
				case "eTag":
					eTag = parser.getText();
					break;
				case "lastModifiedBy":
					lastModifier = IdentitySet.deserialize(parser);
					break;
				case "lastModifiedDateTime":
					lastModifiedDateTime = parser.getText();
					break;
				case "name":
					name = parser.getText();
					break;
				case "webUrl":
					webUrl = new URI(parser.getText());
					break;

				case "cTag":
					cTag = parser.getText();
					break;
				case "deleted":
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						String fieldName = parser.currentName();
						parser.nextToken();

						switch (fieldName) {
							case "state":
								deleted = parser.getText();
								break;
							default:
								throw new IllegalStateException(
										"Unknown attribute detected in AbstractDriveItem : " + fieldName);
						}
					}
					break;
				case "fileSystemInfo":
					fileSystemInfo = FileSystemInfoFacet.deserialize(parser);
					break;
				case "parentReference":
					parentReference = ItemReference.deserialize(parser);
					break;
				case "searchResult":
					searchResult = SearchResultFacet.deserialize(parser);
					break;
				case "shared":
					shared = SharedFacet.deserialize(parser);
					break;
				case "sharepointIds":
					sharePointIds = SharePointIdsFacet.deserialize(parser);
					break;
				case "size":
					size = parser.getLongValue();
					break;
				case "webDavUrl":
					webDavUrl = new URI(parser.getText());
					break;

				case "audio":
					audio = AudioFacet.deserialize(parser);
					break;
				case "file":
					file = FileFacet.deserialize(parser);
					break;
				case "image":
					image = ImageFacet.deserialize(parser);
					break;
				case "location":
					location = LocationFacet.deserialize(parser);
					break;
				case "photo":
					photo = PhotoFacet.deserialize(parser);
					break;
				case "video":
					video = VideoFacet.deserialize(parser);
					break;

				case "folder":
					folder = FolderFacet.deserialize(parser);
					break;
				case "specialFolder":
					specialFolder = SpecialFolderFacet.deserialize(parser);
					break;
				case "root":
					root = true;
					if (parser.nextToken() != JsonToken.END_OBJECT) {
						throw new IllegalStateException(
								"Unknown attribute detected in AbstractDriveItem : " + parser.getText());
					}
					break;
				case "children@odata.nextLink":
					nextLink = new URI(parser.getText());
					break;
				case "children":
					ArrayList<AbstractDriveItem> driveItems = new ArrayList<>();
					while (parser.nextToken() != JsonToken.END_ARRAY) {
						driveItems.add(deserialize(client, parser, false));
					}
					children = driveItems.toArray(new AbstractDriveItem[0]);
					break;

				case "package":
					packages = PackageFacet.deserialize(parser);
					break;

				case "remoteItem":
					remoteItem = RemoteItemFacet.deserialize(parser);
					break;

				case "@odata.context":
					// TODO
					break;
				case "children@odata.context":
					// TODO
					break;
				case "@microsoft.graph.downloadUrl":
					// TODO
					break;
				case "@odata.type":
					// TODO
					break;

				default:
					throw new IllegalStateException(
							"Unknown attribute detected in AbstractDriveItem : " + currentName);
			}
		}

		if (autoClose) parser.close();

		// BaseItem
		assert id != null : "id is null";
		assert creator != null : "creator is null";
		assert createdDateTime != null : "createdDateTime is null";
		assert eTag != null : "eTag is null";
		assert lastModifier != null : "lastModifier is null";
		assert lastModifiedDateTime != null : "lastModifiedDateTime is null";
		assert name != null : "name is null";
		assert webUrl != null : "webUrl is null";

		// DriveItem
		assert cTag != null : "cTag is null";
		assert parentReference != null : "parentReference is null";

		if (file != null) {
			if (remoteItem != null) {
				System.out.println("RemoteFileItem " + id);
				return new RemoteFileItem(id, createdDateTime, description, eTag, lastModifiedDateTime, name, webUrl,
						client, cTag, deleted, parentReference, searchResult, shared, sharePointIds, webDavUrl,
						remoteItem);
			}
			else if (folder != null || packages != null) {
				throw new IllegalStateException("FileItem cannot have multiple type");
			}
			else {
				return new DefaultFileItem(id, creator, createdDateTime, description, eTag, lastModifier,
						lastModifiedDateTime, name, webUrl, client, cTag, deleted, fileSystemInfo, parentReference,
						searchResult, shared, sharePointIds, size, webDavUrl, audio, file, image, location, photo,
						video);
			}
		}
		else if (folder != null) {
			if (remoteItem != null) {
				System.out.println("remoteFolderItem " + id);
				return new RemoteFolderItem(id, createdDateTime, description, eTag, lastModifiedDateTime, name, webUrl,
						client, cTag, deleted, parentReference, searchResult, shared, sharePointIds, webDavUrl,
						remoteItem);
			}
			else if (file != null || packages != null) {
				throw new IllegalStateException("FolderItem cannot have multiple type");
			}
			else {
				return new DefaultFolderItem(id, creator, createdDateTime, description, eTag, lastModifier,
						lastModifiedDateTime, name, webUrl, client, cTag, deleted, fileSystemInfo, parentReference,
						searchResult, shared, sharePointIds, size, webDavUrl, folder, specialFolder, root, nextLink,
						children);
			}
		}
		else if (packages != null) {
			if (remoteItem != null) {
				throw new UnsupportedOperationException("RemotePackageItem isn't yet supported");
			}
			else if (file != null || folder != null) {
				throw new IllegalStateException("PackageItem cannot have multiple type");
			}
			else {
				return new DefaultPackageItem(id, creator, createdDateTime, description, eTag, lastModifier,
						lastModifiedDateTime, name, webUrl, client, cTag, deleted, fileSystemInfo, parentReference,
						searchResult, shared, sharePointIds, size, webDavUrl, packages);
			}
		}
		else if (remoteItem != null) {
			if (remoteItem.getFile() != null) {
				if (remoteItem.getFolder() != null || remoteItem.getPackages() != null) {
					throw new IllegalStateException("FileItem cannot have multiple type");
				}
				else {
					return new RemoteFileItem(id, createdDateTime, description, eTag, lastModifiedDateTime, name,
							webUrl, client, cTag, deleted, parentReference, searchResult, shared, sharePointIds,
							webDavUrl, remoteItem);
				}
			}
			else if (remoteItem.getFolder() != null) {
				if (remoteItem.getFile() != null || remoteItem.getPackages() != null) {
					throw new IllegalStateException("FolderItem cannot have multiple type");
				}
				else {
					return new RemoteFolderItem(id, createdDateTime, description, eTag, lastModifiedDateTime, name,
							webUrl, client, cTag, deleted, parentReference, searchResult, shared, sharePointIds,
							webDavUrl, remoteItem);
				}
			}
			else if (remoteItem.getPackages() != null) {
				throw new UnsupportedOperationException("RemotePackageItem isn't yet supported");
			}
			else {
				throw new UnsupportedOperationException("Unsupported type of item. contact author");
			}
		}
		else {
			throw new UnsupportedOperationException("Unsupported type of item. contact author");
		}
	}

	protected void createPointers() {
		assert parentReference.pathPointer != null : "`parentReference.pathPointer` is null on FileItem";
		assert parentReference.rawPath != null : "`parentReference.rawPath` is null on FileItem";

		assert name != null : "name is null";
		pathPointer = parentReference.pathPointer.resolve(name);
		idPointer = new IdPointer(id, parentReference.driveId);
	}

	@Override
	public String toString() {
		return '<' + name + " (" + id + "), " + pathPointer + '>';
	}


	// TODO: is there any way to merge with constructor? cause both are almost same
	protected void refreshBy(@NotNull AbstractDriveItem newItem) {
		this.id = newItem.id;
		this.creator = newItem.creator;
		this.createdDateTime = newItem.createdDateTime;
		this.cTag = newItem.cTag;
		this.deleted = newItem.deleted;
		this.description = newItem.description;
		this.eTag = newItem.eTag;
		this.fileSystemInfo = newItem.fileSystemInfo;
		this.lastModifier = newItem.lastModifier;
		this.lastModifiedDateTime = newItem.lastModifiedDateTime;
		this.name = newItem.name;
		this.parentReference = newItem.parentReference;
		this.searchResult = newItem.searchResult;
		this.shared = newItem.shared;
		this.sharePointIds = newItem.sharePointIds;
		this.size = newItem.size;
		this.webDavUrl = newItem.webDavUrl;
		this.webUrl = newItem.webUrl;

		if (parentReference.pathPointer != null && parentReference.rawPath != null) {
			assert name != null : "name is null";
			this.pathPointer = parentReference.pathPointer.resolve(name);
		}
		this.idPointer = new IdPointer(id, parentReference.driveId);
	}

	/**
	 * This method refresh content even if you doesn't have changes.<br>
	 * <br>
	 * Note that when refresh content, <b>it can contains difference that you didn't modify</b>. because other side
	 * (it could be other App, other process, etc.) can modify content after you fetched.
	 *
	 * @throws ErrorResponseException if error happens while requesting copying operation. such as invalid login info
	 */
	@Override
	public void refresh() throws ErrorResponseException {
		this.update("{}".getBytes());
	}

	private void update(byte[] content) throws ErrorResponseException {
		RequestTool requestTool = client.requestTool();

		// using async way, because some JDK's HttpConnection doesn't allow PATCH
		ResponseFuture responseFuture = requestTool
				.patchMetadataAsync(Client.ITEM_ID_PREFIX + id, content)
				.syncUninterruptibly();

		AbstractDriveItem newItem = (AbstractDriveItem) requestTool
				.parseDriveItemAndHandle(responseFuture.response(),
						responseFuture.getNow(),
						HttpURLConnection.HTTP_OK);

		this.refreshBy(newItem);
	}




	/*
	 *************************************************************
	 *
	 * Deleting
	 *
	 * *************************************************************
	 */


	@Override
	public void delete() throws ErrorResponseException {
		client.deleteItem(this.id);
	}




	/*
	 *************************************************************
	 *
	 * Coping
	 *
	 * *************************************************************
	 */


	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull FolderItem folder) throws ErrorResponseException {
		return this.copyTo(folder.getId());
	}

	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull FolderItem folder, @NotNull String newName)
			throws ErrorResponseException {
		return this.copyTo(folder.getId(), newName);
	}

	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull ItemReference folder) throws ErrorResponseException {
		if (folder.id != null)
			return this.copyTo(folder.id);
		else if (folder.pathPointer != null)
			return this.copyTo(folder.pathPointer);
		else
			throw ILLEGAL_REFERENCE;
	}

	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull ItemReference folder, @NotNull String newName)
			throws ErrorResponseException {
		if (folder.id != null)
			return this.copyTo(folder.id, newName);
		else if (folder.pathPointer != null)
			return this.copyTo(folder.pathPointer, newName);
		else
			throw ILLEGAL_REFERENCE;
	}

	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull BasePointer dest) throws ErrorResponseException {
		return client.copyItem(idPointer, dest);
	}

	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull BasePointer dest, @NotNull String newName)
			throws ErrorResponseException {
		return client.copyItem(idPointer, dest, newName);
	}

	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull String destId) throws ErrorResponseException {
		return client.copyItem(this.id, destId);
	}

	@Override
	public @NotNull AsyncJobMonitor copyTo(@NotNull String destId, @NotNull String newName)
			throws ErrorResponseException {
		return client.copyItem(this.id, destId, newName);
	}




	/*
	 *************************************************************
	 *
	 * Moving
	 *
	 *************************************************************
	 */


	@Override
	public void moveTo(@NotNull FolderItem folder) throws ErrorResponseException {
		moveTo(folder.getId());
	}

	@Override
	public void moveTo(@NotNull ItemReference reference) throws ErrorResponseException {
		if (reference.id != null) moveTo(reference.id);
		else if (reference.pathPointer != null) moveTo(reference.pathPointer);
		else throw ILLEGAL_REFERENCE;
	}

	@Override
	public void moveTo(@NotNull String id) throws ErrorResponseException {
		AbstractDriveItem item = (AbstractDriveItem) client.moveItem(this.id, id);
		this.refreshBy(item);
	}

	@Override
	public void moveTo(@NotNull BasePointer pointer) throws ErrorResponseException {
		AbstractDriveItem item = (AbstractDriveItem) client.moveItem(this.idPointer, pointer);
		this.refreshBy(item);
	}


	@Override
	public final @NotNull ItemReference newReference() {
		return new ItemReference(getDriveId(), parentReference.driveType, id, pathPointer);
	}




	/*
	 *************************************************************
	 *
	 * Custom Getter
	 *
	 *************************************************************
	 */


	@Override
	public final @NotNull String getDriveId() {return parentReference.driveId;}




	/*
	 *************************************************************
	 *
	 * Custom Setter
	 *
	 *************************************************************
	 */


	public void updateDescription(String description) throws ErrorResponseException {
		update(("{\"description\":\"" + description + "\"}").getBytes());
	}

	// FIXME: make async version
	public void rename(@NotNull String name) throws ErrorResponseException {
		update(("{\"name\":\"" + name + "\"}").getBytes());
	}
}