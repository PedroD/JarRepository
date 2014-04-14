package lumina.ui.views;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.kernel.Logger;
import lumina.ui.actions.RenameItemAction;
import lumina.ui.actions.retarget.DeselectAllRetargetAction;
import lumina.ui.actions.retarget.RenameRetargetAction;
import lumina.ui.actions.retarget.SelectAllRetargetAction;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.handlers.HandlerInterceptionService;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.osgi.service.log.LogService;

import codebase.Strings;


/**
 * A widget listener that edits a tree item.
 * <p>
 * The {@link #handleEvent(Event)} method of this listener will invoke
 * {@link #editTreeItem(TreeItem, IWorkbenchWindow)} static method that
 * instantiates a new {@link TreeItemInlineEditListener.ItemNameEditor}.
 */
public final class TreeItemInlineEditListener implements Listener {
	/**
	 * The workbench window where the tree is located. Used to obtain the the
	 * selection service.
	 */
	private final IWorkbenchWindow workbenchWindow;

	/**
	 * Last tree item selected. Used for detecting the repeating click.
	 */
	private final TreeItem[] lastItem = new TreeItem[1];

	/**
	 * The current tree item being edited.
	 */
	private TreeItem item = null;

	/**
	 * Creates the tree item inline edit listener.
	 * 
	 * @param window
	 *            workbench window
	 */
	public TreeItemInlineEditListener(final IWorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException("The window must be assigned");
		}

