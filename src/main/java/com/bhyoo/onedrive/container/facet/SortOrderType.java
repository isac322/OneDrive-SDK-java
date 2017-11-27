package com.bhyoo.onedrive.container.facet;

import org.jetbrains.annotations.NotNull;

public enum SortOrderType {
	ASCENDING("ascending"),
	DESCENDING("descending");

	private final String type;

	SortOrderType(String type) {this.type = type;}

	public static SortOrderType deserialize(@NotNull String type) {
		switch (type) {
			case "ascending":
				return ASCENDING;
			case "descending":
				return DESCENDING;
			default:
				throw new IllegalStateException("Unknown attribute detected in SortOrderType : " + type);
		}
	}

	@Override public String toString() {return type;}
}
