package lumina.kernel.osgi.trackers;

import lumina.kernel.Logger;
import lumina.kernel.osgi.LuminaExtension;
import lumina.kernel.osgi.factories.ILuminaExtensionFactory;
import lumina.kernel.osgi.registries.LoggerRegistry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

@SuppressWarnings("rawtypes")
public class LoggerTracker extends ServiceTracker {

	/**
	 * The context of the plugin lumina activator.
	 */
	private BundleContext bundleContext;

	/**
	 * Constructs a tracker that uses the specified bundle context to track
	 * services and notifies the specified application object about changes.
	 * 
	 * @param context
	 *            The bundle context to be used by the tracker.
	 * @param registry
	 *            The application object to notify about service changes.
	 **/
	public LoggerTracker(BundleContext context) {
		super(context, LogService.class.getName(), null);
		this.bundleContext = context;
	}

	/**
	 * Overrides the <tt>ServiceTracker</tt> functionality to inform the
	 * application object about the added service.
	 * 
	 * @param ref
	 *            The service reference of the added service.
	 * @return The service object to be used by the tracker.
	 **/
	@SuppressWarnings("unchecked")
	@Override
	public Object addingService(ServiceReference ref) {
		Logger.getInstance().log(LogService.LOG_ERROR,"New logger detected!");
		ILuminaExtensionFactory factory = (ILuminaExtensionFactory) bundleContext.getService(ref);
		final LuminaExtension logger = (LuminaExtension) factory
				.getNewExtensionInstance();
		final String loggerBundleName = logger.getExtensionId();
		if ((bundleContext.getBundle(0).getState() & (Bundle.STARTING | Bundle.ACTIVE)) == 0) {
			return logger;
		}
		LoggerRegistry.getInstance().addService(loggerBundleName, factory);
		return logger;
	}

	/**
	 * Overrides the <tt>ServiceTracker</tt> functionality to inform the
	 * application object about the modified service.
	 * 
	 * @param ref
	 *            The service reference of the modified service.
	 * @param svc
	 *            The service object of the modified service.
	 **/
	@SuppressWarnings("unchecked")
	@Override
	public void modifiedService(ServiceReference ref, Object svc) {
		ILuminaExtensionFactory factory = (ILuminaExtensionFactory) bundleContext.getService(ref);
		final LuminaExtension logger = (LuminaExtension) factory
				.getNewExtensionInstance();
		final String loggerBundleName = logger.getExtensionId();
		if ((bundleContext.getBundle(0).getState() & (Bundle.STARTING | Bundle.ACTIVE)) == 0) {
			return;
		}
		LoggerRegistry.getInstance().modifyService(loggerBundleName, factory);
	}

	/**
	 * Overrides the <tt>ServiceTracker</tt> functionality to inform the
	 * application object about the removed service.
	 * 
	 * @param ref
	 *            The service reference of the removed service.
	 * @param svc
	 *            The service object of the removed service.
	 **/
	@Override
	public void removedService(ServiceReference ref, Object svc) {
		ILuminaExtensionFactory factory = (ILuminaExtensionFactory) bundleContext.getService(ref);
		final LuminaExtension logger = (LuminaExtension) factory
				.getNewExtensionInstance();
		final String loggerBundleName = logger.getExtensionId();
		if ((bundleContext.getBundle(0).getState() & (Bundle.STARTING | Bundle.ACTIVE)) == 0) {
			return;
		}
		LoggerRegistry.getInstance().removeService(loggerBundleName);
	}

}
