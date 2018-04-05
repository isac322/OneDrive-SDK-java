package com.bhyoo.onedrive.container;

import org.jetbrains.annotations.NotNull;

public enum AsyncJobStatus {
	NOT_STARTED("notStarted"),
	IN_PROGRESS("inProgress"),
	COMPLETED("completed"),
	UPDATING("updating"),
	FAILED("failed"),
	DELETE_PENDING("deletePending"),
	DELETE_FAILED("deleteFailed"),
	WAITING("waiting");

	private final String type;

	AsyncJobStatus(String type) {this.type = type;}

	public static AsyncJobStatus deserialize(@NotNull String type) {
		switch (type) {
			case "notStarted":
				return NOT_STARTED;
			case "inProgress":
				return IN_PROGRESS;
			case "completed":
				return COMPLETED;
			case "updating":
				return UPDATING;
			case "failed":
				return FAILED;
			case "deletePending":
				return DELETE_PENDING;
			case "deleteFailed":
				return DELETE_FAILED;
			case "waiting":
				return WAITING;
			default:
				throw new IllegalStateException("Unknown attribute detected in AsyncJobStatus : " + type);
		}
	}

	@Override public String toString() {return type;}
}
