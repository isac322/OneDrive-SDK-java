import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import junit.framework.TestCase;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.onedrive.Client;
import org.onedrive.container.Drive;
import org.onedrive.container.facet.AudioFacet;
import org.onedrive.container.items.*;
import org.onedrive.container.items.pointer.PathPointer;
import org.onedrive.exceptions.ErrorResponseException;
import org.onedrive.network.AsyncHttpsResponseHandler;
import org.onedrive.network.DirectByteInputStream;
import org.onedrive.network.HttpsClientHandler;
import org.onedrive.network.legacy.HttpsRequest;
import org.onedrive.network.legacy.HttpsResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * TODO: add javadoc
 */
public class ClientTest extends TestCase {
	private static Client client;
	ObjectMapper mapper = new ObjectMapper();

	private static Client getClient() {
		if (client == null) {
			final String clientId = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";
			final String[] scope = {"onedrive.readwrite", "offline_access", "onedrive.appfolder"};
			final String redirectURL = "http://localhost:8080/";
			final String clientSecret = "XXXXXXXXXXXXXXXXXXXXXXX";

			client = new Client(clientId, scope, redirectURL, clientSecret);
		}

		return client;
	}

	public void testLogin() {
		getClient();

		assertNotNull(client.getFullToken());
		assertNotNull(client.getAuthCode());
		assertNotNull(client.getRefreshToken());

		assertTrue(client.isLogin());
	}

	public void testExpiration() throws InterruptedException {
		getClient();

		assertTrue(client.isLogin());

		TimeUnit.SECONDS.sleep(client.getExpirationTime());

		assertFalse(client.isExpired());
	}

	public void testLogout() throws ErrorResponseException {
		getClient();

		client.logout();

		assertFalse(client.isLogin());
	}

	public void testItem() {
		getClient();

		// Test Files (shared by someone)
		HttpsResponse response = client.requestTool().newRequest("/drive/items/485BEF1A80539148!115").doGet();
		System.out.println(response.getContentString());
	}

	public void testGetFileItem() throws ErrorResponseException {
		getClient();

		BaseItem file = client.getItem("D4FD82CA6DF96A47!22159");
		System.out.println(file.getName());
		System.out.println(file.getId());
	}

	public void testSharedItem() throws ErrorResponseException {
		getClient();

		BaseItem[] shared = client.getShared();

		for (BaseItem item : shared) {
			System.out.println(item.getId());
			System.out.println(item.getName());
		}
	}

	public void testDrives() throws IOException, ErrorResponseException {
		getClient();

		for (Drive drive : client.getAllDrive()) {
			System.out.println(drive.getDriveType());
			System.out.println(drive.getId());
			System.out.println(drive.getTotalCapacity());
		}
	}

	public void testDrive() throws ErrorResponseException {
		getClient();

		Drive defaultDrive = client.getDefaultDrive();

		System.out.println(defaultDrive);
	}

	public void testPackage() {
		getClient();

		HttpsResponse response = client.requestTool().newRequest("/drive/items/D4FD82CA6DF96A47!2104").doGet();
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
	}

	public void testRemoteItem() {
		getClient();

		HttpsResponse response = client.requestTool()
				.newRequest("/drives/485bef1a80539148/items/485BEF1A80539148!115?expand=children").doGet();
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		//System.out.println(response.getContentString());
	}

	public void testGetItem() throws ErrorResponseException {
		getClient();

		for (int i = 0; i < 10; i++) {
			// BOJ
			FolderItem folder = client.getFolder("D4FD82CA6DF96A47!14841");
			System.out.println(folder.getName());
			for (BaseItem item : folder) {
				System.out.println(item.getName());
			}
		}
	}

	public void testRoot() {
		getClient();

		HttpsResponse response = client.requestTool().newRequest("/drive/root:/?expand=children").doGet();
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
	}

	public void testRootDir() throws ErrorResponseException {
		getClient();

		FolderItem rootDir = client.getRootDir();
		System.out.println(rootDir.getId());
		System.out.println(rootDir.getCTag());
		System.out.println(rootDir.getETag());
		System.out.println(rootDir.getName());
		System.out.println(rootDir.getSize());
		System.out.println(rootDir.getFolderChildren().get(1).getAllChildren().get(0)
				.getParentReference()
				.getPathPointer());
		System.out.println(rootDir.getFolderChildren().get(1));
	}

