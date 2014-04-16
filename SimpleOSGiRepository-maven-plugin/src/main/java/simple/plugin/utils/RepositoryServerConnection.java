package simple.plugin.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Class that communicates with the server.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 */
public final class RepositoryServerConnection {

	/** The server's serverURL. */
	private String serverURL;

	/**
	 * Instantiates a new repository server connection.
	 * 
	 * @param serverURL
	 *            the server serverURL
	 */
	public RepositoryServerConnection(String serverURL) {
		this.serverURL = serverURL;
	}

	/**
	 * Gets the providing bundle file name or null if there's no bundle in the
	 * repository that provides this dependency.
	 * 
	 * @param packageManifestDescription
	 *            the package manifest description
	 * @return the providing bundle file name or null if no bundle provides the
	 *         package.
	 * @throws MojoExecutionException
	 */
	public String getProvidingBundleFileName(String packageManifestDescription)
			throws MojoExecutionException {
		try {
			URL url = new URL(serverURL + packageManifestDescription);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setAllowUserInteraction(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.connect();
			String fileName;
			String raw = conn.getHeaderField("Content-Disposition");
			// raw = "attachment; filename=abc.jpg"
			if (raw != null && raw.indexOf("=") != -1) {
				fileName = raw.split("=")[1].replace("\"", "");
			} else {
				throw new MojoExecutionException(
						ErrorMessageFormatter
								.format("There's no bundle that resolves this dependency."));
			}
			conn.disconnect();
			return fileName;
		} catch (MalformedURLException e) {
			// e.printStackTrace();
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("Server returned a corrupted response! No file name indicated."));
		} catch (IOException e) {
			// e.printStackTrace();
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("Failed to connect to the server!"));
		}
	}

	/**
	 * Download bundle from the repository server.
	 * 
	 * @param path
	 *            the path to store the downloaded bundle.
	 * @param bundleName
	 *            the bundle name
	 * @param packageManifestDescription
	 *            the package manifest description
	 * @throws MojoExecutionException
	 *             the mojo execution exception
	 */
	public void downloadBundle(String path, String bundleName,
			String packageManifestDescription) throws MojoExecutionException {
		try {
			URL website = new URL(serverURL + packageManifestDescription);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(path + bundleName);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("Server returned a corrupted response during download."));
		}
	}
}
