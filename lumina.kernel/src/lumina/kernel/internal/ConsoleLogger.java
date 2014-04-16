package lumina.kernel.internal;

import lumina.kernel.Logger;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * The Class ConsoleLogger that logs into the parent terminal.
 */
public final class ConsoleLogger implements LogService {

	/**
	 * Prints the log level.
	 * 
	 * @param level
	 *            the level
	 * @return the string
	 */
	private String printLogLevel(int level) {
		switch (level) {
		case LogService.LOG_DEBUG:
			return "[DEBUG]";
		case LogService.LOG_ERROR:
			return "[ERROR]";
		case LogService.LOG_INFO:
			return "[INFO]";
		default:
			return "[WARNING]";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.log.LogService#log(int, java.lang.String)
	 */
	@Override
	public void log(int level, String message) {
		System.out.println(printLogLevel(level) + " " + message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.service.log.LogService#log(int, java.lang.String,
	 * java.lang.Throwable)
	 */
	@Override
	public void log(int level, String message, Throwable exception) {
		System.out.println(printLogLevel(level) + " " + message);
		exception.printStackTrace();
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
		System.out.println(printLogLevel(level) + " ON "
				+ sr.getBundle().getSymbolicName() + " " + message);
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
		System.out.println(printLogLevel(level) + " ON "
				+ sr.getBundle().getSymbolicName() + " " + message);
		exception.printStackTrace();
	}
}
