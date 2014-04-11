package simplesolutions.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import simplesolutions.dependencyserver.impl.HttpServer;

/**
 * Listens for client connections.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 */
public final class HttpPortListener extends Thread {
	public final int port;
	private ServerSocket socket;
	private HttpServer myServer;

	/**
	 * Creates the TCP server.
	 * 
	 * @param port
	 *            to listen
	 */
	public HttpPortListener(int port, HttpServer s) {
		super("Http Port Listener");
		this.port = port;
		this.myServer = s;
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shuts the listener down.
	 * 
	 * Note: I should have a list with all active threads so that I could kill
	 * them individually before destroying the listener, but recently the Java
	 * Garbage collector does that automatically for us as soon as all threads
	 * return, so I just need to worry with breaking the while(trues) kind of
	 * loops. Lesson: Never use while(true) loops, use
	 * while(socket.isNotClosed()) type of loops instead.
	 */
	public void shutdown() {
		try {
			if (!socket.isClosed())
				this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method called when the thread starts, listens to port for client
	 * connections.
	 */
	public void run() {
		while (socket != null && !socket.isClosed()) {
			ConnectionThread t = null;
			try {
				Socket newConnection = this.socket.accept();
				BufferedReader inFromClient = new BufferedReader(
						new InputStreamReader(newConnection.getInputStream()));
				DataOutputStream outToClient = new DataOutputStream(
						newConnection.getOutputStream());
				t = new ConnectionThread(inFromClient, outToClient,
						newConnection);
				t.start();
			} catch (IOException e) {
				e.getMessage();
			}
		}
	}

	/**
	 * This thread handles each client connection individually getting the
	 * requested URL from the HTTP headers and asking the HTTP Server the
	 * page/HTML it should delivered as response to the client's request.
	 * 
	 * @author NeMewSys
	 * 
	 */
	private class ConnectionThread extends Thread {
		private BufferedReader in;
		private DataOutputStream out;
		private Socket conn;

		public ConnectionThread(BufferedReader in, DataOutputStream out,
				Socket conn) {
			super("Connection Thread");
			this.in = in;
			this.out = out;
			this.conn = conn;
		}

		/**
		 * Reads the header sent by the client and parses it to get the
		 * requested URL.
		 */
		public void run() {
			try {
				// This timeout variable is used to count the number of retrials
				// to get the HTTP headers from the client.
				int timeOut = 0;
				String requestHeader = "";
				while (conn.isConnected()) {
					if (in.ready()) {
						String tmp = in.readLine();
						if (tmp.length() != 0) {
							requestHeader += tmp + "\n";
							timeOut = 0;
						} else {
							String url = getRequestedUrl(requestHeader);
							out.writeBytes(myServer.getPage(url));
							break;
						}
					} else if (timeOut >= 3) { // Severe connection problems
												// detected
						out.writeBytes(myServer.errorPage());
						break;
					} else {
						timeOut++;
						Thread.sleep(100); // wait a bit...
					}
				}
				conn.close();
			} catch (Exception e) {
				System.err.println("Connection canceled.");
				e.printStackTrace();
			}
		}

		/**
		 * Parses the HTTP header to get the requested URL.
		 * 
		 * @param requestHeader
		 * @return
		 */
		private String getRequestedUrl(String requestHeader) {
			String[] lines = requestHeader.split("\n");
			return lines[0].split(" ")[1];
		}
	}
}
