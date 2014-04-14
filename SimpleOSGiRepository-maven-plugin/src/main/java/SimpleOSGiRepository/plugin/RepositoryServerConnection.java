package SimpleOSGiRepository.plugin;

/**
 * Class that communicates with the server.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 */
public final class RepositoryServerConnection {

	/** The server's port. */
	private int port;

	/** The server's url. */
	private String url;

	/**
	 * Instantiates a new repository server connection.
	 * 
	 * @param serverURL
	 *            the server url
	 * @param serverPort
	 *            the server port
	 */
	public RepositoryServerConnection(String serverURL, int serverPort) {
		this.url = serverURL;
		this.port = serverPort;
	}

	/**
	 * Gets the providing bundle file name or null if there's no bundle in the
	 * repository that provides this dependency.
	 * 
	 * @param packageManifestDescription
	 *            the package manifest description
	 * @return the providing bundle file name or null if no bundle provides the
	 *         package.
	 */
	public String getProvidingBundleFileName(String packageManifestDescription) {
		// TODO:
		// https://stackoverflow.com/questions/10995378/httpurlconnection-downloaded-file-name
		return null;
	}

	/**
	 * Download bundle from the repository server.
	 * 
	 * @param bundleName
	 *            the bundle name
	 * @param downloadPath
	 *            the path to save the downloaded bundle.
	 */
	public void downloadBundle(String bundleName, String downloadPath) {
		// TODO:
		// https://stackoverflow.com/questions/921262/how-to-download-and-save-a-file-from-internet-using-java
	}
}
