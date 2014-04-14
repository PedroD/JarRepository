package SimpleOSGiRepository.plugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException {
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
		getLog().info("Found/Created " + path + " with success!");

		File f = new File(".");
		for (File p : f.listFiles())
			getLog().info(p.getName());
	}

	/**
	 * Creates a folder.
	 * 
	 * @param folderName
	 *            the folder name
	 * @return true, if successful
	 */
	private static boolean createFolder(String folderName) {
		final Path repositoryDirectory = Paths.get(folderName);
		if (!repositoryDirectory.toFile().exists()) {
			repositoryDirectory.toFile().mkdirs();
		}
		return repositoryDirectory.toFile().exists();
	}
}