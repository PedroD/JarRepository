package lumina.kernel.osgi.registries;

import org.osgi.service.log.LogService;

/**
 * The Class LoggerRegistry.
 */
public class LoggerRegistry extends AbstractExtensionRegistry<LogService> {

	/** The singleton instance. */
	private static LoggerRegistry singletonInstance = new LoggerRegistry();

	/**
	 * Instantiates a new logger registry.
	 */
	private LoggerRegistry() {
		// Avoid instantiation.
	}

	/**
	 * Gets the single instance of LoggerRegistry.
	 * 
	 * @return single instance of LoggerRegistry
	 */
	public static LoggerRegistry getInstance() {
		return singletonInstance;
	}
}