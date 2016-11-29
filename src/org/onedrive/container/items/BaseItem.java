package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpResponse;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.Client;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.FileSystemInfoFacet;
import org.onedrive.container.facet.SearchResultFacet;
import org.onedrive.container.facet.SharePointIdsFacet;
import org.onedrive.container.facet.SharedFacet;
import org.onedrive.container.items.pointer.BasePointer;
import org.onedrive.container.items.pointer.IdPointer;
import org.onedrive.container.items.pointer.PathPointer;
import org.onedrive.exceptions.ErrorResponseException;
import org.onedrive.exceptions.InvalidJsonException;
import org.onedrive.network.DirectByteInputStream;
import org.onedrive.network.async.AsyncRequestHandler;
import org.onedrive.network.sync.SyncRequest;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * https://dev.onedrive.com/resources/item.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(using = BaseItem.ItemDeserializer.class)
abstract public class BaseItem {
	@JsonIgnore @NotNull protected final Client client;

	@Getter @NotNull protected String id;
	@Getter protected IdentitySet createdBy;
	@Getter protected String createdDateTime;
	/**
	 * The {@code cTag} value is modified when content or metadata of any descendant of the folder is changed.
	 */
	@Getter protected String cTag;
	@Getter protected ObjectNode deleted;
	@Getter protected String description;
	/**
	 * The {@code eTag} value is only modified when the folder's properties are changed, except for properties that are
	 * derived from descendants (like {@code childCount} or {@code lastModifiedDateTime}).
	 */
	@Getter protected String eTag;
	@Getter protected FileSystemInfoFacet fileSystemInfo;
	@Getter protected IdentitySet lastModifiedBy;
	@Getter protected String lastModifiedDateTime;
	@Getter @NotNull protected String name;
	@Getter @Nullable protected ItemReference parentReference;
	@Getter @Nullable protected SearchResultFacet searchResult;
	@Getter @Nullable protected SharedFacet shared;
	@Getter @Nullable protected SharePointIdsFacet sharePointIds;
	@Getter protected long size;
	@Getter protected String webDavUrl;
	@Getter protected String webUrl;

	@Getter(onMethod = @__(@JsonIgnore)) @NotNull protected PathPointer pathPointer;
	@Getter(onMethod = @__(@JsonIgnore)) @NotNull protected IdPointer idPointer;

	/**
	 * @throws IllegalArgumentException It's solely because of construction of {@link IdPointer}.
	 * @see IdPointer#IdPointer(String, String)
	 * @see IdPointer#IdPointer(String)
	 */
	protected BaseItem(@NotNull Client client, @NotNull String id, IdentitySet createdBy, String createdDateTime,
					   String cTag, ObjectNode deleted, String description, String eTag,
					   FileSystemInfoFacet fileSystemInfo, IdentitySet lastModifiedBy, String lastModifiedDateTime,
					   @NotNull String name, @Nullable ItemReference parentReference,
					   @Nullable SearchResultFacet searchResult, @Nullable SharedFacet shared,
					   @Nullable SharePointIdsFacet sharePointIds, long size, String webDavUrl, String webUrl) {
		this.client = client;

		this.id = id;
		this.createdBy = createdBy;
		this.createdDateTime = createdDateTime;
		this.cTag = cTag;
		this.deleted = deleted;
		this.description = description;
		this.eTag = eTag;
		this.fileSystemInfo = fileSystemInfo;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDateTime = lastModifiedDateTime;
		this.name = name;
		this.parentReference = parentReference;
		this.searchResult = searchResult;
		this.shared = shared;
		this.sharePointIds = sharePointIds;
		this.size = size;
		this.webDavUrl = webDavUrl;
		this.webUrl = webUrl;

		// are they always have `parentReference` except root directory?
		if (parentReference != null) {
			if (parentReference.pathPointer != null && parentReference.rawPath != null) {
				this.pathPointer = parentReference.pathPointer.resolve(name);
			}
			this.idPointer = new IdPointer(id, parentReference.driveId);
		}
		else {
			this.idPointer = new IdPointer(id);
		}
	}

	@Override
	public String toString() {
		return '<' + id + ", " + pathPointer + '>';
	}


	// TODO: is there any way to merge with constructor? cause both are almost same
	protected void refreshBy(@NotNull BaseItem newItem) {
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

		if (parentReference != null) {
			if (parentReference.pathPointer != null && parentReference.rawPath != null) {
				this.pathPointer = parentReference.pathPointer.resolve(name);
			}
			this.idPointer = new IdPointer(id, parentReference.driveId);
		}
		else {
			this.idPointer = new IdPointer(id);
		}
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
		AsyncRequestHandler responseHandler =
				client.requestTool().patchMetadata(Client.ITEM_ID_PREFIX + id, content);

		HttpResponse response = responseHandler.getBlockingResponse();
		DirectByteInputStream result = responseHandler.getResultStream();

		BaseItem newItem =
				client.requestTool().parseAndHandle(response, result, HttpURLConnection.HTTP_OK, BaseItem.class);

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
			throw new IllegalArgumentException(
					"Can not address destination folder. `folder`'s id and path are both null");
	}

	@NotNull
	public String copyTo(@NotNull ItemReference folder, @NotNull String newName) throws ErrorResponseException {
		if (folder.id != null)
			return this.copyTo(folder.id, newName);
		else if (folder.pathPointer != null)
			return this.copyTo(folder.pathPointer, newName);
		else
			throw new IllegalArgumentException(
					"Can not address destination folder. `folder`'s id and path are both null");
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
		else throw new IllegalArgumentException(
					"Can not address destination folder. `folder`'s id and path are both null");
	}

	public void moveTo(@NotNull String id) throws ErrorResponseException {
		BaseItem item = client.moveItem(this.id, id);
		this.refreshBy(item);
	}

	public void moveTo(@NotNull BasePointer pointer) throws ErrorResponseException {
		BaseItem item = client.moveItem(this.idPointer, pointer);
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
		assert parentReference != null;
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
	public void setDescription(String description) throws ErrorResponseException {
		update(("{\"description\":\"" + description + "\"}").getBytes());
	}

	@JsonIgnore
	public void setName(@NotNull String name) throws ErrorResponseException {
		update(("{\"name\":\"" + name + "\"}").getBytes());
	}




	/*
	*************************************************************
	*
	* Custom Jackson Deserializer
	*
	*************************************************************
	 */


	public static class ItemDeserializer extends JsonDeserializer<BaseItem> {
		@Override
		public BaseItem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper codec = (ObjectMapper) parser.getCodec();
			ObjectNode node = codec.readTree(parser);

			if (node.has("file")) {
				if (node.has("folder") || node.has("package") || node.has("remoteItem")) {
					// TODO: custom exception
					throw new RuntimeException(SyncRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, FileItem.class);
			}
			else if (node.has("folder")) {
				if (node.has("file") || node.has("package") || node.has("remoteItem")) {
					// TODO: custom exception
					throw new RuntimeException(SyncRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, FolderItem.class);
			}
			else if (node.has("package")) {
				if (node.has("folder") || node.has("file") || node.has("remoteItem")) {
					// TODO: custom exception
					throw new RuntimeException(SyncRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, PackageItem.class);
			}
			else if (node.has("remoteItem")) {
				if (node.has("folder") || node.has("file") || node.has("file")) {
					// TODO: custom exception
					throw new RuntimeException(SyncRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, RemoteFolderItem.class);
			}
			else {
				throw new InvalidJsonException("Json doesn't have any type (file or folder or package etc.)");
			}
		}
	}
}