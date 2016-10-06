package org.onedrive.container;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.istack.internal.Nullable;
import lombok.Getter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@// TODO: add javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(using = Drive.DriveDeserializer.class)
public class Drive extends BaseContainer {
	protected static Map<String, Drive> containerSet = new LinkedHashMap<>();

	@Getter @Nullable protected final String id;
	@Getter @Nullable protected final String driveType;
	@Getter @Nullable protected final IdentitySet identitySet;
	@Getter @Nullable protected final String state;
	@Getter @Nullable protected final Long totalCapacity;
	@Getter @Nullable protected final Long deleted;
	@Getter @Nullable protected final Long usedCapacity;
	@Getter @Nullable protected final Long remaining;

	protected Drive(String id, String driveType, IdentitySet identitySet) {
		this.id = id;
		this.driveType = driveType;
		this.identitySet = identitySet;
		state = null;
		totalCapacity = deleted = usedCapacity = remaining = null;
	}

	protected Drive(String id, String driveType, IdentitySet identitySet, String state, Long totalCapacity,
					Long deleted, Long usedCapacity, Long remaining) {
		this.id = id;
		this.driveType = driveType;
		this.identitySet = identitySet;
		this.deleted = deleted;
		this.state = state;
		this.totalCapacity = totalCapacity;
		this.usedCapacity = usedCapacity;
		this.remaining = remaining;
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

	static class DriveDeserializer extends JsonDeserializer<Drive> {
		@Override
		public Drive deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper mapper = (ObjectMapper) parser.getCodec();
			ObjectNode rootNode = mapper.readTree(parser);

			JsonNode id = rootNode.get("id");

			Drive ret = containerSet.get(id.asText());

			if (ret != null) return ret;

			JsonNode driveType = rootNode.get("driveType");
			JsonNode identitySet = rootNode.get("identitySet");
			JsonNode quota = rootNode.get("quota");

			if (quota == null) {
				ret = new Drive(id.asText(), driveType.asText(), mapper.convertValue(identitySet, IdentitySet.class));
			}
			else {
				JsonNode state = quota.get("state");
				JsonNode totalCapacity = quota.get("total");
				JsonNode deleted = quota.get("deleted");
				JsonNode usedCapacity = quota.get("used");
				JsonNode remaining = quota.get("remaining");

				ret = new Drive(id.asText(), driveType.asText(), mapper.convertValue(identitySet, IdentitySet.class),
						state == null ? null : state.asText(),
						totalCapacity == null ? null : totalCapacity.asLong(),
						deleted == null ? null : deleted.asLong(),
						usedCapacity == null ? null : usedCapacity.asLong(),
						remaining == null ? null : remaining.asLong());
			}

			containerSet.put(id.asText(), ret);

			return ret;
		}
	}
}
