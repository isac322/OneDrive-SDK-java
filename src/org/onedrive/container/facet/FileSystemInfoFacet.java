package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * https://dev.onedrive.com/facets/filesysteminfo_facet.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileSystemInfoFacet {
	@Getter @NotNull protected final String createdDateTime;
	@Getter @NotNull protected final String lastModifiedDateTime;

	@JsonCreator
	protected FileSystemInfoFacet(@NotNull @JsonProperty("createdDateTime") String createdDateTime,
								  @NotNull @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime) {
		this.createdDateTime = createdDateTime;
		this.lastModifiedDateTime = lastModifiedDateTime;
	}
}
