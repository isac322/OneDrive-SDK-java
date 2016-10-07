package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.network.HttpsRequest;
import org.network.HttpsResponse;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = FolderItem.class)
public class FolderItem extends BaseItem implements Iterable<BaseItem> {
	@NotNull protected FolderFacet folder;
	@Nullable protected SpecialFolderFacet specialFolder;
	@Nullable protected List<FolderItem> folderChildren;
	@Nullable protected List<FileItem> fileChildren;
	@Nullable protected List<BaseItem> allChildren;
	protected boolean root;


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
						 @JsonProperty("root") ObjectNode root,
						 @JsonProperty("searchResult") @Nullable SearchResultFacet searchResult,
						 @JsonProperty("shared") @Nullable SharedFacet shared,
						 @JsonProperty("sharePointIds") @Nullable SharePointIdsFacet sharePointIds,
						 @JsonProperty("size") Long size,
						 @JsonProperty("specialFolder") @Nullable SpecialFolderFacet specialFolder,
						 @JsonProperty("webDavUrl") String webDavUrl,
						 @JsonProperty("webUrl") String webUrl,
						 @JsonProperty("children@odata.nextLink") @Nullable String nextLink,
						 @JsonProperty("children") @Nullable ArrayNode children) {
		this.client = client;

		this.id = id;
		this.createdBy = createdBy;
		this.createdDateTime = createdDateTime == null ? null : BaseContainer.parseDateTime(createdDateTime);
		this.cTag = cTag;
		this.deleted = deleted != null;
		this.description = description;
		this.eTag = eTag;
		this.fileSystemInfo = fileSystemInfo;
		this.folder = folder;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDateTime = lastModifiedDateTime == null ? null : BaseItem.parseDateTime(lastModifiedDateTime);
		this.name = name;
		this.parentReference = parentReference;
		this.searchResult = searchResult;
		this.shared = shared;
		this.sharePointIds = sharePointIds;
		this.size = size;
		this.specialFolder = specialFolder;
		this.webDavUrl = webDavUrl;
		this.webUrl = webUrl;
		this.root = root != null;

		if (children != null) {
			this.folderChildren = new ArrayList<>();
			this.fileChildren = new ArrayList<>();
			this.allChildren = new ArrayList<>();

			parseChildren(client, children, nextLink, allChildren, folderChildren, fileChildren);
		}
		else {
			folderChildren = null;
			fileChildren = null;
			allChildren = null;
		}

		if (!this.root && parentReference == null) {
			throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG + " parentReference is missing!");
		}
	}

	protected static void parseChildren(@NotNull Client client, @NotNull JsonNode array, @Nullable String nextLink,
										@NotNull List<BaseItem> all, @NotNull List<FolderItem> folder,
										@NotNull List<FileItem> file) {
		while (true) {
			for (JsonNode child : array) {
				if (child.isObject()) {
					BaseItem item = client.getMapper().convertValue(child, BaseItem.class);

					if (item instanceof FolderItem) {
						folder.add((FolderItem) item);
					}
					else if (item instanceof FileItem) {
						file.add((FileItem) item);
					}
					else if (item instanceof PackageItem) {
						// TODO: Handling Package (https://dev.onedrive.com/facets/package_facet.htm).
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

			if (nextLink == null) break;

			try {
				ObjectNode json = client.getRequestTool().doGetJson(new URL(nextLink), client.getAccessToken());

				if (json.has("@odata.nextLink")) {
					nextLink = json.get("@odata.nextLink").asText();
				}
				else {
					nextLink = null;
				}

				if (json.has("value")) {
					array = json.get("value");
				}
				else {
					throw new UnsupportedOperationException("Children object must file or folder of package");
				}
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
				throw new RuntimeException(HttpsRequest.NETWORK_ERR_MSG);
			}
		}
	}

	@Override
	@NotNull
	public ItemReference newReference() {
		return new ItemReference(root ? this.id.split("!")[0] : parentReference.driveId, id,
				root ? "/drive/root:" : parentReference.rawPath + '/' + name);
	}

	public boolean isRoot() {
		return root;
	}

	public long childrenCount() {
		return folder.getChildCount();
	}

	protected void fetchChildren() {
		ObjectNode content = client.getRequestTool().doGetJson(
				"/drive/items/" + id + "/children", client.getAccessToken());

		allChildren = new ArrayList<>();
		folderChildren = new ArrayList<>();
		fileChildren = new ArrayList<>();

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

	public boolean isChildrenFetched() {
		return allChildren != null && folderChildren != null && fileChildren != null;
	}

	public boolean isSpecial() {
		return specialFolder != null;
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
				String.format(
						"/drives/%s/items/%s/children",
						root ? this.id.split("!")[0] : parentReference.driveId,
						this.id),
				client.getAccessToken(),
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
	}
}
