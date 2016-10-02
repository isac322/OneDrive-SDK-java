package org.OneDriveSync.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * TODO: add javadoc
 * Created by isac322 on 16. 9. 29.
 */
public final class AuthServer {
	private final Semaphore authLock;
	private HttpServer server = null;
	private String authCode;

	/**
	 * TODO: flexible port and address.
	 */
	public AuthServer(Semaphore authLock) {
		this.authLock = authLock;

		try {
			authLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Lock Error In " + this.getClass().getName());
		}

		try {
			server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
			server.createContext("/", new AuthHandler());
			server.setExecutor(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		if (server != null) server.start();
	}

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
			String response = "<script type='text/javascript'>window.close()</script>";
			httpExchange.sendResponseHeaders(200, response.length());
			OutputStream os = httpExchange.getResponseBody();
			os.write(response.getBytes());
			os.close();

			Map<String, List<String>> queryMap = URLQueryParser.splitQuery(httpExchange.getRequestURI());

			if (queryMap.containsKey("code")) {
				authCode = queryMap.get("code").get(0);
			} else if (queryMap.containsKey("error")) {
				throw new IOException("Wrong Login Info");
			} else {
				throw new IOException("Unrecognized OneDrive Server Error");
			}

			authLock.release();
		}
	}
}