package lumina.kernel;

import lumina.kernel.internal.ConsoleLogger;
import lumina.kernel.osgi.registries.LoggerRegistry;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * The Class Logger used to log to lumina's registered loggers.
 */
public final class Logger implements LogService {

	/** The Constant singletonInstance. */
	private static final LogService singletonInstance = new Logger();

	/**
	 * Gets the single instance of Logger.
	 * 
	 * @return single instance of Logger
	 */
	public static LogService getInstance() {
		return singletonInstance;
	}

	/**
	 * Instantiates a new logger.
	 */
	private Logger() {
		// Avoid instantiation
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.log.LogService#log(int, java.lang.String)
	 */
	@Override
	public void log(int level, String message) {
		String[] loggerNames = LoggerRegistry.getInstance()
				.getRegisteredServicesNames();
		new ConsoleLogger().log(level, message);
		for (String name : loggerNames)
			LoggerRegistry.getInstance().getService(name).log(level, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.log.LogService#log(int, java.lang.String,
	 * java.lang.Throwable)
	 */
	@Override
	public void log(int level, String message, Throwable exception) {
		String[] loggerNames = LoggerRegistry.getInstance()
				.getRegisteredServicesNames();
		new ConsoleLogger().log(level, message, exception);
		for (String name : loggerNames)
			LoggerRegistry.getInstance().getService(name)
					.log(level, message, exception);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.log.LogService#log(org.osgi.framework.ServiceReference,
	 * int, java.lang.String)
	 */
	@Override
	public void log(ServiceReference sr, int level, String message) {
		String[] loggerNames = LoggerRegistry.getInstance()
				.getRegisteredServicesNames();
		new ConsoleLogger().log(level, message);
		for (String name : loggerNames)
			LoggerRegistry.getInstance().getService(name)
					.log(sr, level, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.service.log.LogService#log(org.osgi.framework.ServiceReference,
	 * int, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void log(ServiceReference sr, int level, String message,
			Throwable exception) {
		String[] loggerNames = LoggerRegistry.getInstance()
				.getRegisteredServicesNames();
		new ConsoleLogger().log(level, message);
		for (String name : loggerNames)
			LoggerRegistry.getInstance().getService(name)
					.log(sr, level, message, exception);
	}
}
