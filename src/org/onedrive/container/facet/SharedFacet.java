package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.onedrive.container.IdentitySet;

/**
 * https://dev.onedrive.com/facets/shared_facet.htm
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class SharedFacet {
	@Getter protected final IdentitySet owner;
	@Getter protected final String scope;

	@JsonCreator
	protected SharedFacet(@JsonProperty("owner") IdentitySet owner,
						  @JsonProperty("scope") String scope) {
		this.owner = owner;
		this.scope = scope;
	}
}
