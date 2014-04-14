package lumina.ui.swt.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.IHandler;

/**
 * Manages the interception of command handlers.
 * <p>
 * This service is used to overcome a limitation of the eclipse platform where a
 * command handler cannot override another. According to the <a href=
 * "http://help.eclipse.org/help33/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/workbench_cmd_handlers.htm"
 * >documentation</a> we should be able to do it but unfortunately does not work
 * as <a href=
 * "http://dev.eclipse.org/newslists/news.eclipse.platform.rcp/msg30560.html"
 * >our post</a> indicates.
 */
public final class HandlerInterceptionService {

	private static HandlerInterceptionService instance;

	/**
	 * Current command overrides.
	 */
	private Map<String, IHandler> handlers = new HashMap<String, IHandler>();

	/**
	 * Prevent instantiation from outside.
	 */
	private HandlerInterceptionService() {
	}

	/**
	 * Returns the service instance.
	 * 
	 * @return the service instance
	 */
	public static HandlerInterceptionService getInstance() {
		if (instance == null) {
			instance = new HandlerInterceptionService();
		}

		return instance;
	}

	/**
	 * Checks if a given command handler is intercepted.
	 * 
	 * @param id
	 *            the command id
	 * @return true if there is an overriding command
	 */
	public boolean isIntercepted(final String id) {
		return handlers.containsKey(id);
	}

	/**
	 * Obtains the interceptor handler for the given id.
	 * 
	 * @param id
	 *            the command id
	 * @return the registered interception handler or <code>null</code> if no
	 *         interception has been registered for that id.
	 */
	public IHandler getInterceptor(final String id) {
		return handlers.get(id);
	}

	/**
	 * Registers an interception for a given id.
	 * 
	 * @param id
	 *            the id
	 * @param handler
	 *            the handler to be redirect intercepted methods to.
	 */
	public void registerInterception(final String id, final IHandler handler) {
		if (id == null || id.length() == 0) {
			throw new IllegalArgumentException("Id must be a valid string"); // $NON-NLS-1$
		}

		if (handler == null) {
			throw new IllegalArgumentException("Handler cannot be null"); // $NON-NLS-1$
		}

		handlers.put(id, handler);
	}

	/**
	 * Removes the interception for a given id.
	 * <p>
	 * Any calls to methods of the intercepted handler will be handled locally
	 * from now on.
	 * 
	 * @param id
	 *            the command id
	 * @return the existing interception or <code>null</code> if no interception
	 *         has been previously registered for the given id.
	 */
	public IHandler removeInterception(final String id) {
		return handlers.remove(id);
	}
}
