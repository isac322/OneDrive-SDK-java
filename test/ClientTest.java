import junit.framework.TestCase;
import org.OneDriveSync.Client;
import org.OneDriveSync.container.Drive;
import org.OneDriveSync.utils.OneDriveRequest;
import org.network.HttpsResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 10. 2.
 */
public class ClientTest extends TestCase {
	private static Client client;

	private static Client getClient() {
		if (client == null) {
			final String clientId = "f21d2eff-49e2-4a10-a515-4a077f23c694";
			final String[] scope = {"onedrive.readwrite", "offline_access"};
			final String redirectURL = "http://localhost:8080/";
			final String clientSecret = "mwsydWThCRbJJZZ7uep5GLm";

			client = new Client(clientId, scope, redirectURL, clientSecret);
		}

		return client;
	}

	public void testLogin() throws IOException {
		getClient();

		assertNotNull(client.getAccessToken());
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

	public void testLogout() {
		getClient();

		client.logout();

		assertFalse(client.isLogin());
	}

	public void testItem() throws IOException {
		getClient();

		HttpsResponse response = OneDriveRequest.doGet("/drive/root?expand=children", client.getAccessToken());
		System.out.println(response.getContentString());
	}

	public void testDrive() {
		getClient();

		Drive defaultDrive = client.getDefaultDrive();

		System.out.println(defaultDrive);
	}
}
