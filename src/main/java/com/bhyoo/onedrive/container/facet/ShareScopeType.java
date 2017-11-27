package com.bhyoo.onedrive.container.facet;

import org.jetbrains.annotations.NotNull;

public enum ShareScopeType {
	ANONYMOUS("anonymous"),
	ORGANIZATION("organization"),
	USERS("users");

	private final String type;

	ShareScopeType(String type) {this.type = type;}

	public static ShareScopeType deserialize(@NotNull String type) {
		switch (type) {
			case "anonymous":
				return ANONYMOUS;
			case "organization":
				return ORGANIZATION;
			case "users":
				return USERS;
			default:
				throw new IllegalStateException("Unknown attribute detected in ShareScopeType : " + type);
		}
	}

	@Override public String toString() {return type;}
}
