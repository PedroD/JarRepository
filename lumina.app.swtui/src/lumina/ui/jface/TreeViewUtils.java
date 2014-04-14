package lumina.ui.jface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lumina.base.model.ModelItem;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;

/**
 * Utilities for {@link TreeViewer} objects.
 */
public final class TreeViewUtils {

	private static final String TAG_TREE_SELECTION_PATHS = "treeSelections";

	private static final String TAG_TREE_EXAPANSION_PATHS = "treeExpansions";

	/**
	 * Scrolls the tree by the given x and y offsets.
	 * 
	 * @param tree
	 *            the tree widget
	 * @param x
	 *            the x offset
	 * @param y
	 *            the y offset
	 */
	public static void scroll(final Tree tree, final int x, final int y) {
		final TreeItem item = tree.getItem(new Point(x, y));
		if (item == null) {
			return;
		}

		final Rectangle area = tree.getClientArea();
		final int headerHeight = tree.getHeaderHeight();
		final int itemHeight = tree.getItemHeight();
		TreeItem nextItem = null;
		if (y < area.y + headerHeight + 2 * itemHeight) {
			nextItem = previousTreeItem(tree, item);
		}
		if (y > area.y + area.height - 2 * itemHeight) {
			nextItem = nextTreeItem(tree, item);
		}
		if (nextItem != null) {
			tree.showItem(nextItem);
		}
	}

	/**
	 * Finds the item preceeding another item in the tree.
	 * 
	 * @param tree
	 *            the tree
	 * @param item
	 *            the item whose preceeding item should be found
	 * @return the preceeding item or <code>null</code> if this item is already
	 *         the first item.
	 */
	public static TreeItem previousTreeItem(final Tree tree, final TreeItem item) {
		if (item == null) {
			return null;
		}

		final TreeItem childItem = item;
		final TreeItem parentItem = childItem.getParentItem();

		final int index;
		if (parentItem == null) {
			index = tree.indexOf(childItem);
		} else {
			index = parentItem.indexOf(childItem);
		}

		if (index == 0) {
			return parentItem;
		} else {
			TreeItem nextItem;
			if (parentItem == null) {
				nextItem = tree.getItem(index - 1);
			} else {
				nextItem = parentItem.getItem(index - 1);
			}

			int count = nextItem.getItemCount();
			while (count > 0 && nextItem.getExpanded()) {
				nextItem = nextItem.getItem(count - 1);
				count = nextItem.getItemCount();
			}
			return nextItem;
		}
	}

	/**
	 * Finds the item following another item in the tree.
	 * 
	 * @param tree
	 *            the tree
	 * @param item
	 *            the item whose following item should be found
	 * @return the following item or <code>null</code> if this item is already
	 *         the last item.
	 */
	static TreeItem nextTreeItem(final Tree tree, final TreeItem item) {
		if (item == null) {
			return null;
		}

		if (item.getExpanded()) {
			return item.getItem(0);
		} else {
			TreeItem childItem = item;
			TreeItem parentItem = childItem.getParentItem();

			int index;
			if (parentItem == null) {
				index = tree.indexOf(childItem);
			} else {
				index = parentItem.indexOf(childItem);
			}

			int count;
			if (parentItem == null) {
				count = tree.getItemCount();
			} else {
				count = parentItem.getItemCount();
			}

			while (true) {
				if (index + 1 < count) {
					if (parentItem == null) {
						return tree.getItem(index + 1);
					} else {
						return parentItem.getItem(index + 1);
					}
				} else {
					if (parentItem == null) {
						return null;
					} else {
						childItem = parentItem;
						parentItem = childItem.getParentItem();

						if (parentItem == null) {
							index = tree.indexOf(childItem);
						} else {
							index = parentItem.indexOf(childItem);
						}

						if (parentItem == null) {
							count = tree.getItemCount();
						} else {
							count = parentItem.getItemCount();
						}

					}
				}
			}
		}
	}

