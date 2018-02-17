package com.bhyoo.onedrive.container.items.pointer;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public enum Operator {
	ACTION_COPY("copy"),
	ACTION_CREATE_LINK("action.createLink"),
	CHILDREN("children"),
	CONTENT("content"),
	SEARCH("search"),
	DELTA("delta"),
	THUMBNAILS("thumbnails"),
	UPLOAD_CREATE_SESSION("upload.createSession");

	private final String operator;

	Operator(String operator) {this.operator = operator;}

	@Override public String toString() {return operator;}
}
