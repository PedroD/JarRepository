package lumina;

import lumina.bundles.drivers.ilight.PCNodeProtocol;
import lumina.kernel.osgi.shell.OSGiShell;
import lumina.network.osgi.factories.DriverExtensionFactory;
import lumina.network.osgi.trackers.DriverTracker;
import lumina.network.osgi.trackers.TransportDriverTracker;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * TODO: Corrigir esta constante... isto é um gerador de erros dificeis de
	 * detectar... The plug-in ID. /!\ must mach the application ID on
	 * plugin.xml.
	 */
	public static final String PLUGIN_ID = "lumina.app.swtui";

	/**
	 * The shared instance.
	 */
	private static Activator plugin;

	/**
	 * The bundle context
	 */
	public static BundleContext luminaContext;

	/**
	 * Lumina shell used for receiving OSGi commands
	 */
	private OSGiShell luminaShell;

	/**
	 * Driver tracker used to track bundles offering new drivers
	 */
	private DriverTracker driverTracker;

	/**
	 * The transport driver tracker.
	 */
	private TransportDriverTracker transportDriverTracker;

	/**
	 * The constructor.
	 */
	public Activator() {
		plugin = this;
	}

	private ServiceRegistration pcNodeService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		luminaContext = context;
		startOSGiTrackers(context);
		luminaShell = new OSGiShell(2555);
		luminaShell.Start();
		/*
		 * Register PCNode Driver.
		 */
		pcNodeService = context.registerService(
				DriverExtensionFactory.class.getName(),
				new DriverExtensionFactory(PCNodeProtocol.class), null);
	}

	private void startOSGiTrackers(BundleContext context) {
		driverTracker = new DriverTracker(context);
		transportDriverTracker = new TransportDriverTracker(context);
		driverTracker.open();
		transportDriverTracker.open();
	}

	private void stopOSGiTrackers() {
		driverTracker.close();
		transportDriverTracker.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		stopOSGiTrackers();
		luminaShell.Stop();
		context.ungetService(pcNodeService.getReference());
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
