package org.onedrive.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.Nullable;
import org.onedrive.exceptions.InternalException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

/**
 * {@// TODO: add javadoc}
 *
 * @author <a href="mailto:yoobyeonghun@gmail.com" target="_top">isac322</a>
 */
public final class AuthServer {
	private final Semaphore authLock;
	private HttpServer server = null;
	private String authCode;

	/**
	 * {@// TODO: flexible port and address.}
	 */
	public AuthServer(Semaphore authLock) {
		this.authLock = authLock;

		try {
			authLock.acquire();
		}
		catch (InterruptedException e) {
			throw new InternalException("Lock error in AuthServer constructor.", e);
		}

		try {
			server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
			server.createContext("/", new AuthHandler());
			server.setExecutor(null);
		}
		catch (IOException e) {
			e.printStackTrace();
			// FIXME: custom exception
		}
	}

	public void start() {
		if (server != null) server.start();
	}

	@Nullable
	public String close() {
		if (server != null) {
			server.stop(0);
			return authCode;
		}
		return null;
	}

	private class AuthHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			byte[] response =
					"<script type='text/javascript'>window.close()</script>".getBytes(StandardCharsets.UTF_8);
			httpExchange.sendResponseHeaders(200, response.length);
			OutputStream os = httpExchange.getResponseBody();
			os.write(response);
			os.close();

			String[] query = httpExchange.getRequestURI().getQuery().split("=");

			switch (query[0]) {
				case "code":
					authCode = query[1];
					break;
				case "error":
					// FIXME: custom exception
					throw new IOException("Wrong Login Info");
				default:
					// FIXME: custom exception
					throw new IOException("Unrecognized OneDrive Server Error");
			}

			authLock.release();
		}
	}
}