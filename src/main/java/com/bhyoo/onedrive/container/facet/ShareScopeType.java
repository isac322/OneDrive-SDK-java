package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ShareScopeType {
	@JsonProperty("anonymous") ANONYMOUS("anonymous"),
	@JsonProperty("organization") ORGANIZATION("organization"),
	@JsonProperty("users") USERS("users");

	private final String type;

	ShareScopeType(String type) {this.type = type;}

	@Override public String toString() {return type;}
}
