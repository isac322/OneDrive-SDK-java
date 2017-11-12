package com.bhyoo.onedrive.container.facet;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import static lombok.AccessLevel.PRIVATE;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/audio_facet.htm">https://dev.onedrive.com/facets/audio_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AudioFacet {
	@Getter @Setter(PRIVATE) @Nullable protected String album;
	@Getter @Setter(PRIVATE) @Nullable protected String albumArtist;
	@Getter @Setter(PRIVATE) @Nullable protected String artist;
	@Getter @Setter(PRIVATE) @Nullable protected Integer bitrate;
	@Getter @Setter(PRIVATE) @Nullable protected String composers;
	@Getter @Setter(PRIVATE) @Nullable protected String copyright;
	@Getter @Setter(PRIVATE) @Nullable protected Integer disc;
	@Getter @Setter(PRIVATE) @Nullable protected Integer discCount;
	@Getter @Setter(PRIVATE) @Nullable protected Long duration;
	@Getter @Setter(PRIVATE) @Nullable protected String genre;
	@Getter @Setter(PRIVATE) @Nullable protected Boolean hasDrm;
	@Getter @Setter(PRIVATE) @Nullable protected Boolean isVariableBitrate;
	@Getter @Setter(PRIVATE) @Nullable protected String title;
	@Getter @Setter(PRIVATE) @Nullable protected Integer track;
	@Getter @Setter(PRIVATE) @Nullable protected Integer trackCount;
	@Getter @Setter(PRIVATE) @Nullable protected Integer year;
}
