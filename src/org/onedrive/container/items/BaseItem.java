package org.onedrive.container.items;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.network.BadRequestException;
import org.network.HttpsRequest;
import org.network.HttpsResponse;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.FileSystemInfoFacet;
import org.onedrive.container.facet.SearchResultFacet;
import org.onedrive.container.facet.SharePointIdsFacet;
import org.onedrive.container.facet.SharedFacet;
import org.onedrive.utils.OneDriveRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * https://dev.onedrive.com/resources/item.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(using = BaseItem.ItemDeserializer.class)
abstract public class BaseItem extends BaseContainer {
	@Getter @NotNull protected String id;
	@Getter protected IdentitySet createdBy;
	@Getter protected ZonedDateTime createdDateTime;
	/**
	 * The {@code cTag} value is modified when content or metadata of any descendant of the folder is changed.
	 */
	@Getter protected String cTag;
	@Getter protected boolean deleted;
	@Getter protected String description;
	/**
	 * The {@code eTag} value is only modified when the folder's properties are changed, except for properties that are
	 * derived from descendants (like {@code childCount} or {@code lastModifiedDateTime}).
	 */
	@Getter protected String eTag;
	@Getter protected FileSystemInfoFacet fileSystemInfo;
	@Getter protected IdentitySet lastModifiedBy;
	@Getter protected ZonedDateTime lastModifiedDateTime;
	@Getter @NotNull protected String name;
	@Getter @Nullable protected ItemReference parentReference;
	@Getter @Nullable protected SearchResultFacet searchResult;
	@Getter @Nullable protected SharedFacet shared;
	@Getter @Nullable protected SharePointIdsFacet sharePointIds;
	@Getter protected long size;
	@Getter protected String webDavUrl;
	@Getter protected String webUrl;
	@NotNull protected Client client;

	public void delete() throws IOException {
		HttpsResponse response = OneDriveRequest.doDelete("/drive/items/" + id, client.getAccessToken());

		if (response.getCode() != 200) {
			throw new BadRequestException("Bad request. It must be already deleted item or wrong ID.");
		}
	}

	public void copyTo(@NotNull FolderItem folder) {
		this.copyToId(folder.id, null);
	}

	public void copyTo(@NotNull FolderItem folder, @NotNull String newName) {
		this.copyToId(folder.id, newName);
	}

	public void copyTo(@NotNull ItemReference folder) {
		if (folder.id != null)
			this.copyToId(folder.id, null);
		else if (folder.path != null)
			this.copyToPath(folder.path, null);
		else
			throw new RuntimeException(
					"Because folder's id and path both are null, can not address destination folder.");
	}

	public void copyTo(@NotNull ItemReference folder, @NotNull String newName) {
		if (folder.id != null)
			this.copyToId(folder.id, newName);
		else if (folder.path != null)
			this.copyToPath(folder.path, newName);
		else
			throw new RuntimeException(
					"Because folder's id and path both are null, can not address destination folder.");
	}

	public void copyToPath(@NotNull String path) {
		this.copyToPath(path, null);
	}

	public void copyToPath(@NotNull String path, @Nullable String newName) {
		byte[] prefix = "{\"parentReference\":{\"path\":\"".getBytes();
		byte[] middle = path.getBytes();
		byte[] suffix;
		if (newName == null)
			suffix = "\"}}".getBytes();
		else
			suffix = ("\"},\"name\":\"" + newName + "\"}").getBytes();

		byte[] content = new byte[prefix.length + middle.length + suffix.length];

		System.arraycopy(prefix, 0, content, 0, prefix.length);
		System.arraycopy(middle, 0, content, prefix.length, middle.length);
		System.arraycopy(suffix, 0, content, prefix.length + middle.length, suffix.length);

		this.copyTo(content);
	}

	public void copyToId(@NotNull String id) {
		this.copyToId(id, null);
	}

	public void copyToId(@NotNull String id, @Nullable String newName) {
		byte[] prefix = "{\"parentReference\":{\"id\":\"".getBytes();
		byte[] middle = id.getBytes();
		byte[] suffix;
		if (newName == null)
			suffix = "\"}}".getBytes();
		else
			suffix = ("\"},\"name\":\"" + newName + "\"}").getBytes();

		byte[] content = new byte[prefix.length + middle.length + suffix.length];

		System.arraycopy(prefix, 0, content, 0, prefix.length);
		System.arraycopy(middle, 0, content, prefix.length, middle.length);
		System.arraycopy(suffix, 0, content, prefix.length + middle.length, suffix.length);

		this.copyTo(content);
	}

	protected void copyTo(byte[] content) {
		// ensure that only root item can have null parentReference.
		if (parentReference == null) {
			if (this instanceof FolderItem) {
				FolderItem item = (FolderItem) this;
				if (item.isRoot()) {
					throw new RuntimeException("Root folder can not be moved");
				}
			}

			throw new RuntimeException("ParentReference field is missing.");
		}

		HttpsResponse response = client.getRequestTool().postMetadata(
				String.format("/drives/%s/items/%s/action.copy", parentReference.driveId, this.id),
				client.getAccessToken(), content);

		if (response.getCode() != HttpsURLConnection.HTTP_ACCEPTED) {
			throw new RuntimeException(
					"Copy failed with : " + response.getCode() + " " + response.getMessage() +
							" " + response.getContentString());
		}
	}

	@NotNull
	abstract public ItemReference newReference();

	public static class ItemDeserializer extends JsonDeserializer<BaseItem> {
		@Override
		public BaseItem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper codec = (ObjectMapper) parser.getCodec();
			ObjectNode node = codec.readTree(parser);

			if (node.has("file")) {
				if (node.has("folder") || node.has("package") || node.has("remoteItem")) {
					throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, FileItem.class);
			}
			else if (node.has("folder")) {
				if (node.has("file") || node.has("package") || node.has("remoteItem")) {
					throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, FolderItem.class);
			}
			else if (node.has("package")) {
				if (node.has("folder") || node.has("file") || node.has("remoteItem")) {
					throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, PackageItem.class);
			}
			else if (node.has("remoteItem")) {
				if (node.has("folder") || node.has("file") || node.has("file")) {
					throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Duplicated type.");
				}
				return codec.convertValue(node, RemoteFolderItem.class);
			}
			else {
				throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
			}
		}
	}
}