	private void dfs(FolderItem folders, String tab) throws IOException, ErrorResponseException {
		StringBuilder builder2 = new StringBuilder(tab).append(folders.getName()).append('\t');

		if (folders instanceof RemoteFolderItem) builder2.append(" Remote Item");
		if (folders.getSearchResult() != null) builder2.append(" Search result");
		if (folders.getShared() != null) builder2.append(" Shared");

		System.out.println(builder2.toString());

		tab += '\t';
		for (BaseItem item : folders) {
			if (item instanceof FolderItem) {
				FolderItem folder = (FolderItem) item;

				assertNotNull(folder.getParentReference());
				assertEquals(folder.childrenCount(), folder.getAllChildren().size());
				dfs(folder, tab);
			}
			else if (item instanceof FileItem) {
				StringBuilder builder = new StringBuilder(tab).append(item.getName()).append('\t');
				assertNotNull(item.getParentReference());

				FileItem file = (FileItem) item;
				if (file.getAudio() != null) builder.append(" Audio");
				if (file.getImage() != null) builder.append(" Image");
				if (file.getLocation() != null) builder.append(" has Location");
				if (file.getPhoto() != null) builder.append(" Photo");
				if (file.getVideo() != null) builder.append(" Video");
				if (file.getSearchResult() != null) builder.append(" Search result");
				if (file.getShared() != null) builder.append(" Shared");

				System.out.println(builder.toString());

				String path = file.getParentReference().getPathPointer().toString().substring(1);
				file.download(Paths.get(path));
			}
			else if (item instanceof PackageItem) {
				System.out.println(tab + item.getName() + "\tPackage");
			}
			else {
				throw new RuntimeException("Unsupported type item.");
			}
		}
	}

	private void printDFS(FolderItem folders, String tab) throws IOException {
		StringBuilder builder2 = new StringBuilder(tab).append(folders.getName()).append('\t');

		if (folders instanceof RemoteFolderItem) builder2.append(" Remote Item");
		if (folders.getSearchResult() != null) builder2.append(" Search result");
		if (folders.getShared() != null) builder2.append(" Shared");

		System.out.println(builder2.toString());

		tab += '\t';
		for (BaseItem item : folders) {
			if (item instanceof FolderItem) {
				FolderItem folder = (FolderItem) item;

				printDFS(folder, tab);
			}
			else if (item instanceof FileItem) {
				StringBuilder builder = new StringBuilder(tab).append(item.getName()).append('\t');

				FileItem file = (FileItem) item;
				if (file.getAudio() != null) builder.append(" Audio");
				if (file.getImage() != null) builder.append(" Image");
				if (file.getLocation() != null) builder.append(" has Location");
				if (file.getPhoto() != null) builder.append(" Photo");
				if (file.getVideo() != null) builder.append(" Video");
				if (file.getSearchResult() != null) builder.append(" Search result");
				if (file.getShared() != null) builder.append(" Shared");

				System.out.println(builder.toString());
			}
			else if (item instanceof PackageItem) {
				System.out.println(tab + item.getName() + "\tPackage");
			}
			else {
				throw new RuntimeException("Unsupported type item.");
			}
		}
	}

	public void testRecursiveTravel() throws IOException, ErrorResponseException {
		getClient();

		dfs(client.getRootDir(), "");
	}

	public void testSearch() {
		getClient();

		HttpsResponse response = client.requestTool()
				.newRequest("/drive/root/view.search?q=Gone%20in%20Six%20Characters").doGet();
		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
	}

	public void testJacksonWrite() throws JsonProcessingException, ErrorResponseException {
		getClient();

		BaseItem item = ((FolderItem) client.getRootDir().getAllChildren().get(1)).getAllChildren().get(0);


		ObjectMapper mapper = new ObjectMapper();
		String string = mapper.writeValueAsString(item.getParentReference());
		System.out.println(string);
	}

