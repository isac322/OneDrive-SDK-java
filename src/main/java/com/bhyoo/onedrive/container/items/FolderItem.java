package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.container.facet.FolderFacet;
import com.bhyoo.onedrive.container.facet.SpecialFolderFacet;
import com.bhyoo.onedrive.container.items.pointer.IdPointer;
import com.bhyoo.onedrive.container.items.pointer.PathPointer;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InternalException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import com.bhyoo.onedrive.network.async.ResponseFutureListener;
import com.bhyoo.onedrive.utils.ByteBufStream;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdConverter;
import io.netty.handler.codec.http.HttpMethod;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = FolderItem.class, converter = FolderItem.PointerInjector.class)
public class FolderItem extends BaseItem implements Iterable<BaseItem> {
	@Setter(PRIVATE) @JsonProperty protected FolderFacet folder;
	@Setter(PRIVATE) @JsonProperty protected SpecialFolderFacet specialFolder;
	@Setter(PRIVATE) @JsonProperty protected ObjectNode root;
	@JsonIgnore protected List<FolderItem> folderChildren;
	@JsonIgnore protected List<FileItem> fileChildren;
	@JsonIgnore protected List<BaseItem> allChildren;
	@JsonProperty("children@odata.nextLink") @Nullable String nextLink;
	@JsonProperty("children") @Nullable ArrayNode children;

	protected static void addChildren(@NotNull Client client, @NotNull JsonNode array, @NotNull List<BaseItem> all,
									  @NotNull List<FolderItem> folder, @NotNull List<FileItem> file) {
		for (JsonNode child : array) {
			if (!child.isObject())
				// if child isn't object
				throw new InvalidJsonException("Response isn't object in JSON. response : " + child);

			BaseItem item = client.mapper().convertValue(child, BaseItem.class);

			if (item instanceof FolderItem) {
				folder.add((FolderItem) item);
			}
			else if (item instanceof FileItem) {
				file.add((FileItem) item);
			}
			else {
				// if child is neither FolderItem nor FileItem nor PackageItem.
				assert item instanceof PackageItem || item instanceof RemoteItem : "Wrong item type";
			}
			all.add(item);
		}
	}

	@SneakyThrows(URISyntaxException.class)
	protected static void parseChildren(@NotNull final Client client, @NotNull JsonNode array,
										@Nullable String nextLink, @NotNull List<BaseItem> all,
										@NotNull List<FolderItem> folder, @NotNull List<FileItem> file) {
		final ObjectNode jsonObject[] = new ObjectNode[1];
		while (nextLink != null) {
			final CountDownLatch latch = new CountDownLatch(1);

			ResponseFuture responseFuture = client.requestTool().doAsync(
					HttpMethod.GET,
					new URI(nextLink),
					new ResponseFutureListener() {
						@Override public void operationComplete(ResponseFuture future) throws Exception {
							ByteBufStream result = future.get();

							try {
								jsonObject[0] = (ObjectNode) client.mapper().readTree(result);
							}
							catch (JsonProcessingException e) {
								throw new InvalidJsonException(
										e,
										future.response().status().code(),
										result.getRawBuffer());
							}
							catch (IOException e) {
								// FIXME: custom exception
								throw new RuntimeException("DEV: Unrecognizable json response.", e);
							}
							latch.countDown();
						}
					});

			addChildren(client, array, all, folder, file);

			// responseFuture.syncUninterruptibly();
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				throw new InternalException("Exception occurs while waiting lock in BaseItem#update()", e);
			}

			if (jsonObject[0].has("@odata.nextLink"))
				nextLink = jsonObject[0].get("@odata.nextLink").asText();
			else
				nextLink = null;

			array = jsonObject[0].get("value");
		}

