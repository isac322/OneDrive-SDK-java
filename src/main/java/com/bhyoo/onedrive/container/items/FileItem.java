package com.bhyoo.onedrive.container.items;

import com.bhyoo.onedrive.container.facet.*;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.exceptions.InvalidJsonException;
import com.bhyoo.onedrive.network.async.DownloadFuture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public interface FileItem extends DriveItem {
	/**
	 * Works just like {@link FileItem#download(Path, String)}} except new name of item will automatically set with
	 * {@link FileItem#getName()}.
	 *
	 * @param path Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *             path of item that will be downloaded</b>. It must point parent directory of the item.
	 *
	 * @see FileItem#download(Path, String)
	 */
	void download(@NotNull String path) throws IOException, ErrorResponseException;

	/**
	 * Works just like {@link FileItem#download(Path, String)}}.
	 *
	 * @param path    Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                path of item that will be downloaded</b>. It must point parent directory of the item.
	 * @param newName new file name.
	 *
	 * @see FileItem#download(Path, String)
	 */
	void download(@NotNull String path, @NotNull String newName) throws IOException, ErrorResponseException;

	/**
	 * Works just like {@link FileItem#download(Path, String)}} except new name of item will automatically set with
	 * {@link FileItem#getName()}.
	 *
	 * @param folderPath Folder path. It could be relative path (like . or ..). Note that this parameter <b>isn't
	 *                   path of item that will be downloaded</b>. It must point parent directory of the item.
	 *
	 * @see FileItem#download(Path, String)
	 */
	void download(@NotNull Path folderPath) throws IOException, ErrorResponseException;

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
	void download(@NotNull Path folderPath, String newName) throws IOException, ErrorResponseException;


	@NotNull DownloadFuture downloadAsync(@NotNull Path folderPath) throws IOException;

	@NotNull DownloadFuture downloadAsync(@NotNull Path folderPath, String newName) throws IOException;


	/*
	*************************************************************
	*
	* Custom Getter
	*
	*************************************************************
	 */


	@Nullable String getMimeType();

	@Nullable String getCRC32();

	@Nullable String getSHA1();

	@Nullable String getQuickXorHash();


	@Nullable AudioFacet getAudio();

	@Nullable ImageFacet getImage();

	@Nullable LocationFacet getLocation();

	@Nullable PhotoFacet getPhoto();

	@Nullable VideoFacet getVideo();
}
