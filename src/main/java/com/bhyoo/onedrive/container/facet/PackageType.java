package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PackageType {
	@JsonProperty("oneNote") ONENOTE("oneNote");

	private final String type;

	PackageType(String type) {this.type = type;}

	@Override public String toString() {return type;}
}
