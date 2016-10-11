package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.Client;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.network.AsyncHttpsResponseHandler;
import org.onedrive.network.HttpsClientHandler;
import org.onedrive.network.legacy.HttpsRequest;
import org.onedrive.network.legacy.HttpsResponse;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = FolderItem.class)
public class FolderItem extends BaseItem implements Iterable<BaseItem> {
	@NotNull protected FolderFacet folder;
	@Nullable @JsonProperty protected SpecialFolderFacet specialFolder;
	@Nullable @JsonIgnore protected List<FolderItem> folderChildren;
	@Nullable @JsonIgnore protected List<FileItem> fileChildren;
	@Nullable @JsonIgnore protected List<BaseItem> allChildren;
	@Nullable @JsonProperty protected ObjectNode root;


	@JsonCreator
	protected FolderItem(@JacksonInject("OneDriveClient") Client client,
						 @JsonProperty("id") String id,
						 @JsonProperty("createdBy") IdentitySet createdBy,
						 @JsonProperty("createdDateTime") String createdDateTime,
						 @JsonProperty("cTag") String cTag,
						 @JsonProperty("deleted") ObjectNode deleted,
						 @JsonProperty("description") String description,
						 @JsonProperty("eTag") String eTag,
						 @JsonProperty("fileSystemInfo") FileSystemInfoFacet fileSystemInfo,
						 @JsonProperty("folder") @NotNull FolderFacet folder,
						 @JsonProperty("lastModifiedBy") IdentitySet lastModifiedBy,
						 @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
						 @JsonProperty("name") @NotNull String name,
						 @JsonProperty("parentReference") @Nullable ItemReference parentReference,
						 @JsonProperty("root") @Nullable ObjectNode root,
						 @JsonProperty("searchResult") @Nullable SearchResultFacet searchResult,
						 @JsonProperty("shared") @Nullable SharedFacet shared,
						 @JsonProperty("sharePointIds") @Nullable SharePointIdsFacet sharePointIds,
						 @JsonProperty("size") Long size,
						 @JsonProperty("specialFolder") @Nullable SpecialFolderFacet specialFolder,
						 @JsonProperty("webDavUrl") String webDavUrl,
						 @JsonProperty("webUrl") String webUrl,
						 @JsonProperty("children@odata.nextLink") @Nullable String nextLink,
						 @JsonProperty("children") @Nullable ArrayNode children) {
		super(client, id, createdBy, createdDateTime, cTag, deleted, description, eTag, fileSystemInfo,
				lastModifiedBy, lastModifiedDateTime, name, parentReference, searchResult, shared, sharePointIds,
				size, webDavUrl, webUrl);

		this.folder = folder;
		this.specialFolder = specialFolder;
		this.root = root;

		if (children != null) {
			this.folderChildren = new CopyOnWriteArrayList<>();
			this.fileChildren = new CopyOnWriteArrayList<>();
			this.allChildren = new CopyOnWriteArrayList<>();

			parseChildren(client, children, nextLink, allChildren, folderChildren, fileChildren);
		}
		else {
			folderChildren = null;
			fileChildren = null;
			allChildren = null;
		}

		if (!isRoot() && parentReference == null) {
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " parentReference is missing!");
		}
	}

	protected static void addChildren(@NotNull Client client, @NotNull JsonNode array, @NotNull List<BaseItem> all,
									  @NotNull List<FolderItem> folder, @NotNull List<FileItem> file) {
		for (JsonNode child : array) {
			if (child.isObject()) {
				BaseItem item = client.getMapper().convertValue(child, BaseItem.class);

				if (item instanceof FolderItem) {
					folder.add((FolderItem) item);
				}
				else if (item instanceof FileItem) {
					file.add((FileItem) item);
				}
				else {
					// if child is neither FolderItem nor FileItem nor PackageItem.
					throw new UnsupportedOperationException("Children object must file or folder of package");
				}
				all.add(item);
			}
			// if child is neither FolderItem nor FileItem nor PackageItem.
			else
				throw new UnsupportedOperationException("Children object must file or folder of package");
		}
	}

	@SneakyThrows(URISyntaxException.class)
	protected static void parseChildren(@NotNull final Client client, @NotNull JsonNode array,
										@Nullable String nextLink, @NotNull List<BaseItem> all,
										@NotNull List<FolderItem> folder, @NotNull List<FileItem> file) {
		final ObjectNode jsonObject[] = new ObjectNode[1];
		while (nextLink != null) {
			@NotNull
			HttpsClientHandler httpsHandler =
					client.getRequestTool().doAsync(new URI(nextLink), HttpMethod.GET, new AsyncHttpsResponseHandler
							() {
						@Override
						public void handle(@NotNull InputStream resultStream, @NotNull HttpResponse response) {
							try {
								jsonObject[0] = (ObjectNode) client.getMapper().readTree(resultStream);
							}
							catch (IOException e) {
								e.printStackTrace();
								throw new RuntimeException(
										HttpsRequest.NETWORK_ERR_MSG + " Can not convert response to JSON");
							}
						}
					});

			addChildren(client, array, all, folder, file);
			try {
				httpsHandler.getBlockingCloseFuture().sync();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (jsonObject[0].has("@odata.nextLink")) {
				nextLink = jsonObject[0].get("@odata.nextLink").asText();
			}
			else nextLink = null;

			if (jsonObject[0].has("value")) {
				array = jsonObject[0].get("value");
			}
			else {
				throw new UnsupportedOperationException("Children object must file or folder of package");
			}
		}

		addChildren(client, array, all, folder, file);
	}

	protected void fetchChildren() {
		ObjectNode content = client.getRequestTool().doGetJson("/drive/items/" + id + "/children");

		allChildren = new CopyOnWriteArrayList<>();
		folderChildren = new CopyOnWriteArrayList<>();
		fileChildren = new CopyOnWriteArrayList<>();

		JsonNode value = content.get("value");
		JsonNode nextLink = content.get("@odata.nextLink");

		if (!value.isArray() || (nextLink != null && !nextLink.isTextual())) {
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
		}

		// TODO: if-none-match request header handling.
		// TODO: not 200 OK response handling.
		parseChildren(client, value, nextLink == null ? null : nextLink.asText(),
				allChildren, folderChildren, fileChildren);
	}

	/**
	 * Implementation of <a href='https://dev.onedrive.com/items/create.htm'>detail</a>.
	 * <p>
	 * {@// TODO: Enhance javadoc }
	 * {@// TODO: Implement '@name.conflictBehavior' }
	 *
	 * @param name New folder name.
	 * @return New folder's ID.
	 * @throws RuntimeException If creating folder or converting response is fails.
	 */
	@NotNull
	public String createFolder(@NotNull String name) {
		byte[] prefix = "{\"name\":\"".getBytes();
		byte[] middle = name.getBytes();
		byte[] suffix = "\",\"folder\":{}}".getBytes();

		byte[] content = new byte[prefix.length + middle.length + suffix.length];

		System.arraycopy(prefix, 0, content, 0, prefix.length);
		System.arraycopy(middle, 0, content, prefix.length, middle.length);
		System.arraycopy(suffix, 0, content, prefix.length + middle.length, suffix.length);

		HttpsResponse response = client.getRequestTool().postMetadata(
				String.format("/drives/%s/items/%s/children", getDriveId(), this.id),
				content);

		// 201 Created
		if (response.getCode() != HttpsURLConnection.HTTP_CREATED) {
			throw new RuntimeException("Create folder " + name + " fails.");
		}


		try {
			return client.getMapper().readTree(response.getContent()).get("id").asText();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " Converting response to json is fail.");
		}
	}


	/*
	=============================================================
	Custom Getter
	=============================================================
	 */


	@JsonIgnore
	public boolean isRoot() {
		return root != null;
	}

	@JsonIgnore
	public boolean isChildrenFetched() {
		return allChildren != null && folderChildren != null && fileChildren != null;
	}

	@JsonIgnore
	public boolean isSpecial() {
		return specialFolder != null;
	}

	public long childrenCount() {
		return folder.getChildCount();
	}


	@NotNull
	@Override
	public String getDriveId() {
		if (isRoot()) return id.split("!")[0];
		assert parentReference != null;
		return parentReference.driveId;
	}

	@Nullable
	@Override
	public String getPath() {
		if (isRoot()) return "/drive/root:";
		assert parentReference != null;
		if (parentReference.path == null) return null;
		return parentReference.path + '/' + name;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull
	public List<BaseItem> getAllChildren() {
		if (!isChildrenFetched()) fetchChildren();
		return allChildren;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull
	public List<FolderItem> getFolderChildren() {
		if (!isChildrenFetched()) fetchChildren();
		return folderChildren;
	}

	@SuppressWarnings("ConstantConditions")
	@NotNull
	public List<FileItem> getFileChildren() {
		if (!isChildrenFetched()) fetchChildren();
		return fileChildren;
	}


	/*
	=============================================================
	Custom Iterator
	=============================================================
	 */


	@NotNull
	@Override
	public Iterator<BaseItem> iterator() {
		return new ChildrenIterator(getAllChildren().iterator());
	}

	private class ChildrenIterator implements Iterator<BaseItem> {
		private final Iterator<BaseItem> itemIterator;

		ChildrenIterator(Iterator<BaseItem> iterator) {
			this.itemIterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return itemIterator.hasNext();
		}

		@Override
		public BaseItem next() {
			return itemIterator.next();
		}

		@Override
		public void remove() {
			itemIterator.remove();
		}
	}
}
