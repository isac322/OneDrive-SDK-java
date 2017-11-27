package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * <a href="https://dev.onedrive.com/facets/audio_facet.htm">https://dev.onedrive.com/facets/audio_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class AudioFacet {
	@Getter protected final @Nullable String album;
	@Getter protected final @Nullable String albumArtist;
	@Getter protected final @Nullable String artist;
	@Getter protected final @Nullable Integer bitrate;
	@Getter protected final @Nullable String composers;
	@Getter protected final @Nullable String copyright;
	@Getter protected final @Nullable Integer disc;
	@Getter protected final @Nullable Integer discCount;
	@Getter protected final @Nullable Long duration;
	@Getter protected final @Nullable String genre;
	@Getter protected final @Nullable Boolean hasDrm;
	@Getter protected final @Nullable Boolean isVariableBitrate;
	@Getter protected final @Nullable String title;
	@Getter protected final @Nullable Integer track;
	@Getter protected final @Nullable Integer trackCount;
	@Getter protected final @Nullable Integer year;


	protected AudioFacet(@Nullable String album, @Nullable String albumArtist, @Nullable String artist,
						 @Nullable Integer bitrate, @Nullable String composers, @Nullable String copyright,
						 @Nullable Integer disc, @Nullable Integer discCount, @Nullable Long duration,
						 @Nullable String genre, @Nullable Boolean hasDrm, @Nullable Boolean isVariableBitrate,
						 @Nullable String title, @Nullable Integer track, @Nullable Integer trackCount,
						 @Nullable Integer year) {
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

	public static AudioFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable String album = null;
		@Nullable String albumArtist = null;
		@Nullable String artist = null;
		@Nullable Integer bitrate = null;
		@Nullable String composers = null;
		@Nullable String copyright = null;
		@Nullable Integer disc = null;
		@Nullable Integer discCount = null;
		@Nullable Long duration = null;
		@Nullable String genre = null;
		@Nullable Boolean hasDrm = null;
		@Nullable Boolean isVariableBitrate = null;
		@Nullable String title = null;
		@Nullable Integer track = null;
		@Nullable Integer trackCount = null;
		@Nullable Integer year = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "album":
					album = parser.getText();
					break;
				case "albumArtist":
					albumArtist = parser.getText();
					break;
				case "artist":
					artist = parser.getText();
					break;
				case "bitrate":
					bitrate = parser.getIntValue();
					break;
				case "composers":
					composers = parser.getText();
					break;
				case "copyright":
					copyright = parser.getText();
					break;
				case "disc":
					disc = parser.getIntValue();
					break;
				case "discCount":
					discCount = parser.getIntValue();
					break;
				case "duration":
					duration = parser.getLongValue();
					break;
				case "genre":
					genre = parser.getText();
					break;
				case "hasDrm":
					hasDrm = parser.getBooleanValue();
					break;
				case "isVariableBitrate":
					isVariableBitrate = parser.getBooleanValue();
					break;
				case "title":
					title = parser.getText();
					break;
				case "track":
					track = parser.getIntValue();
					break;
				case "trackCount":
					trackCount = parser.getIntValue();
					break;
				case "year":
					year = parser.getIntValue();
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in AudioFacet : " + currentName);
			}
		}

		return new AudioFacet(album, albumArtist, artist, bitrate, composers, copyright, disc, discCount, duration,
				genre, hasDrm, isVariableBitrate, title, track, trackCount, year);
	}
}
