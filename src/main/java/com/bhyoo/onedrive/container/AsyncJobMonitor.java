package com.bhyoo.onedrive.container;

import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.network.sync.SyncRequest;
import com.bhyoo.onedrive.network.sync.SyncResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.GZIP;

/**
 * <a href='https://docs.microsoft.com/ko-kr/onedrive/developer/rest-api/concepts/long-running-actions'>Official doc</a>
 */
public class AsyncJobMonitor {
	protected @NotNull String url;
	@Getter protected @NotNull String operation;
	@Getter protected @Nullable String resourceId;
	@Getter protected @NotNull Double percentageComplete;
	@Getter protected @NotNull AsyncJobStatus status;
	@Getter protected @NotNull String statusDescription;


	public AsyncJobMonitor(@NotNull String url) {
		this.url = url;

		update();
	}

	@SneakyThrows(MalformedURLException.class)
	public AsyncJobMonitor update() {
		SyncResponse response = new SyncRequest(new URL(this.url))
				.setHeader(ACCEPT, APPLICATION_JSON)
				.setHeader(ACCEPT_ENCODING, GZIP)
				.doGet();

		// TODO: error handling

		try {
			JsonParser parser = RequestTool.jsonFactory.createParser(response.getContent());
			parser.nextToken();

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String currentName = parser.getCurrentName();
				parser.nextToken();

				switch (currentName) {
					case "operation":
						operation = parser.getText();
						break;
					case "resourceId":
						resourceId = parser.getText();
						break;
					case "percentageComplete":
						percentageComplete = parser.getDoubleValue();
						break;
					case "status":
						status = AsyncJobStatus.deserialize(parser.getText());
						break;
					case "statusDescription":
						statusDescription = parser.getText();
						break;
					case "@odata.context":
						// TODO
						break;
					default:
						throw new IllegalStateException(
								"Unknown attribute detected in AsyncJobMonitor : " + currentName);
				}
			}

			return this;
		}
		catch (IOException e) {
			// FIXME: custom exception
			throw new RuntimeException("DEV: Unrecognizable json response.", e);
		}
	}
}
