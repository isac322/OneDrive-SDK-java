package com.bhyoo.onedrive.container.facet;

import org.jetbrains.annotations.NotNull;

public enum ViewType {
	DEFAULT("default"),
	ICONS("icons"),
	DETAILS("details"),
	THUMBNAILS("thumbnails");

	private final String type;

	ViewType(String type) {this.type = type;}

	public static ViewType deserialize(@NotNull String type) {
		switch (type) {
			case "default":
				return DEFAULT;
			case "icons":
				return ICONS;
			case "details":
				return DETAILS;
			case "thumbnails":
				return THUMBNAILS;
			default:
				throw new IllegalStateException("Unknown attribute detected in ViewType : " + type);
		}
	}

	@Override public String toString() {return type;}
}
