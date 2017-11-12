package com.bhyoo.onedrive.container;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DriveType {
	@JsonProperty("personal") PERSONAL("personal"),
	@JsonProperty("business") BUSINESS("business"),
	@JsonProperty("documentLibrary") DOCUMENT_LIBRARY("documentLibrary");

	private final String type;

	DriveType(String type) {this.type = type;}

	@Override public String toString() {return type;}
}