		workbenchWindow = window;
	}

	/**
	 * Registers itself as a {@link org.eclipse.swt.SWT#Selection},
	 * {@link org.eclipse.swt.SWT#MouseDown} and
	 * {@link org.eclipse.swt.SWT#MouseUp} tree event listeners.
	 * 
	 * @param tree
	 *            tree to listen the events from
	 */
	public void register(final Tree tree) {
		tree.addListener(SWT.Selection, this);
		tree.addListener(SWT.MouseDown, this);
		tree.addListener(SWT.MouseUp, this);
	}

	/**
	 * Removes itself from the {@link org.eclipse.swt.SWT#Selection},
	 * {@link org.eclipse.swt.SWT#MouseDown} and
	 * {@link org.eclipse.swt.SWT#MouseUp} tree event listeners.
	 * 
	 * @param tree
	 *            tree to unregister the listener from
	 */
	public void remove(final Tree tree) {
		tree.removeListener(SWT.MouseUp, this);
		tree.removeListener(SWT.MouseDown, this);
		tree.removeListener(SWT.Selection, this);
	}

	/**
	 * Handles the {@link org.eclipse.swt.SWT#Selection},
	 * {@link org.eclipse.swt.SWT#MouseDown} and
	 * {@link org.eclipse.swt.SWT#MouseUp} events.
	 * 
	 * @param event
	 *            event to handle
	 */
	public void handleEvent(final Event event) {
		switch (event.type) {
		case SWT.MouseDown:
			item = null;
			break;
		case SWT.Selection:
			item = (TreeItem) event.item;
			break;
		case SWT.MouseUp:
			boolean modifierKeyPressed = (event.stateMask & SWT.MODIFIER_MASK) != 0;
			if (!modifierKeyPressed) {
				if (item != null && item == lastItem[0]) {
					editTreeItem(item, workbenchWindow);
				}
				lastItem[0] = item;
			}
			break;
		default:
			break;
		}

	}

	/**
	 * Editor widget for tree item names.
	 * <p>
	 * Some of the fields here may seem superfluous but they are needed for to
	 * make sure that this class remains static.
	 */
	static class ItemNameEditor extends Composite implements ISelectionListener {

		/**
		 * The workbench window object.
		 */
		private final IWorkbenchWindow workbenchWindow;

		/**
		 * The selection service used to subscribe the selection events.
		 */
		private final ISelectionService selectionService;

		/**
		 * The tree item being.
		 */
		private final TreeItem treeItem;

		/**
		 * The where the item is being edited.
		 */
		private final Tree tree;

		/**
		 * The initial textEditBox of the (the item name).
		 */
		private final String initialText;

		/**
		 * The tree editor object created to edit the item.
		 */
		private final TreeEditor editor;

		/**
		 * The textEditBox widget where the item name is edited.
		 */
		private final Text textEditBox;

		/**
		 * Gets the view site relative to workbench window.
		 * 
		 * @return the current view site or <code>null</code> if one could not
		 *         be found
		 */
		private IViewSite getViewSite() {
			final IWorkbenchPart part = workbenchWindow.getPartService()
					.getActivePart();
			if (part instanceof IViewPart) {
				final IViewPart viewPart = (IViewPart) part;
				final IWorkbenchPartSite s = viewPart.getSite();
				if (s instanceof IViewSite) {
					return (IViewSite) s;
				}
			}
			return null;
		}

		/**
		 * Refreshes the enabled/disabled statuses of the actions in the menus
		 * and bars.
		 */
		private void updateStatuses() {
			final IViewSite viewSite = getViewSite();
			if (viewSite != null) {
				final IActionBars bars = viewSite.getActionBars();
				bars.updateActionBars();
				// Logger.getInstance().log(LogService.LOG_DEBUG,"ACTION BARS UPDATED");
			}

			if (workbenchWindow != null) {
				SelectionUtils.doUpdateSelectionSourceProvider(workbenchWindow,
						null);
			}
		}

		private static final String UNDO_ACTION_ID = ActionFactory.UNDO.getId();
		private static final String REDO_ACTION_ID = ActionFactory.REDO.getId();
		private static final String CUT_COMMAND_ID = "org.eclipse.ui.edit.cut";
		private static final String COPY_COMMAND_ID = "org.eclipse.ui.edit.copy";
		private static final String PASTE_COMMAND_ID = "org.eclipse.ui.edit.paste";
		private static final String DELETE_ACTION_ID = ActionFactory.DELETE
				.getId();
		private static final String SELECT_ALL_ACTION_ID = SelectAllRetargetAction.ID;
		private static final String DESELECT_ALL_ACTION_ID = DeselectAllRetargetAction.ID;
		private static final String RENAME_ACTION_ID = RenameRetargetAction.ID;

		private IAction savedUndoAction;
		private IAction savedRedoAction;
		// FIXME: This can perhaps be removed
		// private IAction savedCutAction;
		// private IAction savedCopyAction;
		// private IAction savedPasteAction;
		private IAction savedDeleteAction;
		private IAction savedSelectAllAction;
		private IAction savedDeselectAllAction;
		private IAction savedRenameAction;

		private final IHandler cutHandler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event)
					throws ExecutionException {
				if (textEditBox != null && !textEditBox.isDisposed()) {
					textEditBox.cut();
					updateStatuses();
				}
				return null;
			}
		};

		private final IHandler copyHandler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event)
					throws ExecutionException {
				if (textEditBox != null && !textEditBox.isDisposed()) {
					textEditBox.copy();
					updateStatuses();
				}
				return null;
			}
		};

		private final IHandler pasteHandler = new AbstractHandler() {
			@Override
			public Object execute(ExecutionEvent event)
					throws ExecutionException {
				if (textEditBox != null && !textEditBox.isDisposed()) {
					textEditBox.paste();
					updateStatuses();
				}
				return null;
			}
		};

		private final IAction deleteAction = new Action() {
			@Override
			public void run() {
				// Logger.getInstance().log(LogService.LOG_DEBUG,"DELETE CALLED");
				if (textEditBox != null && !textEditBox.isDisposed()) {
					final Point p = textEditBox.getSelection();
					if (p.y >= p.x) {
						final String text = textEditBox.getText();
						textEditBox.setText(Strings.trim(text, p.x, p.y));
						textEditBox.setSelection(p.x);
					}
				}
			}
		};

		private final IAction selectAllAction = new Action() {
			@Override
			public void run() {
				if (textEditBox != null && !textEditBox.isDisposed()) {
					textEditBox.selectAll();
				}
			}
		};

		private final IAction deselectAllAction = new Action() {
			@Override
			public void run() {
				if (textEditBox != null && !textEditBox.isDisposed()) {
					textEditBox.clearSelection();
				}
			}
		};

		/**
		 * Tacks whether the handlers of edit operations have been redirected to
		 * local handlers of the the inline editor.
		 */
		private boolean localHandlersActive = false;

		/**
		 * Saves the workbench action handlers and point the action handlers to
		 * the local edit actions.
		 */
		private synchronized void swapEditActionHandlers() {
			final IViewSite viewSite = getViewSite();
			if (!localHandlersActive && viewSite != null) {
				Logger.getInstance().log(LogService.LOG_DEBUG,"SWAPING HANDLERS ");
				HandlerInterceptionService.getInstance().registerInterception(
						CUT_COMMAND_ID, cutHandler);
				HandlerInterceptionService.getInstance().registerInterception(
						COPY_COMMAND_ID, copyHandler);
				HandlerInterceptionService.getInstance().registerInterception(
						PASTE_COMMAND_ID, pasteHandler);

				final IActionBars bars = viewSite.getActionBars();
				Logger.getInstance().log(LogService.LOG_DEBUG,"MY ACTION BARS Are: " + bars);

				// NOTE: Undo handler is set to null
				final IAction undo = bars
						.getGlobalActionHandler(UNDO_ACTION_ID);
				if (undo != null) {
					savedUndoAction = undo;
				}
				bars.setGlobalActionHandler(UNDO_ACTION_ID, null);

				// NOTE: Redo handler is set to null
				final IAction redo = bars
						.getGlobalActionHandler(REDO_ACTION_ID);
				if (redo != null) {
					savedRedoAction = redo;
				}
				bars.setGlobalActionHandler(REDO_ACTION_ID, null);

				/*
				 * Actions whose handlers are saved
				 */
				final IAction delete = bars
						.getGlobalActionHandler(DELETE_ACTION_ID);
				// if (delete.toString().contains(
				// "TreeItemInlineEditListener$ItemNameEditor")) {
				// Logger.getInstance().log(LogService.LOG_DEBUG,"DELETE IS MAMATED");
				// }
				if (delete != deleteAction) {
					savedDeleteAction = delete;
				}

				bars.setGlobalActionHandler(DELETE_ACTION_ID, deleteAction);

				final IAction selectAll = bars
						.getGlobalActionHandler(SELECT_ALL_ACTION_ID);
				if (selectAll != selectAllAction) {
					savedSelectAllAction = selectAll;
				}
				bars.setGlobalActionHandler(SELECT_ALL_ACTION_ID,
						selectAllAction);

				final IAction deselectAll = bars
						.getGlobalActionHandler(DESELECT_ALL_ACTION_ID);
				if (deselectAll != deselectAllAction) {
					savedDeselectAllAction = deselectAll;
				}
				bars.setGlobalActionHandler(DESELECT_ALL_ACTION_ID,
						deselectAllAction);

				/*
				 * NOTE: Rename handler is set to null because we don't want
				 * rename to be enabled. We are already renaming!
				 */
				final IAction rename = bars
						.getGlobalActionHandler(RENAME_ACTION_ID);
				if (rename != null) {
					savedRenameAction = rename;
				}
				bars.setGlobalActionHandler(RENAME_ACTION_ID, null);

				updateStatuses();
				localHandlersActive = true;
			}
		}

		private synchronized void restoreActionHandlers() {
			final IViewSite viewSite = getViewSite();
			if (localHandlersActive && viewSite != null) {
				Logger.getInstance().log(LogService.LOG_DEBUG,"ACTION HANDLERS RESTORED");
				final IActionBars bars = viewSite.getActionBars();
				HandlerInterceptionService.getInstance().removeInterception(
						CUT_COMMAND_ID);
				HandlerInterceptionService.getInstance().removeInterception(
						COPY_COMMAND_ID);
				HandlerInterceptionService.getInstance().removeInterception(
						PASTE_COMMAND_ID);

				bars.setGlobalActionHandler(UNDO_ACTION_ID, savedUndoAction);

				bars.setGlobalActionHandler(REDO_ACTION_ID, savedRedoAction);

				// FIXME: This can perhaps be removed
				// bars.setGlobalActionHandler(CUT_COMMAND_ID, savedCutAction);
				// bars.setGlobalActionHandler(COPY_COMMAND_ID,
				// savedCopyAction);
				// bars.setGlobalActionHandler(PASTE_COMMAND_ID,
				// savedPasteAction);

				bars.setGlobalActionHandler(DELETE_ACTION_ID, null);
				// bars.updateActionBars();

				bars.setGlobalActionHandler(DELETE_ACTION_ID, savedDeleteAction);
				// bars.updateActionBars();

				Logger.getInstance().log(LogService.LOG_DEBUG,"RESTORED TO DELETE:" + savedDeleteAction);
				Logger.getInstance().log(LogService.LOG_DEBUG,"INSTED OF deleteAction:" + deleteAction);

				Logger.getInstance().log(LogService.LOG_DEBUG,"REALLY SAVED:"
						+ bars.getGlobalActionHandler(DELETE_ACTION_ID));

				bars.setGlobalActionHandler(SELECT_ALL_ACTION_ID,
						savedSelectAllAction);
				bars.setGlobalActionHandler(DESELECT_ALL_ACTION_ID,
						savedDeselectAllAction);
				bars.setGlobalActionHandler(RENAME_ACTION_ID, savedRenameAction);

				updateStatuses();
				localHandlersActive = false;
			}
		}

		/**
		 * Calls the {@link RenameItemAction} with the current textEditBox of
		 * the textEditBox editor.
		 */
		private void changeItemName() {
			final String currentlySetText = textEditBox.getText();
			if (!currentlySetText.equals(initialText)) {
				final IAction action = new RenameItemAction(treeItem.getData(),
						currentlySetText, workbenchWindow);
				action.run();
			}
		}

		/**
		 * Responds to selection changes.
		 * <p>
		 * When the selection is changed it cancels the editor immediately.
		 * 
		 * @param part
		 *            workbench window
		 * @param selection
		 *            new selection
		 */
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			final Object[] objects = SelectionUtils.getSelection(selection);
			final boolean selectionIsTreeItem = objects.length == 1
					&& treeItem.getData() != null
					&& objects[0] == treeItem.getData();
			if (!selectionIsTreeItem) {
				this.dispose();
			}
		}

		/**
		 * Terminates.
		 * <p>
		 * Restores the action handlers, deregisters the selection listeners and
		 * disposes the depenencies.
		 */
		@Override
		public void dispose() {
			restoreActionHandlers();

			if (textEditBox != null) {
				textEditBox.dispose();
			}

			if (editor != null) {
				editor.dispose();
			}

			if (selectionService != null) {
				selectionService.removeSelectionListener(this);
			}

			super.dispose();
		}

		/**
		 * Item name editor constructor.
		 * <p>
		 * Creates the item name editor GUI and sets the correspondent necessary
		 * listeners.
		 * 
		 * @param item
		 *            a tree item; cannot be <code>null</code>
		 * @param style
		 *            the composite style
		 */
		public ItemNameEditor(final IWorkbenchWindow window,
				final TreeItem item, int style) {
			super(item.getParent(), style);

			workbenchWindow = window;
			selectionService = window.getSelectionService();
			if (selectionService != null) {
				selectionService.addSelectionListener(this);
			}

			treeItem = item;
			initialText = item.getText();
			tree = item.getParent();
			assert tree != null;

			if (tree.isDisposed()) {
				throw new IllegalArgumentException(
						"The item's tree is disposed"); // NON-NLS-1
			}

			editor = new TreeEditor(tree);
			boolean isCarbon = SWT.getPlatform().equals("carbon");
			if (!isCarbon) {
				Color black = tree.getDisplay().getSystemColor(SWT.COLOR_BLACK);
				this.setBackground(black);
			}

			textEditBox = new Text(this, SWT.NONE);
			final int inset;
			if (isCarbon) {
				inset = 0;
			} else {
				inset = 1;
			}

			this.addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event e) {
					Rectangle rect = getClientArea();
					textEditBox.setBounds(rect.x + inset, rect.y + inset,
							rect.width - inset * 2, rect.height - inset * 2);
				}
			});

			textEditBox.addListener(SWT.FocusOut, new Listener() {
				public void handleEvent(final Event e) {
					changeItemName();
					dispose();
				}
			});

			textEditBox.addListener(SWT.Verify, new Listener() {
				public void handleEvent(final Event e) {
					swapEditActionHandlers();

					final String newText = textEditBox.getText();
					final String leftText = newText.substring(0, e.start);
					final String rightText = newText.substring(e.end,
							newText.length());
					final GC gc = new GC(textEditBox);
					final Point extent = gc.textExtent(leftText + e.text
							+ rightText);
					gc.dispose();
					final Point size = textEditBox.computeSize(extent.x,
							SWT.DEFAULT);
					editor.horizontalAlignment = SWT.LEFT;
					final Rectangle itemRect = item.getBounds(), rect = tree
							.getClientArea();
					editor.minimumWidth = Math.max(size.x, itemRect.width)
							+ inset * 2;
					final int left = itemRect.x, right = rect.x + rect.width;
					editor.minimumWidth = Math.min(editor.minimumWidth, right
							- left);
					editor.minimumHeight = size.y + inset * 2;
					editor.layout();
				}
			});

			textEditBox.addListener(SWT.Traverse, new Listener() {
				public void handleEvent(final Event e) {
					switch (e.detail) {
					case SWT.TRAVERSE_RETURN:
						changeItemName();
						dispose();
						e.doit = false;
						break;
					case SWT.TRAVERSE_ESCAPE:
						dispose();
						e.doit = false;
						break;
					}
				}
			});

			editor.setEditor(this, item);
			textEditBox.setText(initialText);
			textEditBox.selectAll();

			swapEditActionHandlers();

			textEditBox.setFocus();
		}
	}

	/**
	 * Edits a tree item inline.
	 * 
	 * @param item
	 *            the tree item to be edited
	 * @param workbenchWindow
	 *            the {@link IWorkbenchWindow} object that is the parent of the
	 *            treeviewer, used for the undo/redo context
	 */
	public static void editTreeItem(final TreeItem item,
			final IWorkbenchWindow workbenchWindow) {

		// verify if the operation is allowed
		if (item == null || item.isDisposed()
				|| !(item.getData() instanceof ModelItem))
			return;
		if (!ModelUtils.canRenameItem((ModelItem) item.getData()))
			return;

		// activate inline edit
		final Tree tree = item.getParent();
		if (!tree.isDisposed()) {
			// getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(),
			// cutAction);

			// final Action cutLocal = new Action() {
			// public void run() {
			// Logger.getInstance().log(LogService.LOG_ERROR,"**** CUT LOCAL *****");
			// }
			// };
			//
			// cutLocal.setEnabled(true);
			// cutLocal.setId(ActionFactory.CUT.getId());
			//
			// getViewSite(workbenchWindow).getActionBars().setGlobalActionHandler(
			// ActionFactory.CUT.getId(), cutLocal);
			//
			// getViewSite(workbenchWindow).getActionBars().updateActionBars();
			//
			// Logger.getInstance().log(LogService.LOG_ERROR,"CUT LOCAL REGISTERED");
			// getViewSite(workbenchWindow).getSelectionProvider().setSelection(null);

			new ItemNameEditor(workbenchWindow, item, SWT.NONE);
		}
	}
}
