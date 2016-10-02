package org.OneDriveSync.container;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by isac322 on 16. 10. 3.
 */
abstract class Container {
	protected static Map<String, Container> containerSet = new HashMap<>();
	protected String id;

	public static boolean contains(String id) {
		return containerSet.containsKey(id);
	}

	public static Container get(String id) {
		return containerSet.get(id);
	}

	public static void put(Container drive) {
		containerSet.put(drive.id, drive);
	}
}
