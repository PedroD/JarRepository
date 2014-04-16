package lumina.kernel.osgi.factories;

/**
 * Factory responsible for providing instances of an OSGi service.
 * 
 * @author Pedro Domingues (pedro.domingues@ist.utl.pt)
 * 
 * @param <T>
 */
public interface ILuminaExtensionFactory<T> {

	/**
	 * Gets the new service instance.
	 * 
	 * @param params
	 *            optional parameters necessary to instantiate a service
	 *            associated to some runtime context.
	 * @return the new service instance
	 */
	public abstract T getNewExtensionInstance(Object... params);

}