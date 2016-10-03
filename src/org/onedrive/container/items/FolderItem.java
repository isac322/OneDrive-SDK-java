package org.onedrive.container.items;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.utils.OneDriveRequest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class FolderItem extends BaseItem implements Iterable<BaseItem> {
	protected FolderFacet folder;
	@Getter protected SpecialFolderFacet specialFolder;
	protected List<FolderItem> folderChildren;
	protected List<FileItem> fileChildren;
	protected List<BaseItem> allChildren;

	protected FolderItem(Client client, String id, IdentitySet createdBy, ZonedDateTime createdDateTime, String cTag,
						 boolean deleted, String description, String eTag, FileSystemInfoFacet fileSystemInfo,
						 IdentitySet lastModifiedBy, ZonedDateTime lastModifiedDateTime, String name,
						 ItemReference parentReference, RemoteItemFacet remoteItem, SearchResultFacet searchResult,
						 SharedFacet shared, SharePointIdsFacet sharePointIds, Long size,
						 SpecialFolderFacet specialFolder, String webDavUrl, String webUrl,
						 List<FolderItem> folderChildren, List<FileItem> fileChildren, List<BaseItem> allChildren) {
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
		this.remoteItem = remoteItem;
		this.searchResult = searchResult;
		this.shared = shared;
		this.sharePointIds = sharePointIds;
		this.size = size;
		this.specialFolder = specialFolder;
		this.webDavUrl = webDavUrl;
		this.webUrl = webUrl;
		this.folderChildren = folderChildren;
		this.fileChildren = fileChildren;
		this.allChildren = allChildren;
	}

	protected static void parseChildren(Client client, JSONArray json,
										List<BaseItem> all, List<FolderItem> folder, List<FileItem> file) {
		for (Object item : json) {
			if (item instanceof JSONObject) {
				BaseItem child = BaseItem.parse(client, (JSONObject) item);

				if (child instanceof FolderItem) {
					folder.add((FolderItem) child);
				}
				else if (child instanceof FileItem) {
					file.add((FileItem) child);
				}
				else if (child instanceof PackageItem) {
					// TODO: Handling Package (https://dev.onedrive.com/facets/package_facet.htm).
				}
				else {
					// if child is neither FolderItem nor FileItem nor PackageItem.
					throw new UnsupportedOperationException("Children object must file or folder of package");
				}
				all.add(child);

			}
			else throw new UnsupportedOperationException("Wrong Children type.");
		}
	}

	@Nullable
	static FolderItem parseFolder(Client client, JSONObject json) {
		if (json == null) return null;

		List<BaseItem> all = null;
		List<FolderItem> folder = null;
		List<FileItem> file = null;

		// make items of children recursively.
		if (json.containsKey("children")) {
			all = new ArrayList<>();
			folder = new ArrayList<>();
			file = new ArrayList<>();

			parseChildren(client, json.getArray("children"), all, folder, file);
		}

		return new FolderItem(
				client,
				json.getString("id"),
				IdentitySet.parse(json.getObject("createdBy")),
				BaseContainer.parseDateTime(json.getString("createdDateTime")),
				json.getString("cTag"),
				json.getObject("deleted") != null,
				json.getString("description"),
				json.getString("eTag"),
				FileSystemInfoFacet.parse(json.getObject("fileSystemInfo")),
				IdentitySet.parse(json.getObject("lastModifiedBy")),
				BaseContainer.parseDateTime(json.getString("lastModifiedDateTime")),
				json.getString("name"),
				ItemReference.parse(json.getObject("parentReference")),
				RemoteItemFacet.parse(json.getObject("remoteItem")),
				SearchResultFacet.parse(json.getObject("searchResult")),
				SharedFacet.parse(json.getObject("shared")),
				SharePointIdsFacet.parse(json.getObject("sharepointIds")),
				json.getLong("size"),
				SpecialFolderFacet.parse(json.getObject("specialFolder")),
				json.getString("webDavUrl"),
				json.getString("webUrl"),
				folder,
				file,
				all
		);
	}

	public long chilerenCount() {
		return folder.getChildCount();
	}

	private void fetchChildren() {
		JSONObject json = OneDriveRequest.getJsonResponse(
				"/drive/items/" + id + "/children", client.getAccessToken());

		allChildren = new ArrayList<>();
		folderChildren = new ArrayList<>();
		fileChildren = new ArrayList<>();

		// TODO: if-none-match request header handling.
		// TODO: not 200 OK response handling.
		parseChildren(client, json.getArray("value"), allChildren, folderChildren, fileChildren);
	}

	public boolean isChildrenFetched() {
		return allChildren != null && folderChildren != null && fileChildren != null;
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

	class ChildrenIterator implements Iterator<BaseItem> {
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
