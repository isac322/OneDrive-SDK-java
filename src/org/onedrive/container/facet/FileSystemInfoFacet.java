package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.onedrive.container.BaseContainer;

import java.time.ZonedDateTime;

/**
 * https://dev.onedrive.com/facets/filesysteminfo_facet.htm
 * {@// TODO: enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class FileSystemInfoFacet {
	@Getter @NotNull protected final ZonedDateTime createdDateTime;
	@Getter @NotNull protected final ZonedDateTime lastModifiedDateTime;

	@JsonCreator
	protected FileSystemInfoFacet(@JsonProperty("createdDateTime") String createdDateTime,
								  @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime) {
		this.createdDateTime = BaseContainer.parseDateTime(createdDateTime);
		this.lastModifiedDateTime = BaseContainer.parseDateTime(lastModifiedDateTime);
	}
}
