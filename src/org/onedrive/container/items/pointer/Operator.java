package org.onedrive.container.items.pointer;

import lombok.Getter;

/**
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public enum Operator {
	ACTION_COPY("action.copy"),
	ACTION_CREATE_LINK("action.createLink"),
	CHILDREN("children"),
	CONTENT("content"),
	VIEW_SEARCH("view.search"),
	VIEW_DELTA("view.delta"),
	THUMBNAILS("thumbnails");

	@Getter public String string;

	Operator(String string) {
		this.string = string;
	}
}
