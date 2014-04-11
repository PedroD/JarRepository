package simplesolutions.dependencyserver.impl;

import simplesolutions.util.HttpPortListener;

/**
 * This is a HTTP Server, it has a listener that listens for port 80 and manages
 * the installed pages. Pages are installed as OSGi bundles (plug-ins), and this
 * HTTP Server maintains a list of installed pages.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 */
public final class HttpServer extends Thread {
	/*
	 * The listener that will accept client connections on port 80
	 */
	private HttpPortListener listener;
	/*
	 * The HTTP 200 response header sent in every bundle page
	 */
	private static String responseHeader = "HTTP/1.1 200 OK\r\nExpires:	-1\r\nContent-Type:	text/html; charset=ISO-8859-1\r\nConnection:	close\r\n\r\n";

	/**
	 * Instantiates a new http server.
	 *
	 * @param port the port number
	 */
	public HttpServer(int port) {
		listener = new HttpPortListener(port, this);
	}

	/**
	 * Implementing Thread.run()
	 */
	public void run() {
		try {
			listener.start();
			listener.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If the listener's connection dies this method is executed.
	 * 
	 * @return
	 */
	public String errorPage() {
		return responseHeader + "<h2>Error</h2>";
	}

	/**
	 * Given a relative URL (such as /, /SmartCampus, etc.) this method looks
	 * for an installed bundle accepting this URL and asks him to render a page
	 * (in HTML).
	 * 
	 * @param url
	 * @return
	 */
	public String getPage(String url) {
		return url;
	}

	/**
	 * Closes the server.
	 */
	public void close() {
		listener.shutdown();
	}
}
