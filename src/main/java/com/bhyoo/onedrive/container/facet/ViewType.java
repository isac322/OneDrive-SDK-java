package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ViewType {
	@JsonProperty("default") DEFAULT("default"),
	@JsonProperty("icons") ICONS("icons"),
	@JsonProperty("details") DETAILS("details"),
	@JsonProperty("thumbnails") THUMBNAILS("thumbnails");

	private final String type;

	ViewType(String type) {this.type = type;}

	@Override public String toString() {return type;}
}
