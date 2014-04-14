package lumina.ui.views.navigation;

import lumina.base.model.ModelItem;
import lumina.base.model.transfer.DeviceTransfer;
import lumina.license.Capabilities.Capability;
import lumina.qp.AggregateResult;
import lumina.qp.Sink;
import lumina.qp.Source;
import lumina.ui.jface.CommandUtils;
import lumina.ui.views.AbstractTreeView;
import lumina.ui.views.TreeDragAndDropListener;
import lumina.ui.views.blueprint.propertytesters.CanCenterInPlanPropertyTester;
import lumina.ui.views.control.propertytesters.CanShowDeviceControlPropertyTester;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Navigation GUI view.
 */
public class NavigationView extends AbstractTreeView {

	/**
	 * Id of the navigation view.
	 */
	public static final String ID = "lumina.views.navigation"; //$NON-NLS-1$

	/**
	 * Context menu identifier to be extended in <code>plugin.xml</code>.
	 */
	private static final String CONTEXT_MENU_ID = "lumina.ui.views.navigation.contextMenu"; //$NON-NLS-1$

	// TODO: These IDs should perhaps be moved to somewhere else.
	/**
	 * ID of the center in blueprint command.
	 */
	private static final String CENTER_IN_BLUPRINT_COMMAND_ID = "lumina.blueprint.commands.centerInBlueprint";

	/**
	 * ID of the show device panel command.
	 */
	private static final String SHOW_DEVICE_PANEL_COMMAND_ID = "lumina.control.commands.showDeviceControl";

	/**
	 * Default width of the status column.
	 */
	private static final int STATUS_COLUMN_WIDTH = 50;

	/**
	 * Default width of the columns that displays the area/channel information.
	 */
	private static final int DEVICE_ADDRESS_COLUMN_WIDTH = 45;

	/**
	 * Default width of the columns that displays name of the timer.
	 */
	private static final int TIMER_NAME_COLUMN_WIDTH = 50;

	/**
	 * Default width of the columns that displays name consumption estimates.
	 */
	private static final int CONSUMPTION_PROPERTY_WIDTH = 60;

	/**
	 * Title of the status column.
	 */
	private static final String STATUS_COLUMN_TITLE = Messages
			.getString("NavigationView.status");

	/**
	 * Title of the columns that displays the area/channel information.
	 */
	private static final String ADDRESS_COLUMN_TITLE = Messages
			.getString("NavigationView.areaChannel");

	/**
	 * Title of the columns that displays name of the timer.
	 */
	private static final String TIMER_NAME_COLUMN_TITLE = Messages
			.getString("NavigationView.timer");

	/**
	 * Title of the columns that displays name consumption estimates.
	 */
	private static final String CONSUMPTION_COLUMN_TITLE = Messages
			.getString("NavigationView.consumption");

	/**
	 * Tool tip message for the status column.
	 */
	private static final String STATUS_COLUMN_TOOLTIP = Messages
			.getString("NavigationView.status.tooltip");

	/**
	 * Tool tip message for the area/channel column.
	 */
	private static final String ADDRESS_COLUMN_TOOLTIP = Messages
			.getString("NavigationView.areaChannel.tooltip");

	/**
	 * Tool tip message for the time name column.
	 */
	private static final String TIMER_NAME_COLUMN_TOOLTIP = Messages
			.getString("NavigationView.timer.tooltip");

	/**
	 * Tool tip message for the time name column.
	 */
	private static final String CONSUMPTION_COLUMN_TOOLTIP = Messages
			.getString("NavigationView.consumption.tooltip");

	/**
	 * URI to contribute the command menu when the application is in expert
	 * mode.
	 */
	// FIXME: The command menu should be implemented.

	// popup:#lumina.ui.views.navigation.contextMenu?after=

	// private static final String COMMAND_ADDITION_URI_APP_ADMIN =
	// "popup:lumina.menus.command";

	/**
	 * URI to contribute the command menu when the application is in operator
	 * mode.
	 */
	// private static final String COMMAND_ADDITION_URI_APP_OPERATOR =
	// "popup:lumina.menus.command";

	/**
	 * Transforms updates to devices into aggregations of power consumption.
	 */
	private DevicePowerAggregator powerAggregator;

	/**
	 * Transforms changes to ModelItems in changes to Devices.
	 */
	private NavigationViewMapper navigationViewMapper;

