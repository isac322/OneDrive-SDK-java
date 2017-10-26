package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.*;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.async.DownloadFuture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(as = FileItem.class, converter = FileItem.PointerInjector.class)
public class FileItem extends BaseItem {
	@Getter @Setter(PRIVATE) @Nullable protected AudioFacet audio;
	@NotNull @Setter(PRIVATE) @JsonProperty protected FileFacet file;
	@Getter @Setter(PRIVATE) @Nullable protected ImageFacet image;
	@Getter @Setter(PRIVATE) @Nullable protected LocationFacet location;
	@Getter @Setter(PRIVATE) @Nullable protected PhotoFacet photo;
	@Getter @Setter(PRIVATE) @Nullable protected VideoFacet video;

	/**
	 * Works just like {@link FileItem#download(Path, String)}} except new name of item will automatically set with
	 * {@link FileItem#getName()}.
	 *
	 * @param path Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *             path of item that will be downloaded</b>. It must point parent directory of the item.
	 *
	 * @see FileItem#download(Path, String)
	 */
	public void download(@NotNull String path) throws IOException, ErrorResponseException {
		client.download(this.id, Paths.get(path), this.name);
	}

	/**
	 * Works just like {@link FileItem#download(Path, String)}}.
	 *
	 * @param path    Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                path of item that will be downloaded</b>. It must point parent directory of the item.
	 * @param newName new file name.
	 *
	 * @see FileItem#download(Path, String)
	 */
	public void download(@NotNull String path, @NotNull String newName) throws IOException, ErrorResponseException {
		client.download(this.id, Paths.get(path), newName);
	}

	/**
	 * Works just like {@link FileItem#download(Path, String)}} except new name of item will automatically set with
	 * {@link FileItem#getName()}.
	 *
	 * @param folderPath Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                   path of item that will be downloaded</b>. It must point parent directory of the item.
	 *
	 * @see FileItem#download(Path, String)
	 */
	public void download(@NotNull Path folderPath) throws IOException, ErrorResponseException {
		client.download(this.id, folderPath, this.name);
	}

	// TODO: handling overwriting file

	/**
	 * Download file from OneDrive to {@code folderPath} with {@code newName}.
	 * It could be relative path (like . or ..).<br>
	 * If {@code newName} is already exists in {@code folderPath} or {@code folderPath} is not folder,
	 * it will throw {@link IllegalArgumentException}.
	 *
	 * @param folderPath Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                   path of item that will be downloaded</b>. It must point parent directory of the item.
	 * @param newName    new file name.
	 *
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
		client.download(this.id, folderPath, newName);
	}


	public DownloadFuture downloadAsync(@NotNull Path folderPath) throws IOException {
		return client.downloadAsync(this.id, folderPath, this.name);
	}

	public DownloadFuture downloadAsync(@NotNull Path folderPath, String newName) throws IOException {
		return client.downloadAsync(this.id, folderPath, newName);
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


	@JsonIgnore public @Nullable String getMimeType() {return this.file.getMimeType();}


	@JsonIgnore public @Nullable String getCRC32() {return this.file.getCrc32Hash();}


	@JsonIgnore public @Nullable String getSHA1() {return this.file.getSha1Hash();}

	@JsonIgnore public @Nullable String getQuickXorHash() {return this.file.getQuickXorHash();}


	static class PointerInjector extends BaseItem.PointerInjector<FileItem> {}
}
