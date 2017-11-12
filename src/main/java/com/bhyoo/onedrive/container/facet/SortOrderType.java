package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SortOrderType {
	@JsonProperty("ascending") ASCENDING("ascending"),
	@JsonProperty("descending") DESCENDING("descending");

	private final String type;

	SortOrderType(String type) {this.type = type;}

	@Override public String toString() {return type;}
}
