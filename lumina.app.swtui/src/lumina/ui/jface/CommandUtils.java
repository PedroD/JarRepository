package lumina.ui.jface;

import lumina.kernel.Logger;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.osgi.service.log.LogService;

/**
 * Provides utilities for dealing with {@link org.eclipse.jface.action.Action
 * Actions} and {@link org.eclipse.core.commands.Command Commands}.
 */
public final class CommandUtils {
	/**
	 * Prevent the instantiation of this utility class.
	 */
	private CommandUtils() {
	}

	public static MenuItem createContributionMenu(final String commandId,
			final int style, final Menu parent) {
		final ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getService(ICommandService.class);

		Command command = commandService.getCommand(commandId);

		try {
			// Creation of the basic menu item
			MenuItem item = new MenuItem(parent, style);
			item.setText(command.getName());
			item.setEnabled(command.isEnabled());
			item.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					executeCommand(commandId);
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

			// Set the command image
			final ICommandImageService commandImageService = (ICommandImageService) PlatformUI
					.getWorkbench().getService(ICommandImageService.class);
			final ImageDescriptor descriptor = commandImageService
					.getImageDescriptor(commandId);
			if (descriptor != null) {
				Image image = descriptor.createImage();
				if (image != null) {
					item.setImage(image);
				}
			}
		} catch (NotDefinedException e1) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Command undefined" + commandId, e1); // NON-NLS-1
		}

		return null;
	}

	/**
	 * Executes a command given the command is string using the supplied
	 * handler.
	 * 
	 * @param commandId
	 *            the command id string (as define in the plugin.xml), must not
	 *            be <code>null</code>DFs
	 */
	public static void executeCommand(final String commandId) {
		final IHandlerService handlerService = (IHandlerService) PlatformUI
				.getWorkbench().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(commandId, null);
		} catch (NotDefinedException e1) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Command undefined" + commandId, e1); // NON-NLS-1
		} catch (NotHandledException e2) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Command without handler" + commandId, e2); // NON-NLS-1
		} catch (NotEnabledException e3) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Command not enabled" + commandId, e3); // NON-NLS-1
		} catch (ExecutionException e4) {
			Logger.getInstance().log(LogService.LOG_ERROR,
					"Command execution failed" + commandId, e4); // NON-NLS-1
		}
	}
}
