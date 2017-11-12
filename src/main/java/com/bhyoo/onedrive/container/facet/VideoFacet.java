package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

// TODO: Enhance javadoc

/**
 * <a href="https://dev.onedrive.com/facets/video_facet.htm">https://dev.onedrive.com/facets/video_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class VideoFacet {
	@Getter protected final int audioBitsPerSample;
	@Getter protected final int audioChannels;
	@Getter protected final @NotNull String audioFormat;
	@Getter protected final int audioSamplesPerSecond;
	@Getter protected final int bitrate;
	@Getter protected final long duration;
	@Getter protected final @NotNull String fourCC;
	@Getter protected final double frameRate;
	@Getter protected final long height;
	@Getter protected final long width;

	@JsonCreator
	protected VideoFacet(@JsonProperty("audioBitsPerSample") @NotNull Integer audioBitsPerSample,
						 @JsonProperty("audioChannels") @NotNull Integer audioChannels,
						 @JsonProperty("audioFormat") @NotNull String audioFormat,
						 @JsonProperty("audioSamplesPerSecond") @NotNull Integer audioSamplesPerSecond,
						 @JsonProperty("bitrate") @NotNull Integer bitrate,
						 @JsonProperty("duration") @NotNull Long duration,
						 @JsonProperty("fourCC") @NotNull String fourCC,
						 @JsonProperty("frameRate") @NotNull Double frameRate,
						 @JsonProperty("height") @NotNull Long height,
						 @JsonProperty("width") @NotNull Long width) {
		this.audioBitsPerSample = audioBitsPerSample;
		this.audioChannels = audioChannels;
		this.audioFormat = audioFormat;
		this.audioSamplesPerSecond = audioSamplesPerSecond;
		this.bitrate = bitrate;
		this.duration = duration;
		this.fourCC = fourCC;
		this.frameRate = frameRate;
		this.height = height;
		this.width = width;
	}
}
