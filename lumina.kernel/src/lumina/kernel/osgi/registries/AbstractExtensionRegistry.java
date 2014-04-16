package lumina.kernel.osgi.registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lumina.kernel.Logger;
import lumina.kernel.osgi.factories.ILuminaExtensionFactory;

import org.osgi.service.log.LogService;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractExtensionRegistry.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 * @param <T>
 *            the service type
 */
public abstract class AbstractExtensionRegistry<T> implements ILuminaExtensionRegistry<T> {

	/** The service factories by name. */
	private Map<String, ILuminaExtensionFactory<T>> serviceFactoriesByName = new HashMap<String, ILuminaExtensionFactory<T>>();

	/** The service listeners. */
	private List<ServiceRegistryListener> serviceListeners = new ArrayList<ServiceRegistryListener>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.core.osgi.registries.ILuminaExtensionRegistry#addServiceListener(lumina
	 * .core.osgi.registries.IServiceRegistry.ServiceRegistryListener)
	 */
	@Override
	public final void addServiceListener(ServiceRegistryListener l) {
		if (!serviceListeners.contains(l))
			serviceListeners.add(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.core.osgi.registries.ILuminaExtensionRegistry#removeServiceListener(lumina
	 * .core.osgi.registries.IServiceRegistry.ServiceRegistryListener)
	 */
	@Override
	public final void removeServiceListener(ServiceRegistryListener l) {
		serviceListeners.remove(l);
	}

	/**
	 * Notify service added.
	 * 
	 * @param serviceName
	 *            the service name
	 */
	protected final void notifyServiceAdded(String serviceName) {
		for (ServiceRegistryListener l : serviceListeners)
			l.serviceAdded(serviceName);
	}

	/**
	 * Notify service modified.
	 * 
	 * @param serviceName
	 *            the service name
	 */
	protected final void notifyServiceModified(String serviceName) {
		for (ServiceRegistryListener l : serviceListeners)
			l.serviceModified(serviceName);
	}

	/**
	 * Notify service removed.
	 * 
	 * @param serviceName
	 *            the service name
	 */
	protected final void notifyServiceRemoved(String serviceName) {
		for (ServiceRegistryListener l : serviceListeners)
			l.serviceRemoved(serviceName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.core.osgi.registries.ILuminaExtensionRegistry#modifyService(java.lang.
	 * String, lumina.core.osgi.factories.ILuminaExtensionFactory)
	 */
	@Override
	public final void modifyService(String serviceName,
			ILuminaExtensionFactory<T> factory) {
		Logger.getInstance().log(LogService.LOG_DEBUG,"Driver " + serviceName + " modified.");
		serviceFactoriesByName.remove(serviceName);
		serviceFactoriesByName.put(serviceName, factory);
		notifyServiceModified(serviceName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.core.osgi.registries.ILuminaExtensionRegistry#removeService(java.lang.
	 * String)
	 */
	@Override
	public final void removeService(String serviceName) {
		Logger.getInstance().log(LogService.LOG_DEBUG,"Driver " + serviceName + " removed.");
		serviceFactoriesByName.remove(serviceName);
		notifyServiceRemoved(serviceName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.core.osgi.registries.ILuminaExtensionRegistry#addService(java.lang.String,
	 * lumina.core.osgi.factories.ILuminaExtensionFactory)
	 */
	@Override
	public final void addService(String serviceName, ILuminaExtensionFactory<T> factory) {
		Logger.getInstance().log(LogService.LOG_DEBUG,"Driver " + serviceName + " added.");
		serviceFactoriesByName.put(serviceName, factory);
		notifyServiceAdded(serviceName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lumina.core.osgi.registries.ILuminaExtensionRegistry#getRegisteredServices()
	 */
	@Override
	public final String[] getRegisteredServicesNames() {
		Set<String> driverNames = serviceFactoriesByName.keySet();
		String[] names = new String[driverNames.size()];
		int i = 0;
		for (String d : driverNames)
			names[i++] = d;
		return names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * lumina.core.osgi.registries.ILuminaExtensionRegistry#getService(java.lang.String)
	 */
	@Override
	public final T getService(String serviceName) {
		// TODO: Adicionar o fuzzy search do LDAP?
		return serviceFactoriesByName.get(serviceName).getNewExtensionInstance();
	}
}
