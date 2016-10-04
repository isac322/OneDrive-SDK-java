package org.onedrive.container;

import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * {@// TODO: add javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class Drive extends BaseContainer {
	protected static Map<String, Drive> containerSet = new HashMap<>();
	@Getter protected final String id;
	@Getter protected final String driveType;
	@Getter protected final String state;
	@Getter protected final IdentitySet identitySet;
	@Getter protected final long totalCapacity;
	@Getter protected final long deleted;
	@Getter protected final long usedCapacity;
	@Getter protected final long remaining;

	protected Drive(String id, String driveType, IdentitySet identitySet, String state, long totalCapacity,
					long deleted, long usedCapacity, long remaining) {
		this.id = id;
		this.driveType = driveType;
		this.state = state;
		this.identitySet = identitySet;
		this.totalCapacity = totalCapacity;
		this.deleted = deleted;
		this.usedCapacity = usedCapacity;
		this.remaining = remaining;
	}

	@Nullable
	public static Drive parse(JSONObject json) {
		if (json == null) return null;

		JSONObject quota = json.getObject("quota");

		String id = json.getString("id");

		if (Drive.contains(id)) {
			return Drive.get(id);

		} else if (quota == null) {
			throw new RuntimeException("quota is empty in drive");

		} else {
			Drive drive = new Drive(
					json.getString("id"),
					json.getString("driveType"),
					IdentitySet.parse(json.getObject("owner")),
					quota.getString("state"),
					quota.getLong("total"),
					quota.getLong("deleted"),
					quota.getLong("used"),
					quota.getLong("remaining")
			);

			Drive.put(drive);

			return drive;
		}
	}

	public static boolean contains(String id) {
		return containerSet.containsKey(id);
	}

	@Nullable
	public static Drive get(String id) {
		return containerSet.get(id);
	}

	public static void put(Drive drive) {
		containerSet.put(drive.id, drive);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Drive && id.equals(((Drive) obj).getId());
	}
}
