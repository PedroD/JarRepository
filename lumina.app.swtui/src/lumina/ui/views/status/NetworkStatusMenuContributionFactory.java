package lumina.ui.views.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lumina.Constants;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.network.NetworkInterfaceManager;
import lumina.network.gateways.api.IGateway;
import lumina.ui.actions.ToggleInterfaceConnectionStatusHandler;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A menu contribution factory that contributes the communication interface menu
 * options.
 */
public final class NetworkStatusMenuContributionFactory extends
		AbstractContributionFactory {

	/**
	 * Prefix used to form the IDs of the dynamically generated commands (needed
	 * for the contributions).
	 */
	private static final String TOGGLE_INTERFACE_STATUS_COMMAND_ID_PREFIX = "lumina.commands.network.toggleInterfaceStatus.";

	/**
	 * Prefix used to form the IDs of the dynamically generated contributions.
	 */
	private static final String TOGGLE_INTERFACE_STATUS_CONTRIB_ID_PREFIX = "lumina.menus.network.toggleInterfaceStatus.";

	/**
	 * ID of the network commands category.
	 */
	private static final String NETWORK_COMMAND_CATEGORY_ID = "lumina.commands.categories.communication"; // $NON-NLS-1$

	/**
	 * ID of generated {@link CommandContributionItem}.
	 */
	private static final String COMPOUND_ITEM_CONTRIB_ID = "lumina.commands.categories.communication"; // $NON-NLS-1$

	/**
	 * Menu entry displayed when no interfaces are defined.
	 */
	private static final String NO_INTERFACE = Messages
			.getString("NetworkStatus.NoInterfaceDefined");

	/**
	 * String returned by {@link #getNamespace()}. The same as the the other
	 * factories.
	 * <p>
	 * Note: If a namespace is not provided the call
	 * {@link org.eclipse.ui.internal.menus.WorkbenchMenuService#unregisterVisibleWhen(IContributionItem)}
	 * will fail with a {@link NullPointerException}.
	 */
	// FIXME: Check if in the newer version of eclipse the access is now
	// possible.
	@SuppressWarnings("restriction")
	private static final String NAMESPACE = Constants.APPLICATION_NAMESPACE;

	/**
	 * A list of commands that acts as a command cache.
	 */
	private final List<Command> lastCommands = new LinkedList<Command>();

	public NetworkStatusMenuContributionFactory(final String uri) {
		super(uri, null);
	}

	/**
	 * Obtains the name of the interface.
	 * 
	 * @param networkInterface
	 *            the interface parameter; can be <code>null</code>
	 * @return the interface name of <tt>NO_INTERFACE</tt> if a
	 *         <code>null</code> interface was specified
	 */
	public static String getInterfaceNameInternal(
			final IGateway networkInterface) {
		if (networkInterface == null) {
			return NO_INTERFACE;
		} else {
			return networkInterface.getGatewayConnectionName();
		}
	}

	/**
	 * Creates the ID of the command utilized to toggle the communication
	 * interface status.
	 * <p>
	 * Note: This method depends on the uniqueness of the id of the network
	 * interface.
	 * 
	 * @param p
	 *            the communication interface object; cannot be
	 *            <code>null</code>
	 * @return the id of the command.
	 */
	public static String getCommandId(final IGateway p) {
		if (p == null) {
			return TOGGLE_INTERFACE_STATUS_COMMAND_ID_PREFIX + NO_INTERFACE;
		} else {
			return TOGGLE_INTERFACE_STATUS_COMMAND_ID_PREFIX
					+ getInterfaceNameInternal(p);
		}
	}

	/**
	 * Clears the command action cache.
	 */
	private synchronized void disposeLastCommands() {
		if (lastCommands != null) {
			for (Command c : lastCommands) {
				final IHandler h = c.getHandler();
				if (h != null) {
					h.dispose();
				}
			}

			lastCommands.clear();
		}
	}

	/**
	 * Get the command handler that toggles the status of the communication
	 * interface.
	 * <p>
	 * If this handler is not created the menu option will appear disabled.
	 * 
	 * @param networkInterface
	 *            the communication interface
	 * @return a {@link Command} object
	 */
	private synchronized Command getCommand(
			final NetworkInterfaceManager manager,
			final IGateway networkInterface) {
		final String interfaceName = getInterfaceNameInternal(networkInterface);

		final ICommandService commandService = (ICommandService) PlatformUI
				.getWorkbench().getService(ICommandService.class);

		final Command toggleInterfaceCommand = commandService
				.getCommand(getCommandId(networkInterface));

		/*
		 * FIXME: We used to check this with:
		 * !toggleInterfaceCommand.isDefined()). Is this still relevant.
		 */
		final Category category = commandService
				.getCategory(NETWORK_COMMAND_CATEGORY_ID);

		if (networkInterface == null) {
			toggleInterfaceCommand.define(interfaceName,
					"No communication interfaces defined", category);
		} else {
			toggleInterfaceCommand.define(interfaceName,
					"Connects/disconnects the interface '" + interfaceName
							+ "'", category);
		}

		final boolean createHandler = networkInterface != null
				&& networkInterface.hasTransportPort();

		/*
		 * If the handler is not created the menu item appears disabled in the
		 * menu.
		 */
		if (createHandler) {
			toggleInterfaceCommand
					.setHandler(new ToggleInterfaceConnectionStatusHandler(
							manager, networkInterface));
		} else {
			toggleInterfaceCommand.setHandler(null);
		}

		lastCommands.add(toggleInterfaceCommand);
		return toggleInterfaceCommand;
	}

	@SuppressWarnings("deprecation")
	private IContributionItem makeContributionItem(
			final NetworkInterfaceManager manager,
			final IGateway networkInterface,
			final IServiceLocator serviceLocator) {

		final Command command = getCommand(manager, networkInterface);

		final String id = TOGGLE_INTERFACE_STATUS_CONTRIB_ID_PREFIX
				+ getInterfaceNameInternal(networkInterface);

		final String commandId = command.getId();
		final ImageDescriptor icon = null;
		final ImageDescriptor disabledIcon = null;
		final ImageDescriptor hoverIcon = null;

		String label;
		try {
			label = command.getName();
		} catch (NotDefinedException e) {
			label = networkInterface.getGatewayConnectionName();
		}

		final String mnemonic = "";

		String tooltip;
		try {
			tooltip = command.getDescription();
		} catch (NotDefinedException e) {
			tooltip = "";
		}

		final int style = CommandContributionItem.STYLE_CHECK;

		// FIXME: Change this to a new constructor!
		return new CommandContributionItem(serviceLocator, id, commandId,
				Collections.EMPTY_MAP, icon, disabledIcon, hoverIcon, label,
				mnemonic, tooltip, style);
	}

	@Override
	public void createContributionItems(final IServiceLocator serviceLocator,
			final IContributionRoot additions) {

		final IContributionItem dynamicItem = new CompoundContributionItem(
				COMPOUND_ITEM_CONTRIB_ID) {

			@Override
			public void dispose() {
				disposeLastCommands();
				super.dispose();
			}

			@Override
			protected IContributionItem[] getContributionItems() {
				disposeLastCommands();

				final Project project = ProjectModel.getInstance().getProject();
				if (project != null) {
					final NetworkInterfaceManager manager = project
							.getNetworkInterfaceManager();

					if (manager != null) {
						final IGateway[] interfaces = manager
								.getInterfaces();

						/*
						 * No interfaces defined
						 */
						if (interfaces != null && interfaces.length == 0) {
							return new IContributionItem[] { makeContributionItem(
									manager, null, serviceLocator) };
						}

						/*
						 * Interfaces are defined
						 */
						if (interfaces != null && interfaces.length > 0) {
							final List<IContributionItem> itemList = new ArrayList<IContributionItem>();

							for (IGateway i : interfaces) {
								final IContributionItem c = makeContributionItem(
										manager, i, serviceLocator);

								itemList.add(c);
							}

							return itemList.toArray(new IContributionItem[0]);
						}
					}
				}

				return new IContributionItem[0];
			}
		};

		additions.addContributionItem(dynamicItem, null);
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}
}
