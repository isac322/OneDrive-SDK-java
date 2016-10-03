package org.onedrive.container.facet;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import lombok.Getter;
import org.json.simple.JSONObject;

/**
 * https://dev.onedrive.com/facets/audio_facet.htm
 * TODO: Enhance javadoc
 * Created by isac322 on 16. 10. 3.
 *
 * @author isac322
 */
public class AudioFacet {
	@Getter @Nullable protected final String album;
	@Getter @Nullable protected final String albumArtist;
	@Getter @Nullable protected final String artist;
	@Getter @NotNull protected final long bitrate;
	@Getter @Nullable protected final String composers;
	@Getter @Nullable protected final String copyright;
	@Getter @Nullable protected final Long disc;
	@Getter @Nullable protected final Long discCount;
	@Getter @NotNull protected final long duration;
	@Getter @Nullable protected final String genre;
	@Getter @NotNull protected final boolean hasDrm;
	@Getter @Nullable protected final Boolean isVariableBitrate;
	@Getter @Nullable protected final String title;
	@Getter @Nullable protected final Long track;
	@Getter @Nullable protected final Long trackCount;
	@Getter @Nullable protected final Long year;

	protected AudioFacet(String album, String albumArtist, String artist, long bitrate, String composers,
						 String copyright, Long disc, Long discCount, long duration, String genre,
						 boolean hasDrm, Boolean isVariableBitrate, String title, Long track, Long trackCount,
						 Long year) {
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

	@Nullable
	public static AudioFacet parse(JSONObject json) {
		if (json == null) return null;

		return new AudioFacet(
				json.getString("album"),
				json.getString("albumArtist"),
				json.getString("artist"),
				json.getLong("bitrate"),
				json.getString("composers"),
				json.getString("copyright"),
				json.getLong("disc"),
				json.getLong("discCount"),
				json.getLong("duration"),
				json.getString("genre"),
				json.getBoolean("hasDrm"),
				json.getBoolean("isVariableBitrate"),
				json.getString("title"),
				json.getLong("track"),
				json.getLong("trackCount"),
				json.getLong("year")
		);
	}
}