	public void testJson() throws IOException {
		String json = "{\n" +
				"  \"album\": \"string\",\n" +
				"  \"albumArtist\": \"string\",\n" +
				"  \"artist\": \"string\",\n" +
				"  \"bitrate\": 128,\n" +
				"  \"composers\": \"string\",\n" +
				"  \"copyright\": \"string\",\n" +
				"  \"disc\": 0,\n" +
				"  \"discCount\": 0,\n" +
				"  \"duration\": 567,\n" +
				"  \"genre\": \"string\",\n" +
				"  \"hasDrm\": false,\n" +
				"  \"isVariableBitrate\": false,\n" +
				"  \"title\": \"string\",\n" +
				"  \"track\": 1,\n" +
				"  \"trackCount\": 16,\n" +
				"  \"year\": 2014\n" +
				"}";

		AudioFacet audio = mapper.readValue(json, AudioFacet.class);
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(audio));
	}

	public void testCopy() throws JsonProcessingException, ErrorResponseException {
		getClient();

		FileItem file = client.getFile("D4FD82CA6DF96A47%2124988");
		FolderItem folder = client.getFolder("D4FD82CA6DF96A47!110");

		System.out.println(file.copyTo("/drive/root:/test folder"));
	}

	public void testCopy2() throws IOException {
		getClient();

		byte[] content = ("{\"parentReference\":{\"driveId\":\"D4FD82CA6DF96A47!107\"," +
				"\"id\":\"D4FD82CA6DF96A47!107\"}}").getBytes();

		System.out.println(new String(content));
		System.out.println(String.format("/drive/items/%s/action.copy", "D4FD82CA6DF96A47!10375"));

		HttpsResponse response = client.requestTool().postMetadata(
				String.format("/drive/items/%s/action.copy", "D4FD82CA6DF96A47!10375"),
				content);

		System.out.println(response.getCode());
		System.out.println(response.getMessage());
		System.out.println(response.getContentString());
		System.out.println(response.getHeader().get("Location"));

		String url = response.getHeader().get("Location").get(0);

		for (int i = 0; i < 100; i++) {
			HttpsResponse httpsResponse = client.requestTool().newRequest(new URL(url)).doGet();
			JsonNode jsonNode = mapper.readTree(httpsResponse.getContent());
			System.out.println(mapper.writeValueAsString(jsonNode));
		}
	}

	public void testCreateFolder() throws ErrorResponseException {
		getClient();

		FolderItem rootDir = client.getRootDir();

		@NotNull FolderItem testFolder = client.createFolder(new PathPointer("/"), "test");

		System.out.println(testFolder.getId() + '\t' + testFolder.getName() + '\t' + testFolder.childrenCount());
		testFolder.delete();

		@NotNull FolderItem test = rootDir.createFolder("test");
		System.out.println(test.getId() + '\t' + test.getName() + '\t' + test.childrenCount());
		test.delete();
	}

	public void testUpdate() throws ErrorResponseException {
		getClient();

		FileItem file = client.getFile("D4FD82CA6DF96A47!26026");

		file.setDescription("testtestsetsetsetsetsetsetsetsetset");

		file.refresh();
	}

	public void testUpdateRoot() throws ErrorResponseException {
		getClient();

		FolderItem root = client.getRootDir();

		root.refresh();
	}

	public void testUpdateAudioFacet() throws ErrorResponseException, JsonProcessingException {
		getClient();

		FileItem file = client.getFile("D4FD82CA6DF96A47!25997");

		file.setName("Roy Kim 로이킴 - 봄봄봄. -[ mymusicroad.net ].mp3");
		file.setDescription("fasdffadfasdgqwe");

		System.out.println();
		System.out.println(client.mapper().writeValueAsString(file));

		file.refresh();

		System.out.println();
		System.out.println(client.mapper().writeValueAsString(file));
	}

	public void testMove() throws ErrorResponseException {
		getClient();

		FileItem mp3 = client.getFile("D4FD82CA6DF96A47!25997");

		FolderItem doc = client.getFolder("D4FD82CA6DF96A47!110");
		FolderItem root = client.getRootDir();

		mp3.moveTo(doc.getId());
		mp3.moveTo(root);
		mp3.moveTo(doc.getPathPointer());
		mp3.moveTo(root.newReference());
	}

	public void testRootChildren() throws ErrorResponseException {
		getClient();

		FolderItem root = client.getRootDir();

		for (BaseItem item : root) {
			System.out.println(item.getName() + "\t" + item.getId());
		}
	}

	public void testNetty() throws URISyntaxException, InterruptedException {
		getClient();


		long allBefore = System.currentTimeMillis();
		System.out.println("Netty");
		EventLoopGroup group = new NioEventLoopGroup();
		ArrayList<ChannelFuture> futures = new ArrayList<>();

		for (int i = 0; i < 50; i++) {
			final long before = System.currentTimeMillis();
			HttpsClientHandler httpsClientHandler =
					client.requestTool().doAsync(HttpMethod.GET, "/drive/root:?expand=children");
			httpsClientHandler.addCloseListener(new AsyncHttpsResponseHandler() {
				@Override
				public void handle(DirectByteInputStream resultStream, @NotNull HttpResponse response) {
					try {
						FolderItem root = client.mapper().readValue(resultStream, FolderItem.class);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Netty takes " + (System.currentTimeMillis() - before));
				}
			});

			futures.add(httpsClientHandler.getBlockingCloseFuture());
		}
		for (ChannelFuture future : futures) {
			future.sync();
		}
		System.out.println("total : " + (System.currentTimeMillis() - allBefore));
		System.out.println();

		long sum = 0, now;
		System.out.println("Legacy:");
		for (int i = 0; i < 50; i++) {
			long before = System.currentTimeMillis();

			HttpsResponse httpsResponse =
					client.requestTool().newRequest("/drive/root:?expand=children").doGet();

			try {
				FolderItem root = client.mapper().readValue(httpsResponse.getContent(), FolderItem.class);
				now = System.currentTimeMillis();
				sum += (now - before);
				System.out.println("Legacy takes " + (now - before));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("total : " + sum);
		System.out.println();
	}

	public void testBOJLegacy() throws JsonProcessingException {
		getClient();

		HttpsResponse httpsResponse = client.requestTool()
				.newRequest("/drive/items/D4FD82CA6DF96A47!14841?expand=children")
				.doGet();
		Map<String, List<String>> header = httpsResponse.getHeader();
		for (val entry : header.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}

		System.out.println(httpsResponse.getContentString());
	}

	public void testChunk() throws URISyntaxException, InterruptedException, ErrorResponseException {
		getClient();

		long before = System.currentTimeMillis();
		System.out.println("send begin");

		final FolderItem[] items = new FolderItem[1];
		HttpsClientHandler clientHandler =
				client.requestTool().doAsync(HttpMethod.GET, "/drive/items/D4FD82CA6DF96A47!14841?expand=children",
						new AsyncHttpsResponseHandler() {
							@Override
							public void handle(DirectByteInputStream resultStream, HttpResponse response)
									throws ErrorResponseException {
								items[0] = client.requestTool().parseAndHandle(response, resultStream,
										HttpURLConnection.HTTP_OK, FolderItem.class);
							}
						});

		System.out.println("send done. " + (System.currentTimeMillis() - before));

		clientHandler.getBlockingCloseFuture().sync();

		System.out.println(items[0].isRoot());
		System.out.println(items[0].getId());
		System.out.println(items[0].getName());
		System.out.println(items[0].childrenCount());
		System.out.println(items[0].getAllChildren().size());
	}

	public void testOneDrivePath() throws ErrorResponseException {
		getClient();

		FolderItem rootDir = client.getRootDir();

		System.out.println(rootDir.getPathPointer());

		for (val object : rootDir) {
			System.out.println(object.getName() + '\t' + object.getId() + '\t' + object.getDriveId());
		}

		FileItem mp3 = rootDir.getFileChildren().get(2);
		client.copyItem(mp3.getPathPointer(), rootDir.getFolderChildren().get(0).getId());
	}

	public void testOneDriveCopy() throws ErrorResponseException {
		getClient();

		FolderItem rootDir = client.getRootDir();

		RemoteFolderItem testFiles = (RemoteFolderItem) rootDir.getFolderChildren().get(0);

		System.out.println(testFiles.getName() + '\t' + testFiles.getId() + '\t' + testFiles.getDriveId() + '\t' +
				testFiles.getRealDriveID());

		FileItem testTxt = rootDir.getFileChildren().get(1);
		System.out.println(testTxt.getName());
		client.copyItem(testTxt.getPathPointer(), testFiles.getPathPointer());

		/*
		testTxt.setName("newName");
		testTxt.refresh();

		System.out.println(testTxt.getName());
		System.out.println(testTxt.getPathPointer());
		*/
	}

	public void testRemoteItemChildrenFetching() throws ErrorResponseException {
		getClient();

		RemoteFolderItem item = (RemoteFolderItem) client.getItem("D4FD82CA6DF96A47!2578116");
		System.out.println(item.getAllChildren());

		item.refresh();
	}

	public void testPointer2URI() {
		getClient();

		HttpsRequest httpsRequest = client.requestTool().newRequest("/drive/root:/문서");
		HttpsResponse httpsResponse = httpsRequest.doGet();
		System.out.println(httpsResponse.getCode());
		System.out.println(httpsResponse.getMessage());
		System.out.println(httpsResponse.getContentString());
	}
}
