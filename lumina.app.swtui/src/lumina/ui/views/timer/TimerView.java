package lumina.ui.views.timer;

import lumina.base.model.ModelUtils;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.transfer.TaskTransfer;
import lumina.license.Capabilities.Capability;
import lumina.ui.views.AbstractTreeView;
import lumina.ui.views.TreeDragAndDropListener;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Timer GUI view.
 */
public class TimerView extends AbstractTreeView {

	/**
	 * Id of the navigation view.
	 */
	public static final String ID = "lumina.views.timer"; //$NON-NLS-1$

	/**
	 * The width of the next fire date column.
	 */
	private static final int FIRE_DATE_COLUMN_WIDTH = 95;

	/**
	 * The width of the command column.
	 */
	private static final int COMAND_COLUMN_WIDTH = 110;

	/**
	 * The width of the fire time column.
	 */
	private static final int FIRE_TIME_COLUMN_WIDTH = 45;

	/**
	 * Context menu ID.
	 */
	private static final String CONTEXT_MENU_ID = "lumina.views.timers.contextMenu"; //$NON-NLS-1$

	/**
	 * Constructor for the timer view.
	 */
	public TimerView() {
		super(new TimerViewContentProvider(), new TimerViewLabelProvider());
	}

	/**
	 * Creates the drag-n-drop support.
	 * <p>
	 * Adds the drag-n-drop handler to the timer tree listener.
	 */
	public final void createDNDSupport() {
		final Transfer[] transferTypes = new Transfer[] { TaskTransfer
				.getInstance() };

		final TreeDragAndDropListener dndHandler = new TreeDragAndDropListener(
				this.getSite().getWorkbenchWindow(), getTreeViewer(),
				TaskTransfer.getInstance(), Capability.TIMER_EDIT_TREE);

		getTreeViewer().addDragSupport(
				DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT,
				transferTypes, dndHandler);

		getTreeViewer().addDropSupport(DND.DROP_MOVE | DND.DROP_COPY,
				transferTypes, dndHandler);
	}

	/**
	 * Gets the GUI context menu identifier.
	 * 
	 * @return the context menu id
	 */
	@Override
	protected final String getContextMenuId() {
		return CONTEXT_MENU_ID;
	}

	/**
	 * Gets the timer delete action handler .
	 * 
	 * @return the delete handler
	 */
	@Override
	protected final IAction getDeleteHandler() {
		return new DeleteTimerItemActionHandler(getWorkbenchWindow());
	}

	/**
	 * Creates the columns on the timer tree.
	 * <p>
	 * Creates the time, action and when columns.
	 * 
	 * @param tree
	 *            timer tree
	 * @return array with the tree columns created.
	 */
	@Override
	public final TreeColumn[] createColumns(final Tree tree) {
		final TreeColumn fireTimePropertyColumn = new TreeColumn(tree, SWT.NONE);
		fireTimePropertyColumn.setWidth(FIRE_TIME_COLUMN_WIDTH);
		fireTimePropertyColumn.setText(Messages.getString("TimerView.time")); //$NON-NLS-1$
		fireTimePropertyColumn.setAlignment(SWT.RIGHT);
		fireTimePropertyColumn.setToolTipText(Messages
				.getString("TimerView.executeTime"));

		final TreeColumn commandPropertyColumn = new TreeColumn(tree, SWT.NONE);
		commandPropertyColumn.setWidth(COMAND_COLUMN_WIDTH);
		commandPropertyColumn.setText(Messages.getString("TimerView.action")); //$NON-NLS-1$
		commandPropertyColumn.setAlignment(SWT.LEFT);
		commandPropertyColumn.setToolTipText(Messages
				.getString("TimerView.executeCommand"));

		final TreeColumn fireDatePropertyColumn = new TreeColumn(tree, SWT.NONE);
		fireDatePropertyColumn.setWidth(FIRE_DATE_COLUMN_WIDTH);
		fireDatePropertyColumn.setText(Messages.getString("TimerView.when")); //$NON-NLS-1$
		fireDatePropertyColumn.setAlignment(SWT.LEFT);
		fireDatePropertyColumn.setToolTipText(Messages
				.getString("TimerView.executeDate"));

		return new TreeColumn[] { fireTimePropertyColumn,
				commandPropertyColumn, fireDatePropertyColumn };
	}

	/**
	 * Creates the actions for the navigation view's toolbar.
	 */
	protected final void createViewActions() {
	}

	@Override
	protected CommandContributionItem createInfoToggleContributionItem() {
		return new CommandContributionItem(
				new CommandContributionItemParameter(getViewSite(),
						"lumina.menus.timer.toggleInfo",
						"lumina.views.commands.toggleInfo",
						CommandContributionItem.STYLE_CHECK));
	}

	/**
	 * Checks if it is necessary to update recursively.
	 * 
	 * @param object
	 *            object
	 * @return true
	 */
	@Override
	protected final boolean mustUpdateRecursively(Object object) {
		return true;
	}

	/**
	 * Handler for property change.
	 * <p>
	 * Refreshes the tree item if a property change occurs.
	 * 
	 * @param event
	 *            change property event
	 */
	@Override
	protected final void handlePropertyChange(PropertyChangeEvent event) {
		final String changedProperty = event.getProperty();

		if (PropertyChangeNames.isTaskExecutionStop(changedProperty)) {
			/*
			 * when the task execution is finished, we can update the task item
			 * in the tree so that the time of the next firing of the task is
			 * updated
			 */
			final Object subject = event.getSource();
			assert ModelUtils.isModelItem(subject);

			refreshTreeItem(subject);
		}
	}

}
