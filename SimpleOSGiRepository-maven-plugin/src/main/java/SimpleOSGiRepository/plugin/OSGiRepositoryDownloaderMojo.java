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

	/**
	 * Adds a new classpath entry to this project's .classpath file.
	 * 
	 * @param bundlePath
	 *            the bundle path
	 * @throws MojoExecutionException
	 */
	private static void addClasspathEntry(String bundlePath)
			throws MojoExecutionException {
		/*
		 * Resolve current project's name.
		 */
		String projectName = null;
		// TODO: READ IT FROM .PROJECT FILE
		/*
		 * Add the new library to the classpath.
		 */
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(projectName);
		try {
			IJavaProject javaProject = (IJavaProject) project
					.getNature(JavaCore.NATURE_ID);
			IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
			List<IClasspathEntry> list = new LinkedList<IClasspathEntry>(
					java.util.Arrays.asList(rawClasspath));
			boolean isAlreadyAdded = false;
			for (IClasspathEntry cpe : rawClasspath) {
				isAlreadyAdded = cpe.getPath().toOSString().equals(bundlePath);
				if (isAlreadyAdded)
					break;
			}
			if (!isAlreadyAdded) {
				IClasspathEntry jarEntry = JavaCore.newLibraryEntry(new Path(
						bundlePath), null, null);
				list.add(jarEntry);
			}
			IClasspathEntry[] newClasspath = (IClasspathEntry[]) list
					.toArray(new IClasspathEntry[0]);
			javaProject.setRawClasspath(newClasspath, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}