	/**
	 * Collects paths to expanded children of the given element and returns
	 * whether any paths were expanded.
	 * 
	 * @param item
	 *            item to collect expanded paths for
	 * @param expanded
	 *            list to add to
	 * @return whether any paths were found expanded
	 * @throws DebugException
	 */
	private static boolean collectExpandedItems(TreeItem item,
			List<IPath> expanded) {
		if (item.getExpanded()) {
			boolean childExpanded = false;
			final TreeItem[] items = item.getItems();
			for (int i = 0; i < items.length; i++) {
				childExpanded = collectExpandedItems(items[i], expanded)
						|| childExpanded;
			}
			if (!childExpanded) {
				final IPath path = encodeElement(item);
				expanded.add(path);
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Finds the {@link TreeItem} whose {@link ModelItem} has a give id.
	 * 
	 * @param id
	 *            the id of the model item
	 * @param items
	 *            an array of tree items to be searched
	 * @return the {@link TreeItem} oject or <code>null</code> if not found.
	 */
	private static TreeItem findItem(final String id, final TreeItem[] items) {
		for (int i = 0; i < items.length; i++) {
			final TreeItem item = items[i];
			if (getSafeItemID(item).equals(id)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Parses a comma delimited string with paths to an array of paths.
	 * 
	 * @param string
	 *            the input string
	 * @return array with paths
	 */
	private static IPath[] stringToPaths(final String string) {
		final String[] pathStrings = string.split(",");
		final IPath[] paths = new Path[pathStrings.length];
		for (int i = 0; i < pathStrings.length; i++) {
			final IPath path = Path.fromPortableString(pathStrings[i]);
			paths[i] = path;
		}
		return paths;
	}

	/**
	 * Converts an array of paths to a comma delimited string.
	 * 
	 * @param paths
	 *            the input array with paths
	 * @return a comma delimited string
	 */
	private static String pathsToString(final IPath[] paths) {
		final StringBuffer buffer = new StringBuffer();
		for (IPath path : paths) {
			if (buffer.length() > 0) {
				buffer.append(',');
			}
			buffer.append(path.toPortableString());
		}
		return buffer.toString();
	}

	/**
	 * Prevent instantiation.
	 */
	private TreeViewUtils() {
	}

	/**
	 * Obtains the id of the {@link ModelItem} associated with a
	 * {@link TreeItem}.
	 * 
	 * @param item
	 *            the model item
	 * @return the id on {@link ModelItem#NO_ID} if not applicable
	 */
	public static String getSafeItemID(final TreeItem item) {
		if (item != null) {
			final Object data = item.getData();
			if (data instanceof ModelItem) {
				return ((ModelItem) data).getId();
			}
		}

		return ModelItem.NO_ID;
	}

	/**
	 * Encodes a {@link TreeItem} into an {@link IPath} object.
	 * 
	 * @param item
	 *            the tree item
	 * @return an {@link IPath} object containing the ids of the item and its
	 *         ancestors.
	 */
	public static IPath encodeElement(final TreeItem item) {
		final StringBuffer path = new StringBuffer(getSafeItemID(item));
		TreeItem parent = item.getParentItem();
		while (parent != null) {
			path.insert(0, getSafeItemID(parent) + IPath.SEPARATOR);
			parent = parent.getParentItem();
		}
		return new Path(path.toString());
	}

	public static IPath encodeElement(final TreePath treePath) {
		final StringBuffer path = new StringBuffer();

		for (int i = 0; i < treePath.getSegmentCount(); i++) {
			final Object o = treePath.getSegment(i);
			if (o instanceof ModelItem) {
				final ModelItem modelItem = (ModelItem) o;
				if (path.length() > 0) {
					path.append(IPath.SEPARATOR + modelItem.getId());
				} else {
					path.append(modelItem.getId());
				}
			}
		}

		return new Path(path.toString());
	}

	/**
	 * Obtains the {@link TreePath} corresponding to the a {@link ModelItem}.
	 * 
	 * @param path
	 *            a path consisting of ids of {@link ModelItem}s.
	 * @param viewer
	 *            the table viewer
	 * @return the {@link ModelItem} designated by the path specified.
	 */
	public static TreePath decodePath(final IPath path, final TreeViewer viewer) {
		final String[] ids = path.segments();
		final Tree tree = viewer.getTree();
		final List<Object> elements = new ArrayList<Object>();

		TreeItem[] items = tree.getItems();
		boolean pathFound = false;
		for (int i = 0; i < ids.length; i++) {
			final String id = ids[i];
			final TreeItem item = findItem(id, items);
			if (item != null) {
				pathFound = true;
				elements.add(item.getData());
				items = item.getItems();
			}
		}
		if (pathFound) {
			return new TreePath(elements.toArray());
		}
		return null;
	}

	public static TreeSelection restoreTreeSelection(final TreeViewer viewer,
			final IMemento memento) {
		if (memento == null) {
			return new TreeSelection();
		} else {
			final String value = memento
					.getString(TreeViewUtils.TAG_TREE_SELECTION_PATHS);
			if (value != null) {
				final IPath[] paths = stringToPaths(value);
				final List<TreePath> treePaths = new LinkedList<TreePath>();
				for (int i = 0; i < paths.length; i++) {
					final TreePath treePath = TreeViewUtils.decodePath(
							paths[i], viewer);
					if (treePath != null) {
						treePaths.add(treePath);
					}
				}
				return new TreeSelection(treePaths.toArray(new TreePath[0]));
			} else {
				return new TreeSelection();
			}
		}
	}

	/**
	 * Restores the state of the given viewer to this memento's saved state.
	 * 
	 * @param viewer
	 *            viewer to which state is restored
	 * @param memento
	 *            the memento object to retrieve the state from
	 */
	public static void restoreState(final TreeViewer viewer,
			final IMemento memento) {
		if (memento != null) {
			final String value = memento
					.getString(TreeViewUtils.TAG_TREE_EXAPANSION_PATHS);
			if (value != null) {
				final IPath[] paths = stringToPaths(value);
				for (int i = 0; i < paths.length; i++) {
					final IPath path = paths[i];
					if (path != null) {
						final TreePath treePath = decodePath(path, viewer);
						if (treePath != null) {
							viewer.setExpandedState(treePath, true);
						}
					}
				}
			}
		}
	}

	public static void saveTreeSelection(final TreeSelection treeSelection,
			final IMemento memento) {
		if (memento != null) {
			final TreePath[] selection = treeSelection.getPaths();
			final IPath[] savePaths = new IPath[selection.length];

			for (int i = 0; i < selection.length; i++) {
				savePaths[i] = TreeViewUtils.encodeElement(selection[i]);
			}

			memento.putString(TreeViewUtils.TAG_TREE_SELECTION_PATHS,
					pathsToString(savePaths));
		}
	}

	/**
	 * Saves the current state of the given viewer into a memento.
	 * 
	 * @param viewer
	 *            viewer of which to save the state
	 * @param memento
	 *            the memento to save the information into
	 */
	public static void saveState(TreeViewer viewer, final IMemento memento) {
		if (memento != null) {
			final List<IPath> expanded = new ArrayList<IPath>();
			final TreeItem[] items = viewer.getTree().getItems();

			for (int i = 0; i < items.length; i++) {
				collectExpandedItems(items[i], expanded);
			}

			final TreePath[] expandedTreePaths = viewer.getExpandedTreePaths();
			final IPath[] paths = new IPath[expandedTreePaths.length];
			for (int i = 0; i < paths.length; i++) {
				paths[i] = encodeElement(expandedTreePaths[i]);
			}
			memento.putString(TreeViewUtils.TAG_TREE_EXAPANSION_PATHS,
					pathsToString(paths));
		}
	}

}
