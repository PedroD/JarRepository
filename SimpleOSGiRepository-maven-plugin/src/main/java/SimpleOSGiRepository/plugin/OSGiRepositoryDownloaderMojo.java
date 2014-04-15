package SimpleOSGiRepository.plugin;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaCore;

/**
 * The Class used to download the manifest.mf dependencies from the repository.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 */
@Mojo(name = "download")
public class OSGiRepositoryDownloaderMojo extends AbstractMojo {

	/**
	 * The path to display.
	 */
	@Parameter(property = "download.path")
	private String path;

	/**
	 * The server URL.
	 */
	@Parameter(property = "download.serverurl")
	private String serverURL;

	/**
	 * The server port.
	 */
	@Parameter(property = "download.serverport")
	private int serverPort;

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
		if (serverPort == 0 || serverURL == null)
			throw new MojoExecutionException(
					"You must configure the server URL and port in the POM file!");
		/*
		 * Set default path.
		 */
		if (path == null || path.equals(""))
			path = "." + File.separator + ".settings" + File.separator + "libs";
		/*
		 * Create the folder to download the dependencies to.
		 */
		if (!createFolder(path)) {
			throw new MojoExecutionException(
					"Cannot create folder for putting the downloaded libraries. Please create the folder manually: "
							+ File.separator + path);
		}
		getLog().info("Downloaded bundles will be stored in " + path);
		/*
		 * Cycle through dependencies and download them.
		 */
		RepositoryServerConnection server = new RepositoryServerConnection(
				serverURL, serverPort);
		for (String dependency : ManifestLoader.getExportedPackages()) {
			String bundleName = server.getProvidingBundleFileName(dependency);
			if (bundleName == null)
				throw new MojoFailureException(
						"There's no bundle that can solve this dependency: "
								+ dependency);
			if (!new File(path + File.separator + bundleName).exists()) {
				server.downloadBundle(bundleName, path);
				addClasspathEntry(bundleName);
				getLog().info("Downloaded " + bundleName + ".");
			}

		}
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