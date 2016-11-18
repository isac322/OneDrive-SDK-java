package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * <a href="https://dev.onedrive.com/facets/audio_facet.htm">https://dev.onedrive.com/facets/audio_facet.htm</a>
 * {@// TODO: Enhance javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public class AudioFacet {
	@Getter @Nullable protected final String album;
	@Getter @Nullable protected final String albumArtist;
	@Getter @Nullable protected final String artist;
	@Getter @Nullable protected final Long bitrate;
	@Getter @Nullable protected final String composers;
	@Getter @Nullable protected final String copyright;
	@Getter @Nullable protected final Long disc;
	@Getter @Nullable protected final Long discCount;
	@Getter @Nullable protected final Long duration;
	@Getter @Nullable protected final String genre;
	@Getter @Nullable protected final Boolean hasDrm;
	@Getter @Nullable protected final Boolean isVariableBitrate;
	@Getter @Nullable protected final String title;
	@Getter @Nullable protected final Long track;
	@Getter @Nullable protected final Long trackCount;
	@Getter @Nullable protected final Long year;

	@JsonCreator
	protected AudioFacet(@Nullable @JsonProperty("album") String album,
						 @Nullable @JsonProperty("albumArtist") String albumArtist,
						 @Nullable @JsonProperty("artist") String artist,
						 @Nullable @JsonProperty("bitrate") Long bitrate,
						 @Nullable @JsonProperty("composers") String composers,
						 @Nullable @JsonProperty("copyright") String copyright,
						 @Nullable @JsonProperty("disc") Long disc,
						 @Nullable @JsonProperty("discCount") Long discCount,
						 @Nullable @JsonProperty("duration") Long duration,
						 @Nullable @JsonProperty("genre") String genre,
						 @Nullable @JsonProperty("hasDrm") Boolean hasDrm,
						 @Nullable @JsonProperty("isVariableBitrate") Boolean isVariableBitrate,
						 @Nullable @JsonProperty("title") String title,
						 @Nullable @JsonProperty("track") Long track,
						 @Nullable @JsonProperty("trackCount") Long trackCount,
						 @Nullable @JsonProperty("year") Long year) {
		this.album = album;
		this.albumArtist = albumArtist;
		this.artist = artist;
		this.bitrate = bitrate;
		this.composers = composers;
		this.copyright = copyright;
		this.disc = disc;
		this.discCount = discCount;
		this.duration = duration;
		this.genre = genre;
		this.hasDrm = hasDrm;
		this.isVariableBitrate = isVariableBitrate;
		this.title = title;
		this.track = track;
		this.trackCount = trackCount;
		this.year = year;
	}
}
