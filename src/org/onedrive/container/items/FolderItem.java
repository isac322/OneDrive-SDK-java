package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.network.HttpsRequest;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;

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
	@Getter protected FolderFacet folder;
	@Nullable protected SpecialFolderFacet specialFolder;
	@NotNull protected List<FolderItem> folderChildren;
	@NotNull protected List<FileItem> fileChildren;
	@NotNull protected List<BaseItem> allChildren;
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
						 @JsonProperty("folder") FolderFacet folder,
						 @JsonProperty("lastModifiedBy") IdentitySet lastModifiedBy,
						 @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
						 @JsonProperty("name") String name,
						 @JsonProperty("parentReference") ItemReference parentReference,
						 @JsonProperty("root") ObjectNode root,
						 @JsonProperty("searchResult") SearchResultFacet searchResult,
						 @JsonProperty("shared") SharedFacet shared,
						 @JsonProperty("sharePointIds") SharePointIdsFacet sharePointIds,
						 @JsonProperty("size") Long size,
						 @JsonProperty("specialFolder") SpecialFolderFacet specialFolder,
						 @JsonProperty("webDavUrl") String webDavUrl,
						 @JsonProperty("webUrl") String webUrl,
						 @JsonProperty("children@odata.nextLink") String nextLink,
						 @JsonProperty("children") ArrayNode children) {
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
	}

	protected static void parseChildren(@NotNull Client client, @NotNull JsonNode array, @Nullable String nextLink,
										@NotNull List<BaseItem> all, @NotNull List<FolderItem> folder,
										@NotNull List<FileItem> file) {
		while (true) {
			for (JsonNode child : array) {
				if (child.isObject()) {
					BaseItem item = client.getRequestTool().makeObjectFromJson(child, BaseItem.class);

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

	@NotNull
	public List<BaseItem> getAllChildren() {
		if (!isChildrenFetched()) fetchChildren();
		return allChildren;
	}

	@NotNull
	public List<FolderItem> getFolderChildren() {
		if (!isChildrenFetched()) fetchChildren();
		return folderChildren;
	}

	@NotNull
	public List<FileItem> getFileChildren() {
		if (!isChildrenFetched()) fetchChildren();
		return fileChildren;
	}

	@Override
	@NotNull
	public Iterator<BaseItem> iterator() {
		if (!isChildrenFetched()) fetchChildren();
		return new ChildrenIterator(allChildren.iterator());
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
