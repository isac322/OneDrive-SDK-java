package com.bhyoo.onedrive.container.facet;

import org.jetbrains.annotations.NotNull;

public enum SortType {
	DEFAULT("default"),
	NAME("name"),
	TYPE("type"),
	SIZE("size"),
	TAKEN_OR_CREATED_DATETIME("takenOrCreatedDateTime"),
	LAST_MODIFIED_DATETIME("lastModifiedDateTime"),
	SEQUENCE("sequence");

	private final String type;

	SortType(String type) {this.type = type;}

	public static SortType deserialize(@NotNull String type) {
		switch (type) {
			case "default":
				return DEFAULT;
			case "name":
				return NAME;
			case "type":
				return TYPE;
			case "size":
				return SIZE;
			case "takenOrCreatedDateTime":
				return TAKEN_OR_CREATED_DATETIME;
			case "lastModifiedDateTime":
				return LAST_MODIFIED_DATETIME;
			case "sequence":
				return SEQUENCE;
			default:
				throw new IllegalStateException("Unknown attribute detected in SortType : " + type);
		}
	}

	@Override public String toString() {return type;}
}
