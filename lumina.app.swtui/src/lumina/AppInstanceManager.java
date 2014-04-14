package lumina;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import lumina.kernel.Logger;

import org.osgi.service.log.LogService;

/**
 * This class implements a way to guarantee that only one instance of an
 * application runs at a time. To achieve this, call tryStart() at application
 * startup. If it returns false, that means that startup should be aborted
 * because another instance was found running.
 * <p>
 * Implementation notes:
 * <ul>
 * <li>The possibilities for implementing this in Java are very limited. The
 * chosen implementation involves leaving a thread listening on a known TCP
 * port.</li>
 * <li>We must try hard to avoid false positives (thinking there is another
 * instance running when there isn't) because that would effectively stop the
 * application from starting.</li>
 * </ul>
 * <p>
 * IMPORTANT!<br>
 * Every application should change the TCP_PORT, CHALLENGE_MSG and RESPONSE_MSG
 * constants to a unique value.
 */
public final class AppInstanceManager {

	// Port to listen on. Any high port will do.
	private static final int TCP_PORT = 47861;

	// Challenge/response messages. Must end with newline
	private static final String CHALLENGE_MSG = "LUMINA NEWCOMER\n";
	private static final String RESPONSE_MSG = "LUMINA OLDTIMER\n";

	private static final int CLIENT_TIMEOUT_MILLIS = 5000;

	// FIXME: Should these times be equal?
	private static final int TIMEOUT_MILLIS = 2000;

	private static Runnable callback;

	/**
	 * Prevents instantiation.
	 */
	private AppInstanceManager() {
	}

	private static void runCallback() {
		try {
			if (callback != null)
				callback.run();
		} catch (Exception ex) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"AppInstanceManager: callback threw exception", ex);
		}
	}

	/**
	 * Registers a Runnable to be called whenever a new instance tries to start.
	 * Note: only one callback can be registered at one time.
	 * 
	 * @param callback
	 *            the runnable
	 */
	public static void registerCallback(Runnable callback) {
		AppInstanceManager.callback = callback;
	}

	/**
	 * Tries to determine if an instance of the application is already running.
	 * If another instance is found (listening on a known port), notifies it and
	 * returns false. If not, then it starts listening on that port itself, and
	 * returns true. It is very important that this method be conservative,
	 * because if it wrongly returns false the application will fail to start.
	 * Therefore: - only return false if it is certain that another instance was
	 * found and successfully notified - be careful not to block forever on
	 * driver operations - if any exceptions occur, return true.
	 * 
	 * @return true if no running instance was found; startup should proceed.
	 *         false if a running instance was found and successfully notified;
	 *         startup should abort.
	 */
	public static boolean tryStart() {
		try {
			// Try to open network driver
			final ServerSocket serverSocket = new ServerSocket(TCP_PORT, 5,
					InetAddress.getLocalHost());

			// Success. This means we're the first instance; start a thread to
			// listen
			// on the driver for new instance message
			Logger.getInstance().log(LogService.LOG_INFO,
					"AppInstanceManager: Listening on port " + TCP_PORT);

			Runnable listener = new Runnable() {
				public void run() {
					while (true) {
						if (serverSocket.isClosed()) {
							Logger.getInstance()
									.log(LogService.LOG_INFO,
											"AppInstanceManager: Listener exiting due to closed driver");
							return;
						}
						try {
							Socket clientSocket = serverSocket.accept();
							// Don't block forever waiting for the other party
							// to send the
							// challenge. Throws SocketTimeoutException if the
							// timeout expires
							clientSocket.setSoTimeout(CLIENT_TIMEOUT_MILLIS);

							BufferedReader in = new BufferedReader(
									new InputStreamReader(
											clientSocket.getInputStream()));
							String message = in.readLine();
							if (message != null
									&& message.trim().equals(
											CHALLENGE_MSG.trim())) {
								Logger.getInstance()
										.log(LogService.LOG_INFO,
												"AppInstanceManager: New instance knocked, responding.");

								OutputStream out = clientSocket
										.getOutputStream();
								out.write(RESPONSE_MSG.getBytes());
								clientSocket.close();

								runCallback();
							} else {
								clientSocket.close();
							}
						} catch (SocketTimeoutException ex) {
							// nothing to do
						} catch (Exception ex) {
							Logger.getInstance()
									.log(LogService.LOG_ERROR,
											"AppInstanceManager: Listener exiting due to error",
											ex);
							return;
						}
					}
				}
			};
			Thread t = new Thread(listener, "AppInstanceManager listener");
			t.setDaemon(true);
			t.start();

			return true;
		} catch (UnknownHostException ex) {
			// should never happen as we're binding to localhost
			Logger.getInstance().log(LogService.LOG_ERROR, "Unknown host", ex);
			return true;
		} catch (IOException ex) {
			// Listening on the driver failed. This probably means that another
			// instance is
			// already listening on it, lets try talk to it.

			try {
				Socket clientSocket = new Socket(InetAddress.getLocalHost(),
						TCP_PORT);
				// Don't block forever waiting for the other party to send the
				// response. Throws SocketTimeoutException if the timeout
				// expires
				clientSocket.setSoTimeout(TIMEOUT_MILLIS);

				// send the challenge
				OutputStream out = clientSocket.getOutputStream();
				out.write(CHALLENGE_MSG.getBytes());

				// read the response
				boolean success;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				String message = in.readLine();
				if (message != null
						&& message.trim().equals(RESPONSE_MSG.trim()))
					success = true;
				else
					success = false;
				clientSocket.close();

				if (success) {
					Logger.getInstance()
							.log(LogService.LOG_INFO,
									"AppInstanceManager: Successfully notified first instance.");
					return false;
				} else {
					Logger.getInstance()
							.log(LogService.LOG_INFO,
									"AppInstanceManager: Failed to notify first instance.");
					return true;
				}
			} catch (SocketTimeoutException ex2) {
				Logger.getInstance()
						.log(LogService.LOG_ERROR,
								"AppInstanceManager: Failed to notify other instance due to timeout");
				return true;
			} catch (Exception ex2) {
				Logger.getInstance()
						.log(LogService.LOG_ERROR,
								"AppInstanceManager: Failed to notify other instance due to error",
								ex2);
				return true;
			}
		}
	}

}
