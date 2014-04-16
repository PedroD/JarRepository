package lumina.kernel.osgi.shell;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import lumina.kernel.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Lumina specific telnet shell that interfaces OSGi Bundle Services.
 * 
 * Note: Only one connection is allowed.
 */
public final class OSGiShell extends Thread {
	private int port;
	private static String app = "[Lumina Shell] ";
	private ServerSocket serverSocket;

	public OSGiShell(int port) {
		this.port = port;
	}

	public void Start() {
		super.start();
	}

	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			message("Error opening server on port " + port + ".\n"
					+ e.getMessage());
		}
		message("OSGi shell running as raw socket in port " + port);
		while (!serverSocket.isClosed()) {
			try {
				Socket newClient = serverSocket.accept();
				new AcceptTelnetClient(newClient);
			} catch (IOException e) {
				message("Error accepting client on port " + port + ".\n"
						+ e.getMessage());
			}
		}
	}

	public void Stop() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			message("Error closing server.\n" + e.getMessage());
		}
	}

	private void message(String s) {
		Logger.getInstance().log(LogService.LOG_DEBUG, app + s);
	}

	private class AcceptTelnetClient extends Thread {
		private Socket clientSocket;
		private DataInputStream din;
		private DataOutputStream dout;

		public AcceptTelnetClient(Socket s) {
			clientSocket = s;
			message("Client Connected (" + s.getInetAddress() + ").");
			try {
				din = new DataInputStream(clientSocket.getInputStream());
				dout = new DataOutputStream(clientSocket.getOutputStream());
				sayHello();
				start();
			} catch (IOException e) {
				message("Error communicating with the connected client.\n"
						+ e.getMessage());
			}
		}

		public void run() {
			String input;
			boolean loop = true;
			try {
				String[] cmd = null;
				while (loop) {
					sayPrompt();
					input = (new BufferedReader(new InputStreamReader(din)))
							.readLine();
					if (input == null) // Connection lost
						break;
					else if (input.equals("..") && cmd != null) {
						// Do nothing, reuse previous command
					} else if ((input.startsWith(". ") || input.equals("."))
							&& cmd != null) {
						String tmp = cmd[0];
						cmd = input.split(" ");
						cmd[0] = tmp;
					} else
						cmd = input.split(" ");
					if (cmd.length == 0 || cmd[0] == null || cmd[0].equals(""))
						continue;
					if (cmd[0].equals("exit") || cmd[0].equals("quit")
							|| cmd[0].equals("q")) {
						loop = false;
					} else if (cmd[0].equals("install") && cmd.length == 2) {
						install(cmd);
					} else if (cmd[0].equals("uninstall") && cmd.length == 2) {
						uninstall(Long.parseLong(cmd[1]));
					} else if (cmd[0].equals("update") && cmd.length == 2) {
						update(Long.parseLong(cmd[1]));
					} else if (cmd[0].equals("refresh")) {
						getPackageAdminService().refreshPackages(null);
					} else if (cmd[0].equals("start") && cmd.length == 2) {
						startBundle(Long.parseLong(cmd[1]));
					} else if (cmd[0].equals("stop") && cmd.length == 2) {
						stopBundle(Long.parseLong(cmd[1]));
					} else if (cmd[0].equals("resolve")) {
						if (cmd.length == 2)
							resolveBundle(Long.parseLong(cmd[1]));
						else
							resolveBundle(-1);
					} else if (cmd[0].equals("info") && cmd.length == 2) {
						info(Long.parseLong(cmd[1]));
					} else if (cmd[0].equals("ls") || cmd[0].equals("list")) {
						list(cmd.length > 1);
					} else if (cmd[0].equals("drivers")) {
						// getInstalledDrivers();
					} else {
						sayHelp();
					}
				}
			} catch (IOException e) {
				message("Connection error.\n" + e.getMessage());
			}
			try {
				message("Client Disconnected (" + clientSocket.getInetAddress()
						+ ").");
				clientSocket.close();
			} catch (IOException e) {
				message("Error closing connection.\n" + e.getMessage());
			}
		}

		// private void getInstalledDrivers() {
		// String[] drivers = DriverRegistry.getInstance()
		// .getRegisteredServicesNames();
		// if (drivers.length == 0)
		// say("none.");
		// else
		// for (String d : drivers)
		// say(d);
		// }

		private void update(long id) {
			Bundle b = getLuminaBundleContext().getBundle(id);
			try {
				if (b != null) {
					b.update();
					say("Done!");
				} else
					sayNoBundle();
			} catch (BundleException e) {
				say("Could not update bundle " + b.getBundleId() + "\n\r"
						+ e.getMessage());
			}
		}

		private void uninstall(long id) {
			Bundle b = getLuminaBundleContext().getBundle(id);
			if (b == null) {
				sayNoBundle();
				return;
			}
			try {
				b.uninstall();
				say("Done!");
			} catch (BundleException e) {
				say("Could not uninstall the bundle!\n\r" + e.getMessage());
				e.printStackTrace();
			}
		}

		private void resolveBundle(long id) {
			Bundle b = getLuminaBundleContext().getBundle(id);
			boolean success = false;
			if (b == null) {
				success = getPackageAdminService().resolveBundles(null);
			} else {
				success = getPackageAdminService().resolveBundles(
						new Bundle[] { b });
			}
			if (success)
				say("Bundle resolved successfully!");
			else
				say("Cannot resolve bundle!");
		}

		private void stopBundle(long id) {
			Bundle b = getLuminaBundleContext().getBundle(id);
			if (b == null) {
				sayNoBundle();
				return;
			}
			try {
				b.stop();
			} catch (BundleException e) {
				say("Could not stop the bundle!\n\r" + e.getMessage());
				e.printStackTrace();
			}
		}

		private void startBundle(long id) {
			Bundle b = getLuminaBundleContext().getBundle(id);
			if (b == null) {
				sayNoBundle();
				return;
			}
			try {
				b.start();
			} catch (BundleException e) {
				say("Could not start the bundle!\n\r" + e.getMessage());
				e.printStackTrace();
			}
		}

		private void info(long id) {
			Bundle b = getLuminaBundleContext().getBundle(id);
			if (b == null) {
				sayNoBundle();
			}
			say("Bundle Name: " + b.getSymbolicName());
			say("Bundle Id: " + id);
			say("Bundle State: " + getStateString(b.getState()));
			say("Bundle Location: " + b.getLocation());
			say("Bundle Version: " + b.getVersion());
			say("-------------------------------------");
			say("Bundle's Services:");
			ServiceReference[] srs = b.getRegisteredServices();
			if (srs == null)
				say("   none.");
			else {
				for (ServiceReference sr : srs)
					say("   " + (isServiceBeingUsed(b, sr) ? "[*] " : "[ ] ")
							+ sr.toString());
				say("[*] = being used | [ ] = not used");
			}
			say("");
		}

		private void sayNoBundle() {
			say("No such bundle!");
		}

		private boolean isServiceBeingUsed(Bundle b, ServiceReference sr) {
			ServiceReference[] srs = b.getServicesInUse();
			if (srs == null)
				return false;
			for (ServiceReference s : srs)
				if (s.equals(sr))
					return true;
			return false;
		}

		private void install(String[] cmd) {
			BundleContext bc = getLuminaBundleContext();
			try {
				Bundle b = bc.installBundle(cmd[1]);
				say("Installation successful! New Bundle Id: "
						+ b.getBundleId());
			} catch (BundleException e) {
				say("Error installing. Make sure the URL is correct.\n\r"
						+ e.getMessage());
			}
		}

		private void list(boolean detail) {
			Bundle[] list = getLuminaBundleContext().getBundles();
			say("Id | State | Name" + (detail ? " | Location" : ""));
			for (Bundle b : list) {
				say(b.getBundleId() + " | " + getStateString(b.getState())
						+ " | " + b.getSymbolicName()
						+ (detail ? " | " + b.getLocation() : ""));
			}
			say("");
		}

		private void sayHelp() throws IOException {
			dout.writeBytes("Invalid Command!\n\r\n\r"
					+ "Usage:\n\r"
					+ "install <Relative or Absolute local path/P2 or Maven repo. URL>\n\r"
					+ "uninstall <Bundle Id>\n\r" + "update <Bundle Id>\n\r"
					+ "refresh (forces OSGi to re-wire updated bundles)\n\r"
					+ "start <Bundle Id>\n\r" + "stop <Bundle Id>\n\r"
					+ "resolve [<Bundle Id>] (no parameters = resolve all)\n\r"
					+ "info <Bundle Id>\n\r" + "list [d|-d|detail]\n\r"
					+ "drivers\n\r" + "quit\n\r"
					+ ". (repeat previous command with new parameters)\n\r"
					+ ".. (repeat previous command)\n\r\n\r");
		}

		private void sayPrompt() throws IOException {
			dout.writeBytes("Lumina::OSGi> ");
		}

		private void sayHello() throws IOException {
			dout.writeBytes("=======================================\n\r"
					+ "         Lumina OSGi Shell\n\r"
					+ "=======================================\n\r\n\r");
		}

		private void say(String s) {
			try {
				dout.writeBytes(s + "\n\r");
			} catch (IOException e) {
			}
		}

		private String getStateString(int state) {
			switch (state) {
			case Bundle.INSTALLED:
				return "INSTALLED";
			case Bundle.RESOLVED:
				return "RESOLVED";
			case Bundle.STARTING:
				return "STARTING";
			case Bundle.ACTIVE:
				return "ACTIVE";
			case Bundle.STOPPING:
				return "STOPPING";
			default:
				return "UNKNOWN";
			}
		}

		private BundleContext getLuminaBundleContext() {
			return FrameworkUtil.getBundle(OSGiShell.class).getBundleContext();
		}

		private PackageAdmin getPackageAdminService() {
			return (PackageAdmin) getLuminaBundleContext().getService(
					getLuminaBundleContext().getServiceReference(
							PackageAdmin.class.getName()));
		}
	}
}
