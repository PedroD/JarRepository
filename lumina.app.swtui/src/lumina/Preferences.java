package lumina;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Utility class to simplify access to the Eclipse preferences.
 */
public final class Preferences {

	private static IScopeContext scope = new InstanceScope();

	/** Prevent instantiation. */
	private Preferences() {
	}

	/**
	 * Gets the appropriate preferences node for use in the application.
	 * 
	 * @return Root preferences node.
	 */
	public static IEclipsePreferences getRootNode() {
		return scope.getNode(Activator.PLUGIN_ID);
	}

	/**
	 * Gets a preferences node given its path.
	 * 
	 * @param path
	 *            Path
	 * @return Preferences node.
	 */
	public static IEclipsePreferences getNode(String path) {
		return scope.getNode(Activator.PLUGIN_ID + "/" + path);
	}

}
