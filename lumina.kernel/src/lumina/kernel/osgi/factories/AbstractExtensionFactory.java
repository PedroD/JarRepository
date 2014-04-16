package lumina.kernel.osgi.factories;

/**
 * Factory responsible for producing extension instances.
 * 
 * @param <T>
 */
public abstract class AbstractExtensionFactory<T> implements ILuminaExtensionFactory<T> {
	protected final Class<T> service;

	public AbstractExtensionFactory(Class<T> serviceClass) {
		service = serviceClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lumina.core.osgi.factories.ILuminaExtensionFactory#getNewServiceInstance()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final T getNewExtensionInstance(Object... params) {
		T s = null;
		try {
			s = (T) createInstance(params);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Creates a new service instance.<br>
	 * This method can be override to specify the instatiation process.
	 * <p>
	 * <b>Default implementation does the following:</b><br>
	 * <code>return service.newInstance();</code><br>
	 * Where <code>service</code> is the service class given in the
	 * {@link #AbstractExtensionFactory(Class)} constructor.
	 * 
	 * @param params
	 *            the context params
	 * @return the new service instance
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected Object createInstance(Object... params)
			throws InstantiationException, IllegalAccessException {
		return service.newInstance();
	}
}
