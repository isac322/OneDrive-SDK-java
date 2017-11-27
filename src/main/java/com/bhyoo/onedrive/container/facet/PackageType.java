package com.bhyoo.onedrive.container.facet;

import org.jetbrains.annotations.NotNull;

public enum PackageType {
	ONENOTE("oneNote");

	private final String type;

	PackageType(String type) {this.type = type;}

	public static PackageType deserialize(@NotNull String type) {
		switch (type) {
			case "oneNote":
				return ONENOTE;
			default:
				throw new IllegalStateException("Unknown attribute detected in PackageType : " + type);
		}
	}

	@Override public String toString() {return type;}
}
