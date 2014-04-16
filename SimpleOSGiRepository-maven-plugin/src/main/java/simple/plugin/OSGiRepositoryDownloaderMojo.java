package simple.plugin;

import java.io.File;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import simple.plugin.filemanipulators.ManifestLoader;
import simple.plugin.filemanipulators.TargetFileCreator;
import simple.plugin.utils.ErrorMessageFormatter;
import simple.plugin.utils.RepositoryServerConnection;

/**
 * The Class used to download the manifest.mf dependencies from the repository.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
@Mojo(name = "download", defaultPhase = LifecyclePhase.VALIDATE)
public class OSGiRepositoryDownloaderMojo extends AbstractMojo {

	/**
	 * The path to display.
	 */
	@Parameter
	private String path;

	/**
	 * The server URL.
	 */
	@Parameter
	private String serverURL;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(
				"==========================================================");
		getLog().info(
				"===========Simple OSGi Repository Maven Plugin============");
		getLog().info(
				"==========================================================");
		/*
		 * Validate server info.
		 */
		if (serverURL == null)
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("You must configure the server URL in the POM file!"));
		/*
		 * Add the final slash and the "http://" if missing.
		 */
		if (serverURL.charAt(serverURL.length() - 1) != '/')
			serverURL += '/';
		if (!serverURL.contains("http://"))
			serverURL = "http://" + serverURL;
		/*
		 * Set default path.
		 */
		if (path == null || path.equals(""))
			path = "." + File.separator + ".settings" + File.separator + "libs";
		/*
		 * Add the final separator.
		 */
		if (path.charAt(path.length() - 1) != File.separatorChar)
			path += File.separatorChar;
		/*
		 * Create the folder to download the dependencies to.
		 */
		if (!createFolder(path)) {
			throw new MojoExecutionException(
					ErrorMessageFormatter
							.format("Cannot create folder for putting the downloaded libraries. Please create the folder manually: "
									+ File.separator + path));
		}
		getLog().info(" ");
		getLog().info("Downloaded bundles will be stored in " + path);
		getLog().info(" ");
		/*
		 * Cycle through dependencies and download them.
		 */
		RepositoryServerConnection server = new RepositoryServerConnection(
				serverURL);
		for (String dependency : ManifestLoader.getImportedPackages()) {
			getLog().info("Resolving: " + dependency);
			String bundleName = server.getProvidingBundleFileName(dependency);
			if (bundleName == null)
				throw new MojoFailureException(
						ErrorMessageFormatter
								.format("There's no bundle that can solve this dependency: "
										+ dependency));
			if (!new File(path + bundleName).exists()) {
				server.downloadBundle(path, bundleName, dependency);
				getLog().info(" * Downloaded " + bundleName + ".");
			} else {
				getLog().info(
						" * Bundle " + bundleName
								+ " previously downloaded (delete it from "
								+ path + " if you want to download it again).");
			}
		}
		/*
		 * Create a target file.
		 */
		TargetFileCreator.create(path);
		getLog().info(
				"==========================================================");
		getLog().info(
				"==========================================================");
	}

	/**
	 * Creates a folder.
	 * 
	 * @param folderName
	 *            the folder name
	 * @return true, if successful
	 */
	private static boolean createFolder(String folderName) {
		final java.nio.file.Path repositoryDirectory = Paths.get(folderName);
		if (!repositoryDirectory.toFile().exists()) {
			repositoryDirectory.toFile().mkdirs();
		}
		return repositoryDirectory.toFile().exists();
	}
}