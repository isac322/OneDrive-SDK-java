package org.onedrive.container.items;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.network.HttpsResponse;
import org.onedrive.Client;
import org.onedrive.container.BaseContainer;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.utils.OneDriveRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.time.ZonedDateTime;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileItem extends BaseItem {
	@Getter @Nullable protected AudioFacet audio;
	@NotNull protected FileFacet file;
	@Getter @Nullable protected ImageFacet image;
	@Getter @Nullable protected LocationFacet location;
	@Getter @Nullable protected PhotoFacet photo;
	@Getter @Nullable protected VideoFacet video;

	protected FileItem(Client client, String id, AudioFacet audio, IdentitySet createdBy,
					   ZonedDateTime createdDateTime, String cTag, boolean deleted, String description, String eTag,
					   FileFacet file, FileSystemInfoFacet fileSystemInfo, ImageFacet image,
					   IdentitySet lastModifiedBy, ZonedDateTime lastModifiedDateTime, LocationFacet location,
					   String name, ItemReference parentReference, PhotoFacet photo, RemoteItemFacet remoteItem,
					   SearchResultFacet searchResult, SharedFacet shared, SharePointIdsFacet sharePointIds, long size,
					   VideoFacet video, String webDavUrl, String webUrl) {
		this.client = client;
		this.id = id;
		this.audio = audio;
		this.createdBy = createdBy;
		this.createdDateTime = createdDateTime;
		this.cTag = cTag;
		this.deleted = deleted;
		this.description = description;
		this.eTag = eTag;
		this.file = file;
		this.fileSystemInfo = fileSystemInfo;
		this.image = image;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDateTime = lastModifiedDateTime;
		this.location = location;
		this.name = name;
		this.parentReference = parentReference;
		this.photo = photo;
		this.remoteItem = remoteItem;
		this.searchResult = searchResult;
		this.shared = shared;
		this.sharePointIds = sharePointIds;
		this.size = size;
		this.video = video;
		this.webDavUrl = webDavUrl;
		this.webUrl = webUrl;
	}

	@Nullable
	static FileItem parseFile(Client client, JSONObject json) {
		if (json == null) return null;

		return new FileItem(
				client,
				json.getString("id"),
				AudioFacet.parse(json.getObject("audio")),
				IdentitySet.parse(json.getObject("createdBy")),
				BaseContainer.parseDateTime(json.getString("createdDateTime")),
				json.getString("cTag"),
				json.getObject("deleted") != null,
				json.getString("description"),
				json.getString("eTag"),
				FileFacet.parse(json.getObject("file")),
				FileSystemInfoFacet.parse(json.getObject("fileSystemInfo")),
				ImageFacet.parse(json.getObject("image")),
				IdentitySet.parse(json.getObject("lastModifiedBy")),
				BaseContainer.parseDateTime(json.getString("lastModifiedDateTime")),
				LocationFacet.parse(json.getObject("location")),
				json.getString("name"),
				ItemReference.parse(json.getObject("parentReference")),
				PhotoFacet.parse(json.getObject("photo")),
				RemoteItemFacet.parse(json.getObject("remoteItem")),
				SearchResultFacet.parse(json.getObject("searchResult")),
				SharedFacet.parse(json.getObject("shared")),
				SharePointIdsFacet.parse(json.getObject("sharepointIds")),
				json.getLong("size"),
				VideoFacet.parse(json.getObject("video")),
				json.getString("webDavUrl"),
				json.getString("webUrl")
		);
	}

	public String getCRC32() {
		return this.file.getCrc32Hash();
	}

	public String getSHA1() {
		return this.file.getSha1Hash();
	}

	public String getQuickXorHash() {
		return this.file.getQuickXorHash();
	}

	/**
	 * @see FileItem#download(File)
	 */
	@NotNull
	public void download(@NotNull String path) throws IOException, FileDownFailException {
		this.download(new File(path));
	}

	/**
	 * Download file from OneDrive to {@code path}.<br>
	 * It could be relative path (like . or ..).<br>
	 * If {@code path} is just directory path, automatically naming as {@code getName()}.<br>
	 * <br>
	 * If {@code path} is file path and already exists, it will throw {@link FileAlreadyExistsException}.
	 *
	 * @param path File or folder path. It could be either parent folder(without filename) for download
	 *             or specific file path (with filename).
	 * @throws IOException                If an I/O error occurs, which is possible because the construction of the
	 *                                    canonical pathname may require filesystem queries
	 * @throws SecurityException          If a required system property value cannot be accessed, or if a security
	 *                                    manager exists and its SecurityManager.checkRead method denies read access to
	 *                                    the file
	 * @throws FileAlreadyExistsException If {@code path} is file path and already exists.
	 * @throws FileDownFailException      If fail to download with not 200 OK response.
	 */
	@NotNull
	public void download(@NotNull File path) throws IOException, FileDownFailException {
		File fullPath = path.getCanonicalFile();

		if (!fullPath.isFile()) {
			if (!fullPath.exists()) {
				//
				throw new FileAlreadyExistsException(fullPath.getAbsolutePath() + " is already exists.");
			}

			fullPath = new File(fullPath, this.getName());
		}
		else if (fullPath.exists()) {
			throw new FileAlreadyExistsException(fullPath.getAbsolutePath() + " is already exists.");
		}

		HttpsResponse response = OneDriveRequest.doGet(
				String.format("/drive/items/%s/content", id),
				client.getAccessToken());

		if (response.getCode() != 200) {
			throw new FileDownFailException(
					String.format("File download fails with %d %s", response.getCode(), response.getMessage()));
		}
		else if (!fullPath.getParentFile().mkdirs()) {
			throw new IOException("Fail to create necessary parent directory.");
		}

		FileOutputStream file = new FileOutputStream(fullPath);
		file.write(response.getContent());
		file.flush();
		file.close();
	}
}
