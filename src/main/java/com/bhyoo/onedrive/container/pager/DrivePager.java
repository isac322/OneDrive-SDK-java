package com.bhyoo.onedrive.container.pager;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.container.items.Drive;
import com.bhyoo.onedrive.exceptions.ErrorResponseException;
import com.bhyoo.onedrive.network.async.ResponseFuture;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_OK;

public class DrivePager extends AbstractPager<Drive[]> {
	protected DrivePager(@NotNull RequestTool requestTool, @NotNull DrivePage page) {
		super(requestTool, page);
	}

	public static DrivePager deserialize(@NotNull Client client, @NotNull JsonParser parser) throws IOException {
		DrivePage itemPage = DrivePage.deserialize(client, parser);
		return new DrivePager(client.requestTool(), itemPage);
	}

	@NotNull @Override public Iterator<Drive[]> iterator() {
		return new DrivePageIterator(requestTool, page);
	}

	public static class DrivePage extends Page<Drive[]> {
		DrivePage(@Nullable URI nextLink, @Nullable URI deltaLink, @NotNull Drive[] value) {
			super(nextLink, deltaLink, value);
		}

		@SneakyThrows(URISyntaxException.class)
		public static DrivePage deserialize(@NotNull Client client, @NotNull JsonParser parser) throws IOException {
			@Nullable URI nextLink = null;
			@Nullable URI deltaLink = null;
			@NotNull ArrayList<Drive> values = new ArrayList<>();

			while (parser.nextToken() != JsonToken.END_OBJECT) {
				String currentName = parser.getCurrentName();
				parser.nextToken();

				switch (currentName) {
					case "@odata.nextLink":
						nextLink = new URI(parser.getText());
						break;
					case "@odata.deltaLink":
						deltaLink = new URI(parser.getText());
						break;
					case "value":
						while (parser.nextToken() != JsonToken.END_ARRAY) {
							values.add(Drive.deserialize(client, parser));
						}
						break;
					case "@odata.context":
						// TODO
						break;
					default:
						Logger.getGlobal().info("Unknown attribute detected in DriveItemPager : " + currentName);
				}
			}

			return new DrivePage(nextLink, deltaLink, values.toArray(new Drive[0]));
		}
	}


	class DrivePageIterator extends PageIterator<Drive[]> {
		DrivePageIterator(@NotNull RequestTool requestTool, @NotNull Page<Drive[]> currentPage) {
			super(requestTool, currentPage);
		}

		@Override
		protected DrivePage parse(@NotNull ResponseFuture responseFuture) throws ErrorResponseException {
			return requestTool.parseDrivePageAndHandle(responseFuture.response(), responseFuture.getNow(), HTTP_OK);
		}
	}
}
