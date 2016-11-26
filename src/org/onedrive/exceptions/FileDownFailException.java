package org.onedrive.exceptions;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileDownFailException extends IOException {
	public FileDownFailException(@NotNull String message) {
		super(message);
	}
}
