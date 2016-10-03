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
	@Getter @NotNull protected final int bitrate;
	@Getter @Nullable protected final String composers;
	@Getter @Nullable protected final String copyright;
	@Getter @Nullable protected final Integer disc;
	@Getter @Nullable protected final Integer discCount;
	@Getter @NotNull protected final long duration;
	@Getter @Nullable protected final String genre;
	@Getter @NotNull protected final boolean hasDrm;
	@Getter @NotNull protected final boolean isVariableBitrate;
	@Getter @Nullable protected final String title;
	@Getter @Nullable protected final Integer track;
	@Getter @Nullable protected final Integer trackCount;
	@Getter @Nullable protected final Integer year;

	protected AudioFacet(String album, String albumArtist, String artist, int bitrate, String composers,
						 String copyright, Integer disc, Integer discCount, long duration, String genre,
						 boolean hasDrm, boolean isVariableBitrate, String title, Integer track, Integer trackCount,
						 Integer year) {
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
				json.getInt("bitrate"),
				json.getString("composers"),
				json.getString("copyright"),
				json.getInt("disc"),
				json.getInt("discCount"),
				json.getLong("duration"),
				json.getString("genre"),
				json.getBoolean("hasDrm"),
				json.getBoolean("isVariableBitrate"),
				json.getString("title"),
				json.getInt("track"),
				json.getInt("trackCount"),
				json.getInt("year")
		);
	}
}
