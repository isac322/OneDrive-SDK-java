package org.onedrive.container.items;

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
import lombok.Getter;
import lombok.Setter;
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
import org.onedrive.exceptions.InternalException;
import org.onedrive.exceptions.InvalidJsonException;
import org.onedrive.network.async.ResponseFuture;
import org.onedrive.network.async.ResponseFutureListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;

import static lombok.AccessLevel.PROTECTED;

/**
 * https://dev.onedrive.com/resources/item.htm
 * {@// TODO: Enhance javadoc }
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(using = BaseItem.ItemDeserializer.class)
abstract public class BaseItem {
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
	@Getter @Setter(PROTECTED) @NotNull protected String name;
	@Getter @Setter(PROTECTED) @Nullable protected ItemReference parentReference;
	@Getter @Setter(PROTECTED) @Nullable protected SearchResultFacet searchResult;
	@Getter @Setter(PROTECTED) @Nullable protected SharedFacet shared;
	@Getter @Setter(PROTECTED) @Nullable protected SharePointIdsFacet sharePointIds;
	@Getter @Setter(PROTECTED) protected long size;
	@Getter @Setter(PROTECTED) protected String webDavUrl;
	@Getter @Setter(PROTECTED) protected String webUrl;

	@Getter @JsonIgnore @NotNull protected PathPointer pathPointer;
	@Getter @JsonIgnore @NotNull protected IdPointer idPointer;

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
		final BaseItem[] newItem = new BaseItem[1];
		final CountDownLatch latch = new CountDownLatch(1);
		client.requestTool().patchMetadataAsync(Client.ITEM_ID_PREFIX + id, content,
				new ResponseFutureListener() {
					@Override public void operationComplete(ResponseFuture future) throws Exception {
						newItem[0] = client.requestTool().parseAndHandle(
								future.response(),
								future.get(),
								HttpURLConnection.HTTP_OK,
								BaseItem.class);
						latch.countDown();
					}
				});
		try {
			latch.await();
		}
		catch (InterruptedException e) {
			throw new InternalException("Exception occurs while waiting lock in BaseItem#update()", e);
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


	public static class ItemDeserializer extends JsonDeserializer<BaseItem> {
		@Override
		public BaseItem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper codec = (ObjectMapper) parser.getCodec();
			ObjectNode node = parser.readValueAsTree();

			BaseItem ret;
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
				if (node.has("folder") || node.has("file") || node.has("file")) {
					isMultipleType = true;
				}
				ret = codec.convertValue(node, RemoteFolderItem.class);
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


	static protected class PointerInjector<T extends BaseItem> extends StdConverter<T, T> {
		@Override public T convert(T value) {
			// are they always have `parentReference` except root directory?
			assert value.parentReference != null : "`parentReference` is null on FileItem";
			assert value.parentReference.pathPointer != null : "`parentReference.pathPointer` is null on FileItem";
			assert value.parentReference.rawPath != null : "`parentReference.rawPath` is null on FileItem";

			value.pathPointer = value.parentReference.pathPointer.resolve(value.name);
			value.idPointer = new IdPointer(value.id, value.parentReference.driveId);

			return value;
		}
	}
}