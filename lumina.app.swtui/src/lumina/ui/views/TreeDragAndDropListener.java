package lumina.ui.views;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.transfer.ModelItemTransfer;
import lumina.base.model.transfer.TransferFactory;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.actions.DropItemsAction;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The listener interface for receiving treeDragAndDrop events.
 * <p>
 * The class that is interested in processing a treeDragAndDrop event implements
 * this interface, and the object created with that class is registered with a
 * component using the component's <code>addTreeDragAndDropListener</code>
 * method. When the treeDragAndDrop event occurs, that object's appropriate
 * method is invoked.
 */
public class TreeDragAndDropListener implements DragSourceListener,
		DropTargetListener {

	private final IWorkbenchWindow workbenchWindow;

	/**
	 * The tree viewer.
	 */
	private final TreeViewer treeViewer;

	/**
	 * Transfer object.
	 */
	private ModelItemTransfer modelItemTransfer;

	/**
	 * The required capability.
	 */
	private Capability requiredCapability;

	/**
	 * The type of drag operation currently in progress (copy/move/none). Used
	 * to update the feed back icon of the drag-drop operation.
	 */
	private int dragOperation = DND.DROP_NONE;

	/**
	 * Indicates if the source the current drag operation is being performed on
	 * the local tree.
	 */
	private boolean draggingStartedHere = false;

	/**
	 * Updates the drag cursor on an event.
	 * 
	 * @param event
	 *            the event to be updated.
	 */
	private void dragUpdate(final DropTargetEvent event) {
		event.detail = DND.DROP_NONE;
		if (event.item instanceof TreeItem) {
			final TreeItem treeItem = (TreeItem) event.item;
			final ModelItem dropOverItem = getDropOverItem(treeItem);

			if (DropItemsAction.canPasteOrDrop(getItems(event), dropOverItem)) {
				event.detail = dragOperation;
			}
		}
	}

	/**
	 * Instantiates a new tree drag and drop listener.
	 * 
	 * @param workbenchWindow
	 *            the workbench window
	 * @param treeViewer
	 *            the tree viewer
	 * @param transfer
	 *            the transfer
	 * @param requiredCapability
	 *            the required capability
	 */
	public TreeDragAndDropListener(final IWorkbenchWindow workbenchWindow,
			final TreeViewer treeViewer, final ModelItemTransfer transfer,
			Capability requiredCapability) {
		this.workbenchWindow = workbenchWindow;
		this.treeViewer = treeViewer;
		this.modelItemTransfer = transfer;
		this.requiredCapability = requiredCapability;
	}

	/**
	 * Processes the the drop message.
	 * 
	 * @param event
	 *            the drop event.
	 */
	private void dragTo(final DropTargetEvent event) {
		if (!(event.item instanceof TreeItem)) {
			return;
		}

		if (!Capabilities.canDo(requiredCapability)) {
			return;
		}

		// perform the drop
		final TreeItem treeItem = (TreeItem) event.item;
		final ModelItem dropOverItem = getDropOverItem(treeItem);

		if (dragOperation == DND.DROP_MOVE) {
			final ModelItem[] items = getItems(event);
			if (DropItemsAction.canPasteOrDrop(items, dropOverItem)) {
				treeViewer.getTree().setInsertMark(null, false);
				final boolean isLocal = draggingStartedHere;
				final DropItemsAction dropItemsAction = new DropItemsAction(
						items, dropOverItem, isLocal, workbenchWindow);

				dropItemsAction.run();
			}
		}
	}

	/**
	 * Gets the model item from a tree item.
	 * <p>
	 * Used to over which the drop operation is taking place.
	 * 
	 * @param treeItem
	 *            a tree item
	 * @return the model item represented by the tree item.
	 */
	private ModelItem getDropOverItem(final TreeItem treeItem) {
		final Object data = treeItem.getData();
		if (data instanceof ModelItem) {
			return (ModelItem) data;
		} else {
			return null;
		}
	}

	/**
	 * Gets the items of a drop event.
	 * 
	 * @param event
	 *            the drop event
	 * @return the items of the drop event
	 */
	private ModelItem[] getItems(final DropTargetEvent event) {
		final ModelItem[] items;
		if (draggingStartedHere) {
			items = getSelection();
		} else {
			items = (ModelItem[]) event.data;
		}

		return items;
	}

	/**
	 * Obtains the list of items of the current selection.
	 * 
	 * @return the model items corresponding to the selection of the tree.
	 */
	private ModelItem[] getSelection() {
		final ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			final Object[] selectedObjects = structuredSelection.toArray();
			final ModelItem[] items = ModelUtils.toModelItems(selectedObjects);
			return items;
		}
		return null;
	}

	/**
	 * The lazily created tree scroller used for scrolling the tree
	 * automatically when performing drag and drop operations.
	 */
	private TreeAutoScrollerListener treeScroller;

	/**
	 * Do register scroller.
	 */
	private void doRegisterScroller() {
		if (treeScroller == null && workbenchWindow != null) {
			treeScroller = new TreeAutoScrollerListener(workbenchWindow);
			if (treeViewer != null) {
				treeScroller.register(treeViewer.getTree());
			}
		}
	}

	/**
	 * Do remove scroller.
	 */
	private void doRemoveScroller() {
		if (treeScroller != null && treeViewer != null) {
			treeScroller.remove(treeViewer.getTree());
		}
	}

	/**
	 * Start drag event.
	 * <p>
	 * Processes the start of the drag event.
	 * 
	 * @param event
	 *            drag source event
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(DragSourceEvent)
	 */
	public void dragStart(DragSourceEvent event) {
		final ModelItem[] selection = getSelection();
		final boolean canDrag = ModelUtils.sameType(selection);
		event.doit = canDrag;
		if (canDrag) {
			modelItemTransfer = TransferFactory.getTransferFor(selection);
			if (modelItemTransfer != null) {
				final TransferData[] supportedTypes = modelItemTransfer
						.getSupportedTypes();
				if (supportedTypes != null && supportedTypes.length > 0) {
					event.dataType = supportedTypes[0];
					draggingStartedHere = true;
					doRegisterScroller();
				}
			}
		}
	}

	/**
	 * Checks if is dragging.
	 * 
	 * @return true, if is dragging
	 */
	public boolean isDragging() {
		return draggingStartedHere;
	}

	/**
	 * Define the drag data.
	 * <p>
	 * If data being dragged can be selected it will be kept.
	 * 
	 * @param event
	 *            drag source event
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragSetData(final DragSourceEvent event) {
		final boolean canSetSelection = modelItemTransfer
				.isSupportedType(event.dataType);
		if (canSetSelection) {
			event.data = getSelection();
		}
	}

	/**
	 * Processes the drag finish.
	 * 
	 * @param event
	 *            drag source event
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	public void dragFinished(final DragSourceEvent event) {
		draggingStartedHere = false;
		doRemoveScroller();
	}

	/**
	 * Processes the drag enter.
	 * 
	 * @param event
	 *            drop target event
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragEnter(DropTargetEvent event) {
	}

	/**
	 * Gets the tree selection.
	 * 
	 * @return the tree selection
	 */
	final ModelItem[] getTreeSelection() {
		final ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structSelection = (IStructuredSelection) selection;
			return ModelUtils.toModelItems(structSelection.toArray());
		}
		return null;
	}

	/**
	 * Processes the drag over.
	 * 
	 * @param event
	 *            drop target event
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragOver(final DropTargetEvent event) {
		event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL
				| DND.FEEDBACK_EXPAND | DND.FEEDBACK_INSERT_AFTER;
		if (event.detail != DND.DROP_NONE) {
			dragOperation = event.detail;

			if (event.item instanceof TreeItem) {
				final TreeItem treeItem = (TreeItem) event.item;
				treeViewer.getTree().setInsertMark(treeItem, false);
			} else {
				treeViewer.getTree().setInsertMark(null, false);
			}
		}

		dragUpdate(event);
	}

	/**
	 * Processes the drag operation changed.
	 * 
	 * @param event
	 *            drop target event
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragOperationChanged(DropTargetEvent event) {
		if (event.detail != DND.DROP_NONE) {
			dragOperation = event.detail;
		}

		dragUpdate(event);
	}

	/**
	 * Processes the drag leave.
	 * 
	 * @param event
	 *            drop target event
	 * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dragLeave(DropTargetEvent event) {
		treeViewer.getTree().setInsertMark(null, false);
	}

	/**
	 * Processes the drop accept.
	 * 
	 * @param event
	 *            drop target event
	 * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void dropAccept(DropTargetEvent event) {
		treeViewer.getTree().setInsertMark(null, false);
	}

	/**
	 * Processes the drop event.
	 * 
	 * @param event
	 *            drop target event
	 * @see org.eclipse.swt.dnd.DropTargetListener#drop(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	public void drop(DropTargetEvent event) {
		dragTo(event);

		draggingStartedHere = false;
	}
}
