package com.bhyoo.onedrive.exceptions;

/**
 * Exception that happens in OneDrive server, such as invalid json.<br>
 * Most of this exception can be resolved by retrying. but if same exception happens continuously,
 * it's probably SDK error, so contact author.
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public interface OneDriveServerException {
}
