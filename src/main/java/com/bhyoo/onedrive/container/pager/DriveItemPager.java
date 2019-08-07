package com.bhyoo.onedrive.container.pager;

import com.bhyoo.onedrive.client.Client;
import com.bhyoo.onedrive.client.RequestTool;
import com.bhyoo.onedrive.container.items.AbstractDriveItem;
import com.bhyoo.onedrive.container.items.DriveItem;
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

import static io.netty.handler.codec.http.HttpMethod.GET;
import static java.net.HttpURLConnection.HTTP_OK;

public class DriveItemPager extends AbstractPager<DriveItem[]> {
	protected DriveItemPager(@NotNull RequestTool requestTool, @NotNull DriveItemPage page) {
		super(requestTool, page);
	}

	public static DriveItemPager deserialize(@NotNull Client client, @NotNull JsonParser parser, boolean autoClose)
			throws IOException {
		DriveItemPage itemPage = DriveItemPage.deserialize(client, parser, autoClose);
		return new DriveItemPager(client.requestTool(), itemPage);
	}

	@SneakyThrows(URISyntaxException.class)
	public static @NotNull DriveItem[] deserializeRecursive(final @NotNull Client client, @NotNull JsonParser parser,
															boolean autoClose) throws IOException {
		@Nullable URI nextLink;
		@NotNull ArrayList<DriveItem> items = new ArrayList<>();
		@Nullable JsonParser currentParser = parser;

		do {
			@Nullable ResponseFuture responseFuture = null;
			nextLink = null;

			while (currentParser.nextToken() != JsonToken.END_OBJECT) {
				String currentName = currentParser.getCurrentName();
				currentParser.nextToken();

				switch (currentName) {
					case "@odata.nextLink":
						nextLink = new URI(currentParser.getText());
						responseFuture = client.requestTool().doAsync(GET, nextLink);
						break;
					case "@odata.deltaLink":
						break;
					case "value":
						while (currentParser.nextToken() != JsonToken.END_ARRAY) {
							items.add(AbstractDriveItem.deserialize(client, currentParser, false));
						}
						break;
					case "@odata.context":
						// TODO
						break;
					default:
						throw new IllegalStateException(
								"Unknown attribute detected in DriveItemPager : " + currentName);
				}
			}

			if (responseFuture != null) {
				responseFuture.syncUninterruptibly();
				if (parser != currentParser) currentParser.close();
				currentParser = RequestTool.jsonFactory.createParser(responseFuture.getNow());
				currentParser.nextToken();
			}
		} while (nextLink != null);

		if (currentParser != parser) currentParser.close();
		if (autoClose) parser.close();

		return items.toArray(new DriveItem[0]);
	}

	@Override public @NotNull Iterator<DriveItem[]> iterator() {return new ItemPageIterator(requestTool, page);}


	public static class DriveItemPage extends Page<DriveItem[]> {
		DriveItemPage(@Nullable URI nextLink, @Nullable URI deltaLink, @NotNull DriveItem[] value) {
			super(nextLink, deltaLink, value);
		}

		@SneakyThrows(URISyntaxException.class)
		public static DriveItemPage deserialize(@NotNull Client client, @NotNull JsonParser parser, boolean autoClose)
				throws IOException {
			@Nullable URI nextLink = null;
			@Nullable URI deltaLink = null;
			@NotNull ArrayList<DriveItem> values = new ArrayList<>();

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
							values.add(AbstractDriveItem.deserialize(client, parser, false));
						}
						break;
					case "@odata.context":
						// TODO
						break;
					case "@odata.count":
						// TODO
						break;
					default:
						throw new IllegalStateException(
								"Unknown attribute detected in DriveItemPager : " + currentName);
				}
			}

			if (autoClose) parser.close();
			return new DriveItemPage(nextLink, deltaLink, values.toArray(new DriveItem[0]));
		}
	}


	class ItemPageIterator extends PageIterator<DriveItem[]> {
		ItemPageIterator(@NotNull RequestTool requestTool, @NotNull Page<DriveItem[]> currentPage) {
			super(requestTool, currentPage);
		}

		@Override
		protected DriveItemPage parse(@NotNull ResponseFuture responseFuture) throws ErrorResponseException {
			return requestTool.parseDriveItemPageAndHandle(
					responseFuture.response(), responseFuture.getNow(), HTTP_OK);
		}
	}
}
