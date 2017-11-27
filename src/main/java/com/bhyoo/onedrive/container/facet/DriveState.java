package com.bhyoo.onedrive.container.facet;

import org.jetbrains.annotations.NotNull;

public enum DriveState {
	NORMAL("normal"),
	NEARING("nearing"),
	CRITICAL("critical"),
	EXCEEDED("exceeded");

	private final String type;

	DriveState(String type) {this.type = type;}

	public static DriveState deserialize(@NotNull String type) {
		switch (type) {
			case "normal":
				return NORMAL;
			case "nearing":
				return NEARING;
			case "critical":
				return CRITICAL;
			case "exceeded":
				return EXCEEDED;
			default:
				throw new IllegalStateException("Unknown attribute detected in ViewType : " + type);
		}
	}

	@Override public String toString() {return type;}
}