	/**
	 * The drag n drop listener.
	 */
	private TreeDragAndDropListener dndHandler;

	/**
	 * Constructor.
	 * <p>
	 * Creates the navigation view with the default navigation view content
	 * provider and the default navigation view label provider.
	 * 
	 * @see NavigationViewContentProvider
	 * @see NavigationViewLabelProvider
	 */
	public NavigationView() {
		super(new NavigationViewContentProvider(),
				new NavigationViewLabelProvider());
	}

	@Override
	public CommandContributionItem createInfoToggleContributionItem() {
		return new CommandContributionItem(
				new CommandContributionItemParameter(getViewSite(),
						"lumina.menus.navigation.toggleInfo",
						"lumina.views.commands.toggleInfo",
						CommandContributionItem.STYLE_CHECK));
	}

	/**
	 * Initializes the objects that comprise the item processor.
	 */
	private void initItemProcessor() {
		powerAggregator = new DevicePowerAggregator();
		navigationViewMapper = new NavigationViewMapper();
		navigationViewMapper.addSink(powerAggregator);
	}

	/**
	 * Returns the navigation view mapper.
	 * <p>
	 * If necessary, the item processor initialization will be invoked.
	 * 
	 * @return navigation view mapper
	 */
	@Override
	protected final Sink<ModelItem> getItemSink() {
		if (powerAggregator == null || navigationViewMapper == null) {
			initItemProcessor();
		}

		return navigationViewMapper;
	}

	/**
	 * Returns the device power aggregator.
	 * <p>
	 * If necessary, the item processor initialization will be invoked.
	 * 
	 * @return device power aggregator
	 */
	@Override
	protected final Source<AggregateResult> getAggregateResultSource() {
		if (powerAggregator == null || navigationViewMapper == null) {
			initItemProcessor();
		}

		return powerAggregator;
	}

	@Override
	protected void handleDoubleClick(ISelection selection) {
		if (CanCenterInPlanPropertyTester.canCenterInBluePrint(selection)) {
			CommandUtils.executeCommand(CENTER_IN_BLUPRINT_COMMAND_ID);
		}

		if (selection instanceof StructuredSelection) {
			if (CanShowDeviceControlPropertyTester
					.canDisplayDeviceControl((StructuredSelection) selection)) {
				CommandUtils.executeCommand(SHOW_DEVICE_PANEL_COMMAND_ID);
			}
		}

	}

	/**
	 * Creates the drag-n-drop support for the view.
	 */
	@Override
	protected final void createDNDSupport() {
		final Transfer[] transferTypes = new Transfer[] { DeviceTransfer
				.getInstance() };

		dndHandler = new TreeDragAndDropListener(this.getSite()
				.getWorkbenchWindow(), getTreeViewer(),
				DeviceTransfer.getInstance(), Capability.DEVICE_EDIT_TREE);

		getTreeViewer().addDragSupport(
				DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT,
				transferTypes, dndHandler);

		getTreeViewer().addDropSupport(DND.DROP_MOVE | DND.DROP_COPY,
				transferTypes, dndHandler);

	}

	/**
	 * Creates the contextual menu.
	 */
	@Override
	protected final void createContextMenu() {
		super.createContextMenu();

		/*
		 * Temporary removed due to bluescreen
		 */
		// IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
		// .getService(IMenuService.class);
		// final DeviceMenuContributionFactory deviceCommandMenuFactory;
		// if (UserMode.getMode() == lumina.ADMIN) {
		// deviceCommandMenuFactory = new DeviceMenuContributionFactory(
		// COMMAND_ADDITION_URI_APP_ADMIN);
		// }
		// else if (UserMode.getMode() == lumina.OPERATOR) {
		// deviceCommandMenuFactory = new DeviceMenuContributionFactory(
		// COMMAND_ADDITION_URI_APP_OPERATOR);
		// }
		// else {
		// deviceCommandMenuFactory = null;
		// }
		//
		// if (deviceCommandMenuFactory != null) {
		// menuService.addContributionFactory(deviceCommandMenuFactory);
		// this.getSite().getPage().addSelectionListener(deviceCommandMenuFactory);
		// }
	}

