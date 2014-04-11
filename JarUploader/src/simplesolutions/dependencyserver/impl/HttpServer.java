package simplesolutions.dependencyserver.impl;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
	 * @param port
	 *            the port number
	 */
	public HttpServer(int port) {
		super("Http Server");
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
	 * @param out
	 * @return
	 */
	public void getPackageFile(String url, DataOutputStream out) {
		try {
			String[] packageNameManifest = url.split("/");
			String packagePath = Main.getJarRegistry().getJarProvidingPackage(
					packageNameManifest[1]);
			if (packagePath == null)
				out.writeBytes(responseHeader);
			else {
				String fileName = packagePath.split(Main.getJarsFolder())[1];
				/*
				 * Let us send the file to the client.
				 */
				out.writeBytes("HTTP/1.1 200 OK\r\nExpires:	-1\r\nContent-Type: application/force-download\r\nContent-Disposition: attachment; filename=\""
						+ fileName + "\"\r\nConnection:	close\r\n\r\n");
				InputStream istream = new FileInputStream(packagePath);
				final byte[] buffer = new byte[1024 * 8];
				while (true) {
					final int len = istream.read(buffer);
					if (len <= 0) {
						break;
					}
					out.write(buffer, 0, len);
				}
				istream.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Closes the server.
	 */
	public void close() {
		listener.shutdown();
	}
}
