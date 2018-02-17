package com.bhyoo.onedrive.container.facet;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * <a href="https://dev.onedrive.com/facets/video_facet.htm">https://dev.onedrive.com/facets/video_facet.htm</a>
 *
 * @author <a href="mailto:bh322yoo@gmail.com" target="_top">isac322</a>
 */
public class VideoFacet {
	@Getter protected final int audioBitsPerSample;
	@Getter protected final int audioChannels;
	@Getter protected final @Nullable String audioFormat;
	@Getter protected final int audioSamplesPerSecond;
	@Getter protected final int bitrate;
	@Getter protected final long duration;
	@Getter protected final @Nullable String fourCC;
	@Getter protected final double frameRate;
	@Getter protected final long height;
	@Getter protected final long width;

	protected VideoFacet(@NotNull Integer audioBitsPerSample, @NotNull Integer audioChannels,
						 @Nullable String audioFormat, @NotNull Integer audioSamplesPerSecond,
						 @NotNull Integer bitrate, @NotNull Long duration, @Nullable String fourCC,
						 @NotNull Double frameRate, @NotNull Long height, @NotNull Long width) {
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

	public static VideoFacet deserialize(@NotNull JsonParser parser) throws IOException {
		@Nullable Integer audioBitsPerSample = null;
		@Nullable Integer audioChannels = null;
		@Nullable String audioFormat = null;
		@Nullable Integer audioSamplesPerSecond = null;
		@Nullable Integer bitrate = null;
		@Nullable Long duration = null;
		@Nullable String fourCC = null;
		@Nullable Double frameRate = null;
		@Nullable Long height = null;
		@Nullable Long width = null;

		while (parser.nextToken() != JsonToken.END_OBJECT) {
			String currentName = parser.getCurrentName();
			parser.nextToken();

			switch (currentName) {
				case "audioBitsPerSample":
					audioBitsPerSample = parser.getIntValue();
					break;
				case "audioChannels":
					audioChannels = parser.getIntValue();
					break;
				case "audioFormat":
					audioFormat = parser.getText();
					break;
				case "audioSamplesPerSecond":
					audioSamplesPerSecond = parser.getIntValue();
					break;
				case "bitrate":
					bitrate = parser.getIntValue();
					break;
				case "duration":
					duration = parser.getLongValue();
					break;
				case "fourCC":
					fourCC = parser.getText();
					break;
				case "frameRate":
					frameRate = parser.getDoubleValue();
					break;
				case "height":
					height = parser.getLongValue();
					break;
				case "width":
					width = parser.getLongValue();
					break;
				default:
					throw new IllegalStateException("Unknown attribute detected in VideoFacet : " + currentName);
			}
		}

		assert audioBitsPerSample != null;
		assert audioChannels != null;
		assert audioSamplesPerSecond != null;
		assert bitrate != null;
		assert duration != null;
		assert frameRate != null;
		assert height != null;
		assert width != null;

		return new VideoFacet(audioBitsPerSample, audioChannels, audioFormat, audioSamplesPerSecond, bitrate, duration,
				fourCC, frameRate, height, width);
	}
}
