package com.bhyoo.onedrive.container;

import org.jetbrains.annotations.NotNull;

public enum DriveType {
	PERSONAL("personal"),
	BUSINESS("business"),
	DOCUMENT_LIBRARY("documentLibrary");

	private final String type;

	DriveType(String type) {this.type = type;}

	public static DriveType deserialize(@NotNull String type) {
		switch (type) {
			case "personal":
				return PERSONAL;
			case "business":
				return BUSINESS;
			case "documentLibrary":
				return DOCUMENT_LIBRARY;
			default:
				throw new IllegalStateException("Unknown attribute detected in DriveType : " + type);
		}
	}

	@Override public String toString() {return type;}
}
