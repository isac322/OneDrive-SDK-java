package org.onedrive.container;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class Identity extends BaseContainer {
	protected static Map<String, Identity> identitySet = new HashMap<>();
	@Getter @NotNull protected final String id;
	@Getter @Nullable protected final String displayName;
	@Getter @Nullable protected final JSONObject thumbnails;

	protected Identity(String name, String id, JSONObject thumbnails) {
		this.displayName = name;
		this.id = id;
		this.thumbnails = thumbnails;
	}

	@Nullable
	public static Identity parse(JSONObject json) {
		if (json == null) return null;

		String id = json.getString("id");

		if (Identity.contains(id)) {
			return Identity.get(id);

		} else {
			Identity identity = new Identity(
					id,
					json.getString("displayName"),
					json.getObject("thumbnails")
			);

			Identity.put(identity);

			return identity;
		}
	}

	public static boolean contains(String id) {
		return identitySet.containsKey(id);
	}

	@Nullable
	public static Identity get(String id) {
		return identitySet.get(id);
	}

	public static void put(Identity identity) {
		identitySet.put(identity.id, identity);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Identity && id.equals(((Identity) obj).getId());
	}
}
