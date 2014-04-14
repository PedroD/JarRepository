package lumina.ui.swt.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

/**
 * Base class for handlers that can be intercepted.
 * <p>
 * If an interception has been registered in the Handler Interception Service,
 * any method calls to this handler will be redirected to the interceptor,
 * otherwise they will be handled locally.
 */
public abstract class InterceptedHandler extends AbstractHandler {

	/**
	 * The command id. Used to unregister the override when the handler is
	 * disposed.
	 */
	private final String commandId;

	/**
	 * Creates an handler that overrides a given command.
	 */
	public InterceptedHandler() {
		commandId = getCommandId();
	}

	/**
	 * Gets the command id for the handler being overridden.
	 * 
	 * @return a String containing the command id.
	 */
	public abstract String getCommandId();

	/**
	 * Execute default handler.
	 * 
	 * @param event
	 *            execution event
	 * @return object execution result
	 * @throws ExecutionException
	 *             to notify a problem with the execution
	 */
	protected abstract Object executeDefault(ExecutionEvent event)
			throws ExecutionException;

	/**
	 * Adds the default handler listener.
	 * 
	 * @param handlerListener
	 *            handler listener
	 */
	protected void addHandlerListenerDefault(IHandlerListener handlerListener) {
		super.addHandlerListener(handlerListener);
	}

	/**
	 * Default dispose.
	 */
	protected void disposeDefault() {
		super.dispose();
	}

	/**
	 * Checks if the default is enabled.
	 * 
	 * @return true if parent is enabled
	 */
	protected boolean isEnabledDefault() {
		return super.isEnabled();
	}

	/**
	 * Checks if parent is handled.
	 * 
	 * @return true if parent is handled.
	 */
	protected boolean isHandledDefault() {
		return super.isHandled();
	}

	/**
	 * Removes the default handler listener.
	 * 
	 * @param handlerListener
	 *            handler listener to remove
	 */
	protected void removeHandlerListenerDefault(IHandlerListener handlerListener) {
		super.removeHandlerListener(handlerListener);
	}

	/**
	 * Adds a handler listener.
	 * 
	 * @param handlerListener
	 *            handler listener to add
	 */
	@Override
	public final void addHandlerListener(IHandlerListener handlerListener) {
		final IHandler h = HandlerInterceptionService.getInstance()
				.getInterceptor(commandId);
		if (h != null) {
			h.addHandlerListener(handlerListener);
		} else {
			addHandlerListenerDefault(handlerListener);
		}
	}

	/**
	 * Disposes interceptor and dependents.
	 */
	@Override
	public final void dispose() {
		final IHandler h = HandlerInterceptionService.getInstance()
				.getInterceptor(commandId);
		if (h != null) {
			h.dispose();
		} else {
			disposeDefault();
		}
	}

	/**
	 * Checks if it is enabled.
	 * 
	 * @return true if it is enabled, false otherwise
	 */
	@Override
	public final boolean isEnabled() {
		final IHandler h = HandlerInterceptionService.getInstance()
				.getInterceptor(commandId);
		if (h != null) {
			return h.isEnabled();
		} else {
			return isEnabledDefault();
		}
	}

	/**
	 * Checks if it is handled.
	 * 
	 * @return true if it is handled, false otherwise
	 */
	@Override
	public final boolean isHandled() {
		final IHandler h = HandlerInterceptionService.getInstance()
				.getInterceptor(commandId);
		if (h != null) {
			return h.isHandled();
		} else {
			return isHandledDefault();
		}
	}

	/**
	 * Removes the handler listener.
	 * 
	 * @param handlerListener
	 *            handler listener to remove
	 */
	@Override
	public final void removeHandlerListener(IHandlerListener handlerListener) {
		final IHandler h = HandlerInterceptionService.getInstance()
				.getInterceptor(commandId);
		if (h != null) {
			h.removeHandlerListener(handlerListener);
		} else {
			removeHandlerListenerDefault(handlerListener);
		}
	}

	/**
	 * Execute event.
	 * <p>
	 * If an handler is defined it executes it, otherwise it will execute the
	 * default handler.
	 * 
	 * @param event
	 *            execution event
	 * @return the execution result object
	 * @throws ExecutionException
	 *             if the handler of the {@link #executeDefault(ExecutionEvent)}
	 *             throws the exception.
	 */
	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException {
		final IHandler h = HandlerInterceptionService.getInstance()
				.getInterceptor(commandId);
		if (h != null) {
			return h.execute(event);
		} else {
			return executeDefault(event);
		}
	}
}
