package lumina.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Displays a tool tip of an element of a
 * {@link org.eclipse.jface.viewers.TreeViewer}.
 * <p>
 * Implementers of this class should override the method
 * {@link #getTooltipText(Object)} to supply the appropriate tool tip text.
 */
public abstract class AbstractTreeItemTooltip implements Listener {
	private Tree parentTree;

	private Shell tip = null;
	private Label tooltipLabel = null;

	/**
	 * A listener for the tooltip label region.
	 */
	private static class LabelListener implements Listener {
		private final Tree parentTree;

		LabelListener(final Tree tree) {
			parentTree = tree;
		}

		public void handleEvent(final Event event) {
			Label label = (Label) event.widget;
			Shell shell = label.getShell();

			switch (event.type) {
			case SWT.MouseDown:
				Event e = new Event();
				e.item = (TreeItem) label.getData("_TABLEITEM");
				/*
				 * Assuming table is single select, set the selection as if the
				 * mouse down event went through to the table
				 */
				parentTree.setSelection(new TreeItem[] { (TreeItem) e.item });
				parentTree.notifyListeners(SWT.Selection, e);
				shell.dispose();
				break;
			case SWT.MouseExit:
				shell.dispose();
				break;
			}
		}
	};

	/**
	 * Constructor.
	 */
	public AbstractTreeItemTooltip() {
	}

	/**
	 * Registers itself to the tree listeners.
	 * <p>
	 * It registers itself to {@link org.eclipse.swt.SWT#Dispose},
	 * {@link org.eclipse.swt.SWT#KeyDown},
	 * {@link org.eclipse.swt.SWT#MouseMove} and
	 * {@link org.eclipse.swt.SWT#MouseHover} listeners.
	 * 
	 * @param tree
	 *            tree to register as listener
	 */
	public final void register(final Tree tree) {
		tree.addListener(SWT.Dispose, this);
		tree.addListener(SWT.KeyDown, this);
		tree.addListener(SWT.MouseMove, this);
		tree.addListener(SWT.MouseHover, this);

		parentTree = tree;
	}

	/**
	 * Removes itself to the tree listeners.
	 * <p>
	 * It removes from {@link org.eclipse.swt.SWT#Dispose},
	 * {@link org.eclipse.swt.SWT#KeyDown},
	 * {@link org.eclipse.swt.SWT#MouseMove} and
	 * {@link org.eclipse.swt.SWT#MouseHover} listeners.
	 * 
	 * @param tree
	 *            tree to register as listener
	 */
	public final void unregister(final Tree tree) {
		parentTree = null;
		if (tree != null && !tree.isDisposed()) {
			tree.removeListener(SWT.MouseHover, this);
			tree.removeListener(SWT.MouseMove, this);
			tree.removeListener(SWT.Dispose, this);
			tree.removeListener(SWT.KeyDown, this);
		}
	}

	/**
	 * Handles the event to which it has been registered.
	 * <p>
	 * Handles the {@link org.eclipse.swt.SWT#Dispose},
	 * {@link org.eclipse.swt.SWT#KeyDown},
	 * {@link org.eclipse.swt.SWT#MouseMove} and
	 * {@link org.eclipse.swt.SWT#MouseHover} events.
	 * 
	 * @param event
	 *            event
	 * @see AbstractTreeItemTooltip#register(Tree)
	 */
	public final void handleEvent(final Event event) {
		switch (event.type) {
		case SWT.Dispose:
		case SWT.KeyDown:
		case SWT.MouseMove: {
			if (tip != null) {
				tip.dispose();
				tip = null;
				tooltipLabel = null;
			}
			break;
		}

		case SWT.MouseHover: {
			if (parentTree == null) {
				break;
			}

			final TreeItem treeItem = parentTree.getItem(new Point(event.x,
					event.y));

			if (treeItem != null) {
				if (tip != null && !tip.isDisposed())
					tip.dispose();

				final Shell shell = treeItem.getParent().getShell();
				tip = new Shell(shell, SWT.ON_TOP | SWT.TOOL);
				tip.setLayout(new FillLayout());

				final Display display = shell.getDisplay();
				tooltipLabel = new Label(tip, SWT.NONE);
				tooltipLabel.setForeground(display
						.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
				tooltipLabel.setBackground(display
						.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
				tooltipLabel.setData("_TABLEITEM", treeItem);

				final Object data = treeItem.getData();
				final String tooltipText = getTooltipText(data);

				if (tooltipText != null) {
					tooltipLabel.setText(tooltipText);

					final LabelListener labelListener = new LabelListener(
							treeItem.getParent());

					tooltipLabel.addListener(SWT.MouseExit, labelListener);
					tooltipLabel.addListener(SWT.MouseDown, labelListener);

					final Point size = tip
							.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					final Rectangle rect = treeItem.getBounds(0);
					final Point pt = treeItem.getParent().toDisplay(rect.x,
							rect.y);

					// XXX: Inset not working as expected on
					// the left hand side.

					final int leftIndent = 20;
					final int inset = 2;

					tip.setBounds(pt.x + leftIndent, pt.y, size.x + inset,
							size.y + inset);
					tip.setVisible(true);
				}
			}
		}
		}
	};

	/**
	 * Obtains the tool tip text of an item.
	 * <p>
	 * Descending classed are expected to implement this method to provide the
	 * tool tip text for the item.
	 * 
	 * @param data
	 *            the tree item data
	 * @return a string with the tool tip text to be displayed or null if the
	 *         tool tip should not be shown.
	 */
	protected abstract String getTooltipText(final Object data);

}
