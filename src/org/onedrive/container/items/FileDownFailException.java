package org.onedrive.container.items;

import org.jetbrains.annotations.NotNull;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileDownFailException extends Exception {
	public FileDownFailException(@NotNull String message) {
		super(message);
	}
}
