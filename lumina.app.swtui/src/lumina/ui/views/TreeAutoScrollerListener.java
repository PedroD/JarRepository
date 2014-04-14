package lumina.ui.views;

import lumina.ui.jface.TreeViewUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Automatically scrolls the trees.
 */
public class TreeAutoScrollerListener implements Listener {
	private static final int SCROLL_SPEED = 40;

	private Runnable heartBeat = new Runnable() {
		public void run() {
			if (!tracking || scrolledTree == null || scrolledTree.isDisposed()) {
				return;
			}

			final Point cursor = display.map(null, scrolledTree,
					display.getCursorLocation());
			TreeViewUtils.scroll(scrolledTree, cursor.x, cursor.y);
			display.timerExec(SCROLL_SPEED, heartBeat);
		}
	};

	private volatile boolean tracking;

	/**
	 * The workbench window where the scrolledTree is located. Used to obtain
	 * the the selection service.
	 */
	private final IWorkbenchWindow workbenchWindow;

	private final Display display;

	private volatile Tree scrolledTree;

	/**
	 * Constructor.
	 * <p>
	 * Sets the parameters for the tree auto scroller listener.
	 * 
	 * @param window
	 *            workbench window
	 */
	public TreeAutoScrollerListener(final IWorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException("The window must be assigned");
		}

		workbenchWindow = window;
		display = workbenchWindow.getShell().getDisplay();
	}

	/**
	 * Registers itself as {@link org.eclipse.swt.SWT#MouseEnter} and
	 * {@link org.eclipse.swt.SWT#MouseExit} tree listeners.
	 * 
	 * @param tree
	 *            tree to register to
	 */
	public final void register(final Tree tree) {
		tree.addListener(SWT.MouseEnter, this);
		tree.addListener(SWT.MouseExit, this);
		scrolledTree = tree;
	}

	/**
	 * Remove itself from {@link org.eclipse.swt.SWT#MouseEnter} and
	 * {@link org.eclipse.swt.SWT#MouseExit} tree listeners.
	 * 
	 * @param tree
	 *            tree to deregister from
	 */
	public final void remove(final Tree tree) {
		tree.removeListener(SWT.MouseExit, this);
		tree.removeListener(SWT.MouseEnter, this);
	}

	/**
	 * Event handler for the registered listeners.
	 * 
	 * @param event
	 *            event
	 */
	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.MouseEnter:
			tracking = true;
			display.timerExec(0, heartBeat);
			break;
		case SWT.MouseExit:
			tracking = false;
			break;
		}
	}
}
