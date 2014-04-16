package simple.plugin.filemanipulators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * The Class TargetFileCreator.
 */
public final class TargetFileCreator {

	/**
	 * Creates the target file.
	 * 
	 * @param pathToPointTo
	 *            the path to point to.
	 */
	public static void create(String pathToPointTo) {
		try {
			String[] tmp = new File(".").getCanonicalPath()
					.replace(File.separatorChar, '/').split("/");
			final String projectName = tmp[tmp.length - 1];
			FileOutputStream targetFile = new FileOutputStream(projectName
					+ ".target");
			String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
					+ "<?pde version=\"3.8\"?><target name=\""
					+ projectName
					+ " Target File\" sequenceNumber=\"6\">\r\n"
					+ "<locations>\r\n"
					+ "<location path=\""
					+ new File(pathToPointTo).getCanonicalPath()
					+ "\" type=\"Directory\"/>\r\n"
					+ "</locations>\r\n"
					+ "</target>";
			targetFile.write(content.getBytes());

			targetFile.flush();
			targetFile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