	/**
	 * Gets the selection being dragged.
	 * 
	 * @return the current selection being dragged or <code>null</code> if
	 *         nothing is being dragged.
	 */
	public final ISelection getDraggingSelection() {
		if (dndHandler.isDragging()) {
			return getSelected();
		} else {
			return null;
		}
	}

	/**
	 * Returns the context menu identifier.
	 * 
	 * @return context menu identifier
	 */
	@Override
	protected final String getContextMenuId() {
		return CONTEXT_MENU_ID;
	}

	/**
	 * Returns the action handler for deleting a navigation item.
	 * 
	 * @return item delete action handler
	 */
	@Override
	protected final IAction getDeleteHandler() {
		return new DeleteNavigationItemActionHandler(getWorkbenchWindow());
	}

	/**
	 * Creates the columns for the item information.
	 * <p>
	 * Builds the tree columns for status, area, timer and power consumption,
	 * 
	 * @param tree
	 *            tree
	 * @return tree column array
	 */
	@Override
	public final TreeColumn[] createColumns(final Tree tree) {
		final TreeColumn statusPropertyColumn = new TreeColumn(tree, SWT.NONE);
		statusPropertyColumn.setWidth(STATUS_COLUMN_WIDTH);
		statusPropertyColumn.setText(STATUS_COLUMN_TITLE);
		statusPropertyColumn.setAlignment(SWT.RIGHT);
		statusPropertyColumn.setToolTipText(STATUS_COLUMN_TOOLTIP);

		final TreeColumn addressPropertyColumn = new TreeColumn(tree, SWT.NONE);
		addressPropertyColumn.setWidth(DEVICE_ADDRESS_COLUMN_WIDTH);
		addressPropertyColumn.setText(ADDRESS_COLUMN_TITLE);
		addressPropertyColumn.setAlignment(SWT.RIGHT);
		addressPropertyColumn.setToolTipText(ADDRESS_COLUMN_TOOLTIP);

		final TreeColumn timerPropertyColumn = new TreeColumn(tree, SWT.NONE);
		timerPropertyColumn.setWidth(TIMER_NAME_COLUMN_WIDTH);
		timerPropertyColumn.setText(TIMER_NAME_COLUMN_TITLE);
		timerPropertyColumn.setAlignment(SWT.RIGHT);
		timerPropertyColumn.setToolTipText(TIMER_NAME_COLUMN_TOOLTIP);

		final TreeColumn powerPropertyColumn = new TreeColumn(tree, SWT.NONE);
		powerPropertyColumn.setWidth(CONSUMPTION_PROPERTY_WIDTH);
		powerPropertyColumn.setText(CONSUMPTION_COLUMN_TITLE);
		powerPropertyColumn.setAlignment(SWT.RIGHT);
		powerPropertyColumn.setToolTipText(CONSUMPTION_COLUMN_TOOLTIP);

		return new TreeColumn[] { statusPropertyColumn, addressPropertyColumn,
				timerPropertyColumn, powerPropertyColumn };
	}

	/**
	 * Creates the actions for the navigation view's toolbar.
	 */
	public final void createViewActions() {
		// final Action collapseAction = new Action(Messages.getString("NavigationView.expandOrCollapse")) { //$NON-NLS-1$
		// boolean collapsed = true;
		//
		// public void run() {
		// if (collapsed) {
		// getTreeViewer().collapseAll();
		// setImageDescriptor(lumina.Activator.getImageDescriptor("/icons/actions/expand.png")); //$NON-NLS-1$
		//
		// }
		// else {
		// getTreeViewer().expandAll();
		// setImageDescriptor(lumina.Activator.getImageDescriptor("/icons/actions/collapse.png")); //$NON-NLS-1$
		//
		// }
		// collapsed = !collapsed;
		// }
		// };
		//
		// collapseAction.setImageDescriptor(lumina.Activator.getImageDescriptor("/icons/actions/collapse.png")); //$NON-NLS-1$
		//
		// final IToolBarManager mgr =
		// getViewSite().getActionBars().getToolBarManager();
		// mgr.insertAfter("lumina.menus.navigation.toggleInfo",
		// collapseAction);
	}

	/**
	 * Checks if an update must be applied recursivelly.
	 * 
	 * @param object
	 *            object
	 * @return true
	 */
	@Override
	protected final boolean mustUpdateRecursively(Object object) {
		return false;
	}
}
