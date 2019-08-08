package com.bhyoo.onedrive.container.facet;

import com.bhyoo.onedrive.container.IdentitySet;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * <a href="https://dev.onedrive.com/facets/shared_facet.htm">https://dev.onedrive.com/facets/shared_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class SharedFacet {
	@Getter protected final @Nullable IdentitySet owner;
	@Getter protected final @Nullable ShareScopeType scope;
	@Getter protected final @Nullable IdentitySet sharedBy;
	@Getter protected final @Nullable String sharedDateTime;

	protected SharedFacet(@Nullable IdentitySet owner, @Nullable ShareScopeType scope,
						  @Nullable IdentitySet sharedBy, @Nullable String sharedDateTime) {
		this.owner = owner;
		this.scope = scope;
		this.sharedBy = sharedBy;
		this.sharedDateTime = sharedDateTime;
	}

	public static SharedFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable IdentitySet owner = null;
		@Nullable ShareScopeType scope = null;
		@Nullable IdentitySet sharedBy = null;
		@Nullable String sharedDateTime = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "owner":
					owner = IdentitySet.deserialize(parser);
					break;
				case "scope":
					scope = ShareScopeType.deserialize(parser.getText());
					break;
				case "sharedBy":
					sharedBy = IdentitySet.deserialize(parser);
					break;
				case "sharedDateTime":
					sharedDateTime = parser.getText();
					break;
				default:
					Logger.getGlobal().info("Unknown attribute detected in SharedFacet : " + currentName);
			}
		}

		return new SharedFacet(owner, scope, sharedBy, sharedDateTime);
	}
}
