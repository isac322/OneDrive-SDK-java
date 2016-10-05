package org.onedrive.container.items;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.onedrive.utils.OneDriveRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

	private FolderItem() {
	}

	protected static void parseChildren(@NotNull Client client, @Nullable JsonNode array, @Nullable String nextLink,
										@NotNull List<BaseItem> all, @NotNull List<FolderItem> folder,
										@NotNull List<FileItem> file) {
		if (array == null) return;

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
				ObjectNode json = OneDriveRequest.doGetJson(new URL(nextLink), client.getAccessToken());

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

	private void fetchChildren() {
		ObjectNode content = OneDriveRequest.doGetJson("/drive/items/" + id + "/children", client.getAccessToken());

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

	public static class FolderDeserializer extends JsonDeserializer<FolderItem> {
		private Client client;

		public FolderDeserializer(Client client) {
			super();
			this.client = client;
		}

		@Override
		public FolderItem deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper mapper = (ObjectMapper) parser.getCodec();

			FolderItem folderItem = new FolderItem();
			folderItem.client = client;

			ObjectNode rootNode = mapper.readTree(parser);
			Iterator<Map.Entry<String, JsonNode>> iterator = rootNode.fields();

			String nextLink = null;
			ArrayNode arrayNode = null;

			while (iterator.hasNext()) {
				Map.Entry<String, JsonNode> next = iterator.next();

				JsonNode value = next.getValue();
				String key = next.getKey();

				/*
				for performance (I think). but didn't test yet.
				 */
				switch (value.getNodeType()) {
					// createdBy, fileSystemInfo, folder, lastModifiedBy, parentReference, remoteItem, root,
					// searchResult, shared, sharePointIds, specialFolder
					case OBJECT:
						switch (key) {
							case "createdBy":
								folderItem.createdBy = mapper.convertValue(value, IdentitySet.class);
								break;
							case "fileSystemInfo":
								folderItem.fileSystemInfo = mapper.convertValue(value, FileSystemInfoFacet.class);
								break;
							case "folder":
								folderItem.folder = mapper.convertValue(value, FolderFacet.class);
								break;
							case "lastModifiedBy":
								folderItem.lastModifiedBy = mapper.convertValue(value, IdentitySet.class);
								break;
							case "parentReference":
								folderItem.parentReference = mapper.convertValue(value, ItemReference.class);
								break;
							case "remoteItem":
								folderItem.remoteItem = mapper.convertValue(value, RemoteItemFacet.class);
								break;
							case "root":
								folderItem.root = true;
								break;
							case "shared":
								folderItem.shared = mapper.convertValue(value, SharedFacet.class);
								break;
							case "sharePointIds":
								folderItem.sharePointIds = mapper.convertValue(value, SharePointIdsFacet.class);
								break;
							case "searchResult":
								folderItem.searchResult = mapper.convertValue(value, SearchResultFacet.class);
								break;
							case "specialFolder":
								folderItem.specialFolder = mapper.convertValue(value, SpecialFolderFacet.class);
								break;
							default:
								throw new RuntimeException("Unsupported Class Type." + key + " : " + value);
						}
						break;
					// id, createdDateTime, cTag, description, eTag, lastModifiedDateTime, name, webDavUrl, webUrl
					case STRING:
						switch (key) {
							case "id":
								folderItem.id = value.asText();
								break;
							case "createdDateTime":
								folderItem.createdDateTime = BaseContainer.parseDateTime(value.asText());
								break;
							case "cTag":
								folderItem.cTag = value.asText();
								break;
							case "description":
								folderItem.description = value.asText();
								break;
							case "eTag":
								folderItem.description = value.asText();
								break;
							case "lastModifiedDateTime":
								folderItem.lastModifiedDateTime = BaseContainer.parseDateTime(value.asText());
								break;
							case "name":
								folderItem.name = value.asText();
								break;
							case "webUrl":
								folderItem.webUrl = value.asText();
								break;
							case "webDavUrl":
								folderItem.webDavUrl = value.asText();
								break;
							case "children@odata.nextLink":
								nextLink = value.asText();
								break;
						}
						break;
					case BOOLEAN:
						if (key.equals("deleted"))
							folderItem.deleted = value.asBoolean();
						else throw new RuntimeException("Unsupported Class Type." + key + " : " + value);
						break;
					case ARRAY:
						if (key.equals("children"))
							arrayNode = (ArrayNode) value;
						else throw new RuntimeException("Unsupported Class Type." + key + " : " + value);
						break;
					case NUMBER:
						if (key.equals("size")) folderItem.size = value.asLong();
						else throw new RuntimeException("Unsupported Class Type." + key + " : " + value);
						break;
					default:
						throw new RuntimeException("Unsupported Class Type");
				}
			}

			if (arrayNode != null) {
				folderItem.folderChildren = new ArrayList<>();
				folderItem.allChildren = new ArrayList<>();
				folderItem.fileChildren = new ArrayList<>();

				parseChildren(folderItem.client, arrayNode, nextLink,
						folderItem.allChildren, folderItem.folderChildren, folderItem.fileChildren);
			}

			return folderItem;
		}
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
