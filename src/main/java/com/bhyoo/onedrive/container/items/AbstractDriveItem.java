package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.facet.FileSystemInfoFacet;
import com.bhyoo.onedrive.container.facet.SearchResultFacet;
import com.bhyoo.onedrive.container.facet.SharePointIdsFacet;
import com.bhyoo.onedrive.container.facet.SharedFacet;
import com.bhyoo.onedrive.container.items.pointer.BasePointer;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.net.URI;

import static lombok.AccessLevel.PROTECTED;

// TODO: Enhance javadoc
// TODO: Rework Item concept with multiple inheritance via interface for RemoteFileItem and RemoteFolderItem

/**
 * https://dev.onedrive.com/resources/item.htm
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@EqualsAndHashCode(of = "parentReference", callSuper = true)
@JsonDeserialize(using = AbstractDriveItem.ItemDeserializer.class)
abstract public class AbstractDriveItem extends AbstractBaseItem implements DriveItem {
	@NotNull private static final IllegalArgumentException ILLEGAL_REFERENCE =
			new IllegalArgumentException("Can not address destination folder. `folder`'s id and path are both null");

	@JacksonInject("OneDriveClient") @JsonIgnore @NotNull protected Client client;


	// TODO: missing attributes : publication, sharepointIds, activities, content, permissions, thumbnails,


	/**
	 * The {@code cTag} value is modified when content or metadata of any descendant of the folder is changed.
	 */
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) @JsonProperty("cTag") @NotNull protected String cTag;

	// TODO: custom class for this variable
	@Setter(PROTECTED) @JsonProperty protected ObjectNode deleted;
	/**
	 * The {@code eTag} value is only modified when the folder's properties are changed, except for properties that are
	 * derived from descendants (like {@code childCount} or {@code lastModifiedDateTime}).
	 */
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) protected FileSystemInfoFacet fileSystemInfo;
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) @NotNull protected ItemReference parentReference;
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) @Nullable protected SearchResultFacet searchResult;
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) @Nullable protected SharedFacet shared;
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) @Nullable protected SharePointIdsFacet sharePointIds;
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) protected long size;
	@Getter(onMethod = @__(@Override)) @Setter(PROTECTED) protected URI webDavUrl;

	@Getter(onMethod = @__(@Override)) @JsonIgnore @Nullable protected PathPointer pathPointer;
	@Getter(onMethod = @__(@Override)) @JsonIgnore @NotNull protected IdPointer idPointer;

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
		ResponseFuture responseFuture = client.requestTool().patchMetadataAsync(Client.ITEM_ID_PREFIX + id, content);
		responseFuture.syncUninterruptibly();

		AbstractDriveItem newItem = client.requestTool()
				.parseAndHandle(responseFuture.response(),
						responseFuture.getNow(),
						HttpURLConnection.HTTP_OK,
						AbstractDriveItem.class);
		this.refreshBy(newItem);
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
		return this.copyTo(folder.getId());
	}

	@NotNull
	public String copyTo(@NotNull FolderItem folder, @NotNull String newName) throws ErrorResponseException {
		return this.copyTo(folder.getId(), newName);
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
		moveTo(folder.getId());
	}

	public void moveTo(@NotNull ItemReference reference) throws ErrorResponseException {
		if (reference.id != null) moveTo(reference.id);
		else if (reference.pathPointer != null) moveTo(reference.pathPointer);
		else throw ILLEGAL_REFERENCE;
	}

	public void moveTo(@NotNull String id) throws ErrorResponseException {
		AbstractDriveItem item = (AbstractDriveItem) client.moveItem(this.id, id);
		this.refreshBy(item);
	}

	public void moveTo(@NotNull BasePointer pointer) throws ErrorResponseException {
		AbstractDriveItem item = (AbstractDriveItem) client.moveItem(this.idPointer, pointer);
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
				ret = codec.convertValue(node, DefaultFileItem.class);
			}
			// or folder?
			else if (node.has("folder")) {
				if (node.has("file") || node.has("package") || node.has("remoteItem")) {
					isMultipleType = true;
				}
				ret = codec.convertValue(node, DefaultFolderItem.class);
			}
			// or package?
			else if (node.has("package")) {
				if (node.has("folder") || node.has("file") || node.has("remoteItem")) {
					isMultipleType = true;
				}
				ret = codec.convertValue(node, DefaultPackageItem.class);
			}
			// or remote item?
			else if (node.has("remoteItem")) {
				if (node.has("folder") || node.has("file") || node.has("package")) {
					isMultipleType = true;
				}

				JsonNode remoteItem = node.get("remoteItem");
				if (remoteItem.has("folder")) {
					ret = codec.convertValue(node, RemoteFolderItem.class);
				}
				else if (remoteItem.has("file")) {
					ret = codec.convertValue(node, RemoteFileItem.class);
				}
				else {
					throw new InvalidJsonException(
							"Json is remote type but remote item doesn't have any type" +
									" (file or folder or package etc.). please contact author",
							HttpURLConnection.HTTP_OK,
							codec.writeValueAsBytes(node));
				}
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


	static protected class PointerInjector<T extends AbstractDriveItem> extends StdConverter<T, T> {
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