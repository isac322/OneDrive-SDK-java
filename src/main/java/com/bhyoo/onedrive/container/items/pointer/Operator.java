package com.bhyoo.onedrive.container.items.pointer;

/**
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public enum Operator {
	COPY("copy"),
	ACTION_CREATE_LINK("action.createLink"),
	CHILDREN("children"),
	CONTENT("content"),
	SEARCH("search"),
	DELTA("delta"),
	THUMBNAILS("thumbnails"),
	CREATE_UPLOAD_SESSION("createUploadSession");

	private final String operator;

	Operator(String operator) {this.operator = operator;}

	@Override public String toString() {return operator;}
}