		addChildren(client, array, all, folder, file);
	}

	protected void fetchChildren() throws ErrorResponseException {
		_fetchChildren(Client.ITEM_ID_PREFIX + id + "/children");
	}

	protected void _fetchChildren(String url) throws ErrorResponseException {
		ObjectNode content = client.requestTool().doGetJson(url);

		allChildren = new CopyOnWriteArrayList<>();
		folderChildren = new CopyOnWriteArrayList<>();
		fileChildren = new CopyOnWriteArrayList<>();

		JsonNode value = content.get("value");
		JsonNode nextLink = content.get("@odata.nextLink");

		// TODO: if-none-match request header handling.
		// TODO: not 200 OK response handling.
		parseChildren(client, value, nextLink == null ? null : nextLink.asText(),
				allChildren, folderChildren, fileChildren);
	}

	// TODO: Enhance javadoc
	// TODO: Implement '@name.conflictBehavior'

	// TODO: add more @throws

	/**
	 * Implementation of <a href='https://dev.onedrive.com/items/create.htm'>detail</a>.
	 * <p>
	 *
	 * @param name New folder name.
	 *
	 * @return New folder's object.
	 *
	 * @throws RuntimeException If creating folder or converting response is fails.
	 */
	@NotNull
	public FolderItem createFolder(@NotNull String name) throws ErrorResponseException {
		return client.createFolder(this.id, name);
	}


	/**
	 * Update data itself with {@code newItem}.<br>
	 * This will <b>set all children {@code List} null</b>. so very next call that related with children may take a
	 * while to
	 * fetch children data.
	 *
	 * @param newItem New object that contains new data to update.
	 *
	 * @throws IllegalArgumentException It's because of construction of {@link IdPointer} or {@link PathPointer}.
	 * @throws InternalException        if parameter {@code parentReference} is null even if this isn't root directory.
	 */
	@Override
	protected void refreshBy(@NotNull BaseItem newItem) {
		super.refreshBy(newItem);

		FolderItem item = (FolderItem) newItem;

		this.folder = item.folder;
		this.specialFolder = item.specialFolder;

		folderChildren = null;
		fileChildren = null;
		allChildren = null;

		if (isRoot()) {
			pathPointer = new PathPointer("/", getDriveId());
		}
	}


	/*
	*************************************************************
	*
	* Custom Getter
	*
	*************************************************************
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
		return parentReference.driveId;
	}

	@NotNull
	public List<BaseItem> allChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return allChildren;
	}

	@NotNull
	public List<FolderItem> folderChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return folderChildren;
	}

	@NotNull
	public List<FileItem> fileChildren() throws ErrorResponseException {
		if (!isChildrenFetched()) fetchChildren();
		return fileChildren;
	}


	/*
	*************************************************************
	*
	* Custom Iterator
	*
	*************************************************************
	 */


	@NotNull
	@Override
	public Iterator<BaseItem> iterator() {
		try {
			return new ChildrenIterator(allChildren().iterator());
		}
		catch (ErrorResponseException e) {
			// FIXME: custom exception
			throw new RuntimeException(e);
		}
	}

	static class PointerInjector<T extends FolderItem> extends StdConverter<T, T> {
		@Override public T convert(T value) {
			if (value.children != null) {
				value.folderChildren = new CopyOnWriteArrayList<>();
				value.fileChildren = new CopyOnWriteArrayList<>();
				value.allChildren = new CopyOnWriteArrayList<>();

				parseChildren(value.client, value.children, value.nextLink, value.allChildren, value.folderChildren,
						value.fileChildren);
			}

			assert value.parentReference != null :
					"All item can't have null `parentReference` argument";
			if (value.isRoot()) {
				value.pathPointer = new PathPointer("/", value.getDriveId());
			}
			else {
				assert value.parentReference.pathPointer != null :
						"`parentReference.pathPointer` is null on FolderItem";
				assert value.parentReference.rawPath != null : "`parentReference.rawPath` is null on FolderItem";
				value.pathPointer = value.parentReference.pathPointer.resolve(value.name);
			}
			value.idPointer = new IdPointer(value.id, value.getDriveId());

			return value;
		}
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
