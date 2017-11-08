package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.IdentitySet;
import com.bhyoo.onedrive.container.facet.FileSystemInfoFacet;
import com.bhyoo.onedrive.container.facet.SearchResultFacet;
import com.bhyoo.onedrive.container.facet.SharePointIdsFacet;
import com.bhyoo.onedrive.container.facet.SharedFacet;
import com.bhyoo.onedrive.container.items.pointer.BasePointer;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InternalException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import com.bhyoo.onedrive.network.async.ResponseFutureListener;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;

import static lombok.AccessLevel.PROTECTED;

// TODO: Enhance javadoc
// TODO: Rework Item concept with multiple inheritance via interface for RemoteFileItem and RemoteFolderItem

/**
 * https://dev.onedrive.com/resources/item.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = {"id", "parentReference"})
@JsonDeserialize(using = DriveItem.ItemDeserializer.class)
abstract public class DriveItem {
	@NotNull private static final IllegalArgumentException ILLEGAL_REFERENCE =
			new IllegalArgumentException("Can not address destination folder. `folder`'s id and path are both null");

	@JacksonInject("OneDriveClient") @JsonIgnore @NotNull protected Client client;

	@Getter @Setter(PROTECTED) @NotNull protected String id;
	@Getter @Setter(PROTECTED) protected IdentitySet createdBy;
	@Getter @Setter(PROTECTED) protected String createdDateTime;
	/**
	 * The {@code cTag} value is modified when content or metadata of any descendant of the folder is changed.
	 */
	@Getter @Setter(PROTECTED) @JsonProperty("cTag") @NotNull protected String cTag;
	@Getter @Setter(PROTECTED) protected ObjectNode deleted;
	@Getter @Setter(PROTECTED) protected String description;
	/**
	 * The {@code eTag} value is only modified when the folder's properties are changed, except for properties that are
	 * derived from descendants (like {@code childCount} or {@code lastModifiedDateTime}).
	 */
	@Getter @Setter(PROTECTED) @JsonProperty("eTag") protected String eTag;
	@Getter @Setter(PROTECTED) protected FileSystemInfoFacet fileSystemInfo;
	@Getter @Setter(PROTECTED) protected IdentitySet lastModifiedBy;
	@Getter @Setter(PROTECTED) protected String lastModifiedDateTime;
	@Setter(PROTECTED) @NotNull protected String name;
	@Getter @Setter(PROTECTED) @NotNull protected ItemReference parentReference;
	@Getter @Setter(PROTECTED) @Nullable protected SearchResultFacet searchResult;
	@Getter @Setter(PROTECTED) @Nullable protected SharedFacet shared;
	@Getter @Setter(PROTECTED) @Nullable protected SharePointIdsFacet sharePointIds;
	@Getter @Setter(PROTECTED) protected long size;
	@Getter @Setter(PROTECTED) protected String webDavUrl;
	@Getter @Setter(PROTECTED) protected String webUrl;

	@Getter @JsonIgnore @Nullable protected PathPointer pathPointer;
	@Getter @JsonIgnore @NotNull protected IdPointer idPointer;

	@NotNull
	public String getName() { return name; }

	@Override
	public String toString() {
		return '<' + name + " (" + id + "), " + pathPointer + '>';
	}


	// TODO: is there any way to merge with constructor? cause both are almost same
	protected void refreshBy(@NotNull DriveItem newItem) {
		this.id = newItem.id;
		this.createdBy = newItem.createdBy;
		this.createdDateTime = newItem.createdDateTime;
		this.cTag = newItem.cTag;
		this.deleted = newItem.deleted;
		this.description = newItem.description;
		this.eTag = newItem.eTag;
		this.fileSystemInfo = newItem.fileSystemInfo;
		this.lastModifiedBy = newItem.lastModifiedBy;
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
	public void refresh() throws ErrorResponseException {
		this.update("{}".getBytes());
	}

	private void update(byte[] content) throws ErrorResponseException {
		final DriveItem[] newItem = new DriveItem[1];
		final CountDownLatch latch = new CountDownLatch(1);
		client.requestTool().patchMetadataAsync(Client.ITEM_ID_PREFIX + id, content,
				new ResponseFutureListener() {
					@Override public void operationComplete(ResponseFuture future) throws Exception {
						newItem[0] = client.requestTool().parseAndHandle(
								future.response(),
								future.get(),
								HttpURLConnection.HTTP_OK,
								DriveItem.class);
						latch.countDown();
					}
				});
		try {
			latch.await();
		}
		catch (InterruptedException e) {
			throw new InternalException("Exception occurs while waiting lock in DriveItem#update()", e);
		}
		this.refreshBy(newItem[0]);
	}




	/*
	*************************************************************
	*
	* Deleting
	*
	* *************************************************************
	 */


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


	@NotNull
	public String copyTo(@NotNull FolderItem folder) throws ErrorResponseException {
		return this.copyTo(folder.id);
	}

	@NotNull
	public String copyTo(@NotNull FolderItem folder, @NotNull String newName) throws ErrorResponseException {
		return this.copyTo(folder.id, newName);
	}

	@NotNull
	public String copyTo(@NotNull ItemReference folder) throws ErrorResponseException {
		if (folder.id != null)
			return this.copyTo(folder.id);
		else if (folder.pathPointer != null)
			return this.copyTo(folder.pathPointer);
		else
			throw ILLEGAL_REFERENCE;
	}

	@NotNull
	public String copyTo(@NotNull ItemReference folder, @NotNull String newName) throws ErrorResponseException {
		if (folder.id != null)
			return this.copyTo(folder.id, newName);
		else if (folder.pathPointer != null)
			return this.copyTo(folder.pathPointer, newName);
		else
			throw ILLEGAL_REFERENCE;
	}

	@NotNull
	public String copyTo(@NotNull BasePointer dest) throws ErrorResponseException {
		return client.copyItem(idPointer, dest);
	}

	@NotNull
	public String copyTo(@NotNull BasePointer dest, @NotNull String newName) throws ErrorResponseException {
		return client.copyItem(idPointer, dest, newName);
	}

	@NotNull
	public String copyTo(@NotNull String destId) throws ErrorResponseException {
		return client.copyItem(this.id, destId);
	}

	@NotNull
	public String copyTo(@NotNull String destId, @NotNull String newName) throws ErrorResponseException {
		return client.copyItem(this.id, destId, newName);
	}




	/*
	*************************************************************
	*
	* Moving
	*
	*************************************************************
	 */


	public void moveTo(@NotNull FolderItem folder) throws ErrorResponseException {
		moveTo(folder.id);
	}

	public void moveTo(@NotNull ItemReference reference) throws ErrorResponseException {
		if (reference.id != null) moveTo(reference.id);
		else if (reference.pathPointer != null) moveTo(reference.pathPointer);
		else throw ILLEGAL_REFERENCE;
	}

	public void moveTo(@NotNull String id) throws ErrorResponseException {
		DriveItem item = client.moveItem(this.id, id);
		this.refreshBy(item);
	}

	public void moveTo(@NotNull BasePointer pointer) throws ErrorResponseException {
		DriveItem item = client.moveItem(this.idPointer, pointer);
		this.refreshBy(item);
	}


	@NotNull
	public final ItemReference newReference() {
		return new ItemReference(getDriveId(), id, pathPointer);
	}




	/*
	*************************************************************
	*
	* Custom Getter
	*
	*************************************************************
	 */


	@NotNull
	@JsonIgnore
	public String getDriveId() {
		return parentReference.driveId;
	}




	/*
	*************************************************************
	*
	* Custom Setter
	*
	*************************************************************
	 */


	@JsonIgnore
	public void updateDescription(String description) throws ErrorResponseException {
		update(("{\"description\":\"" + description + "\"}").getBytes());
	}

	@JsonIgnore
	public void updateName(@NotNull String name) throws ErrorResponseException {
		update(("{\"name\":\"" + name + "\"}").getBytes());
	}




	/*
	*************************************************************
	*
	* Custom Jackson Deserializer
	*
	*************************************************************
	 */


	// FIXME: https://stackoverflow.com/questions/34406808/deserializing-unwrapped-flattened-json-in-java
	public static class ItemDeserializer extends JsonDeserializer<DriveItem> {
		@Override
		public DriveItem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper codec = (ObjectMapper) parser.getCodec();
			ObjectNode node = parser.readValueAsTree();

			DriveItem ret;
			boolean isMultipleType = false;

			// is the object a file??
			if (node.has("file")) {
				if (node.has("folder") || node.has("package") || node.has("remoteItem")) {
					isMultipleType = true;
				}
				ret = codec.convertValue(node, FileItem.class);
			}
			// or folder?
			else if (node.has("folder")) {
				if (node.has("file") || node.has("package") || node.has("remoteItem")) {
					isMultipleType = true;
				}
				ret = codec.convertValue(node, FolderItem.class);
			}
			// or package?
			else if (node.has("package")) {
				if (node.has("folder") || node.has("file") || node.has("remoteItem")) {
					isMultipleType = true;
				}
				ret = codec.convertValue(node, PackageItem.class);
			}
			// or remote item?
			else if (node.has("remoteItem")) {
				if (node.has("folder") || node.has("file") || node.has("package")) {
					isMultipleType = true;
				}
				ret = codec.convertValue(node, RemoteItem.class);
			}
			// unrecognizable object!
			else {
				throw new InvalidJsonException(
						"Json doesn't have any type (file or folder or package etc.). please contact author",
						HttpURLConnection.HTTP_OK,
						codec.writeValueAsBytes(node));
			}

			if (isMultipleType)
				throw new InvalidJsonException(
						"Multiple item type is contained. please contact author.",
						HttpURLConnection.HTTP_OK,
						codec.writeValueAsBytes(node));

			else
				return ret;
		}
	}


	static protected class PointerInjector<T extends DriveItem> extends StdConverter<T, T> {
		@Override public T convert(T value) {
			// are they always have `parentReference` except root directory?
			assert value.parentReference != null : "`parentReference` is null on DriveItem";
			assert value.parentReference.pathPointer != null : "`parentReference.pathPointer` is null on FileItem";
			assert value.parentReference.rawPath != null : "`parentReference.rawPath` is null on FileItem";

			value.pathPointer = value.parentReference.pathPointer.resolve(value.name);
			value.idPointer = new IdPointer(value.id, value.parentReference.driveId);

			return value;
		}
	}
}