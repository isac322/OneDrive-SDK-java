package org.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.internal.Nullable;
import lombok.Getter;

/**
 * https://dev.onedrive.com/facets/audio_facet.htm
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
	protected AudioFacet(@JsonProperty("album") String album,
						 @JsonProperty("albumArtist") String albumArtist,
						 @JsonProperty("artist") String artist,
						 @JsonProperty("bitrate") Long bitrate,
						 @JsonProperty("composers") String composers,
						 @JsonProperty("copyright") String copyright,
						 @JsonProperty("disc") Long disc,
						 @JsonProperty("discCount") Long discCount,
						 @JsonProperty("duration") Long duration,
						 @JsonProperty("genre") String genre,
						 @JsonProperty("hasDrm") Boolean hasDrm,
						 @JsonProperty("isVariableBitrate") Boolean isVariableBitrate,
						 @JsonProperty("title") String title,
						 @JsonProperty("track") Long track,
						 @JsonProperty("trackCount") Long trackCount,
						 @JsonProperty("year") Long year) {
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
