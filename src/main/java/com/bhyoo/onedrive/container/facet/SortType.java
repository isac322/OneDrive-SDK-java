package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SortType {
	@JsonProperty("default") DEFAULT("default"),
	@JsonProperty("name") NAME("name"),
	@JsonProperty("type") TYPE("type"),
	@JsonProperty("size") SIZE("size"),
	@JsonProperty("takenOrCreatedDateTime") TAKEN_OR_CREATED_DATETIME("takenOrCreatedDateTime"),
	@JsonProperty("lastModifiedDateTime") LAST_MODIFIED_DATETIME("lastModifiedDateTime"),
	@JsonProperty("sequence") SEQUENCE("sequence");

	private final String type;

	SortType(String type) {this.type = type;}

	@Override public String toString() {return type;}
}
