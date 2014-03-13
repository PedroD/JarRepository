import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class JarLoader {

	public static void main(String[] args) {
		BufferedReader bf = loadJarFile("d:\\bundle.jar");
		try {
			StringBuilder out = new StringBuilder();
			String line;
			while ((line = bf.readLine()) != null) {
				out.append(line + "\n");
			}
			System.out.println(out.toString());
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
	}

	@SuppressWarnings("resource")
	public static BufferedReader loadJarFile(String filename) {
		ZipFile zip = null;
		try {
			zip = new ZipFile(filename);
			for (Enumeration<? extends ZipEntry> e = zip.entries(); e
					.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				if (entry.toString().equals("META-INF/MANIFEST.MF")) {
					zip.close();
					ZipFile fis = new ZipFile(filename);
					return new BufferedReader(new InputStreamReader(
							fis.getInputStream(entry)));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
