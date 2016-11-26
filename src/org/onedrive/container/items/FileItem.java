package org.onedrive.container.items;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onedrive.Client;
import org.onedrive.container.IdentitySet;
import org.onedrive.container.facet.*;
import org.onedrive.container.items.pointer.IdPointer;
import org.onedrive.exceptions.ErrorResponseException;
import org.onedrive.exceptions.InvalidJsonException;
import org.onedrive.network.ErrorResponse;
import org.onedrive.network.legacy.HttpsResponse;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = FileItem.class)
public class FileItem extends BaseItem {
	@Getter @Nullable protected AudioFacet audio;
	@NotNull @JsonProperty protected FileFacet file;
	@Getter @Nullable protected ImageFacet image;
	@Getter @Nullable protected LocationFacet location;
	@Getter @Nullable protected PhotoFacet photo;
	@Getter @Nullable protected VideoFacet video;

	/**
	 * @throws IllegalArgumentException It's solely because of construction of {@link IdPointer}.
	 * @see IdPointer#IdPointer(String, String)
	 * @see IdPointer#IdPointer(String)
	 */
	@JsonCreator
	protected FileItem(@JacksonInject("OneDriveClient") Client client,
					   @JsonProperty("id") @NotNull String id,
					   @JsonProperty("audio") @Nullable AudioFacet audio,
					   @JsonProperty("createdBy") IdentitySet createdBy,
					   @JsonProperty("createdDateTime") String createdDateTime,
					   @JsonProperty("cTag") String cTag,
					   @JsonProperty("deleted") ObjectNode deleted,
					   @JsonProperty("description") String description,
					   @JsonProperty("eTag") String eTag,
					   @JsonProperty("file") @NotNull FileFacet file,
					   @JsonProperty("fileSystemInfo") FileSystemInfoFacet fileSystemInfo,
					   @JsonProperty("image") @Nullable ImageFacet image,
					   @JsonProperty("lastModifiedBy") IdentitySet lastModifiedBy,
					   @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
					   @JsonProperty("location") @Nullable LocationFacet location,
					   @JsonProperty("name") @NotNull String name,
					   @JsonProperty("parentReference") @NotNull ItemReference parentReference,
					   @JsonProperty("photo") @Nullable PhotoFacet photo,
					   @JsonProperty("searchResult") @Nullable SearchResultFacet searchResult,
					   @JsonProperty("shared") @Nullable SharedFacet shared,
					   @JsonProperty("sharePointIds") @Nullable SharePointIdsFacet sharePointIds,
					   @JsonProperty("size") long size,
					   @JsonProperty("video") @Nullable VideoFacet video,
					   @JsonProperty("webDavUrl") String webDavUrl,
					   @JsonProperty("webUrl") String webUrl) {
		super(client, id, createdBy, createdDateTime, cTag, deleted, description, eTag, fileSystemInfo,
				lastModifiedBy, lastModifiedDateTime, name, parentReference, searchResult, shared, sharePointIds,
				size, webDavUrl, webUrl);

		this.audio = audio;
		this.file = file;
		this.image = image;
		this.location = location;
		this.photo = photo;
		this.video = video;
	}

	/**
	 * Works just like {@link FileItem#download(Path, String)}} except new name of item will automatically set with
	 * {@link FileItem#getName()}.
	 *
	 * @param path Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *             path of item that will be downloaded</b>. It must point parent directory of the item.
	 * @see FileItem#download(Path, String)
	 */
	public void download(@NotNull String path) throws IOException, ErrorResponseException {
		this.download(Paths.get(path));
	}

	/**
	 * Works just like {@link FileItem#download(Path, String)}}.
	 *
	 * @param path    Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                path of item that will be downloaded</b>. It must point parent directory of the item.
	 * @param newName new file name.
	 * @see FileItem#download(Path, String)
	 */
	public void download(@NotNull String path, @NotNull String newName) throws IOException, ErrorResponseException {
		this.download(Paths.get(path), newName);
	}

	/**
	 * Works just like {@link FileItem#download(Path, String)}} except new name of item will automatically set with
	 * {@link FileItem#getName()}.
	 *
	 * @param folderPath Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                   path of item that will be downloaded</b>. It must point parent directory of the item.
	 * @see FileItem#download(Path, String)
	 */
	public void download(@NotNull Path folderPath) throws IOException, ErrorResponseException {
		download(folderPath, this.getName());
	}

	/**
	 * Download file from OneDrive to {@code folderPath} with {@code newName}.
	 * It could be relative path (like . or ..).<br>
	 * If {@code newName} is already exists in {@code folderPath} or {@code folderPath} is not folder,
	 * it will throw {@link IllegalArgumentException}.
	 * {@// TODO: handling overwriting file }
	 *
	 * @param folderPath Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                   path of item that will be downloaded</b>. It must point parent directory of the item.
	 * @param newName    new file name.
	 * @throws SecurityException        If a required system property value cannot be accessed, or if a security
	 *                                  manager exists and its SecurityManager.checkRead method denies read access to
	 *                                  the file
	 * @throws IllegalArgumentException If {@code folderPath} is exists and is not directory.
	 * @throws ErrorResponseException   if error happens while requesting downloading operation. such as trying to
	 *                                  download already deleted file.
	 * @throws InvalidJsonException     if fail to parse response of copying request into json. it caused by server
	 *                                  side not by SDK.
	 * @throws IOException              if an I/O error occurs
	 */
	public void download(@NotNull Path folderPath, String newName) throws IOException, ErrorResponseException {
		Path parent = folderPath.toAbsolutePath();
		folderPath = parent.resolve(newName);
		// it's illegal if and only if `folderPath` exists but not directory.
		if (Files.exists(parent) && !Files.isDirectory(parent))
			throw new IllegalArgumentException("`folderPath` argument must pointing directory.");

		Files.createDirectories(parent);

		HttpsResponse response =
				client.requestTool().newRequest(Client.ITEM_ID_PREFIX + id + "/content").doGet();

		if (response.getCode() != HttpURLConnection.HTTP_OK) {
			try {
				ErrorResponse error = client.mapper().readValue(response.getContent(), ErrorResponse.class);
				throw new ErrorResponseException(
						HttpsURLConnection.HTTP_OK,
						response.getCode(),
						error.getCode(),
						error.getMessage()
				);
			}
			catch (JsonProcessingException e) {
				throw new InvalidJsonException(e, response.getCode(), response.getContent());
			}
			catch (IOException e) {
				e.printStackTrace();
				// TODO: custom exception
				throw new RuntimeException("DEV: Unrecognizable error response. contact author");
			}
		}

		AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
				folderPath,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE);

		ByteBuffer contentBuf = ByteBuffer.wrap(response.getContent());

		fileChannel.write(contentBuf, 0);
	}


	@Override
	protected void refreshBy(@NotNull BaseItem newItem) {
		super.refreshBy(newItem);

		FileItem item = (FileItem) newItem;

		this.audio = item.audio;
		this.file = item.file;
		this.image = item.image;
		this.location = item.location;
		this.photo = item.photo;
		this.video = item.video;
	}



	/*
	*************************************************************
	*
	* Custom Getter
	*
	*************************************************************
	 */


	@Nullable
	@JsonIgnore
	public String getMimeType() {
		return this.file.getMimeType();
	}

	@Nullable
	@JsonIgnore
	public String getCRC32() {
		return this.file.getCrc32Hash();
	}

	@Nullable
	@JsonIgnore
	public String getSHA1() {
		return this.file.getSha1Hash();
	}

	@Nullable
	@JsonIgnore
	public String getQuickXorHash() {
		return this.file.getQuickXorHash();
	}
}
