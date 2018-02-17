package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * <a href="https://dev.onedrive.com/facets/package_facet.htm">https://dev.onedrive.com/facets/package_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class PackageFacet {
	@Getter protected final @NotNull PackageType type;

	protected PackageFacet(@NotNull PackageType type) {this.type = type;}

	public static PackageFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable PackageType type = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "type":
					type = PackageType.deserialize(parser.getText());
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in PackageFacet : " + currentName);
			}
		}

		assert type != null;

		return new PackageFacet(type);
	}
}
