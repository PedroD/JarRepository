package lumina.kernel.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator implements BundleActivator {
	/**
	 * The bundle context
	 */
	public static BundleContext context;

	@Override
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
	}
}