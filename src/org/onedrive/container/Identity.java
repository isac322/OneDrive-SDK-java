package org.onedrive.container;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * https://dev.onedrive.com/resources/identitySet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
@JsonDeserialize(using = Identity.IdentityDeserializer.class)
public class Identity {
	protected static Map<String, Identity> identitySet = new HashMap<>();
	@Getter @NotNull protected final String id;
	@Getter @Nullable protected final String displayName;
	@Getter @Nullable protected final ObjectNode thumbnails;

	protected Identity(@Nullable String name, @NotNull String id, @Nullable ObjectNode thumbnails) {
		this.displayName = name;
		this.id = id;
		this.thumbnails = thumbnails;
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


	static class IdentityDeserializer extends JsonDeserializer<Identity> {
		@Override
		public Identity deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			ObjectMapper mapper = (ObjectMapper) parser.getCodec();
			ObjectNode rootNode = mapper.readTree(parser);

			JsonNode id = rootNode.get("id");

			Identity identity = identitySet.get(id.asText());

			if (identity != null) return identity;
			else {
				JsonNode displayName = rootNode.get("displayName");
				JsonNode thumbnails = rootNode.get("thumbnails");

				identity = new Identity(displayName == null ? null : displayName.asText(),
						id.asText(),
						thumbnails == null ? null : (ObjectNode) thumbnails);

				identitySet.put(id.asText(), identity);

				return identity;
			}
		}
	}
}
