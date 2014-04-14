package lumina.ui.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.Floor;
import lumina.base.model.ModelUtils;
import lumina.base.model.Queries;
import lumina.ui.actions.base.ReselectBaseAction;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Select all action.
 * 
 * @see lumina.ui.actions.base.ReselectBaseAction
 * @see org.eclipse.ui.ISelectionListener
 * 
 */
public class SelectAllAction extends ReselectBaseAction implements
		ISelectionListener {

	private final IWorkbenchWindow workbenchWindow;

	/**
	 * updated when the section changes.
	 */
	private IStructuredSelection selection;

	/**
	 * Backup of the original selection (before the expansion).
	 */
	private IStructuredSelection originalBeforeExpansion;

	/**
	 * Flag the indicates weather a select all operation is underway.
	 */
	private boolean isSelectingAll;

	/**
	 * Select all action.
	 * 
	 * @param provider
	 *            selection provider
	 * @param window
	 *            workbench window
	 */
	public SelectAllAction(final ISelectionProvider provider,
			final IWorkbenchWindow window) {
		super(provider);
		workbenchWindow = window;

		setEnabled(false);
		window.getSelectionService().addSelectionListener(this);
	}

	private static boolean sameItems(final Object[] left, final Object[] right) {
		if (left == null || right == null) {
			return false;
		}

		final Set<Object> leftObjs = new HashSet<Object>();
		for (final Object o : left) {
			leftObjs.add(o);
		}
		final Set<Object> rightObjs = new HashSet<Object>();
		for (final Object o : right) {
			rightObjs.add(o);
		}
		return leftObjs.size() == rightObjs.size()
				&& leftObjs.containsAll(rightObjs);
	}

	/**
	 * Trigger for selection change.
	 * 
	 * @param part
	 *            workbench part
	 * @param incoming
	 *            selection
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection incoming) {
		if (!(incoming instanceof IStructuredSelection))
			return;

		selection = (IStructuredSelection) incoming;
		final Object[] selections = selection.toArray();

		boolean canSelect = true;
		for (Object o : selections) {
			if (!ModelUtils.isModelItem(o)) {
				canSelect = false;
			}
		}

		final Object[] extendedSelection = getExtendedSelection(selections);

		isSelectingAll = sameItems(selections, extendedSelection);
		if (isSelectingAll) {
			canSelect = false;
		}

		setEnabled(canSelect);
	}

	/**
	 * Returns an extended selection from the current selected objects.<br/>
	 * Selects the children of a selected object.
	 * 
	 * @param selection
	 *            array with selected objects
	 * @return array with selected objects
	 */
	final Object[] getExtendedSelection(final Object[] selection) {
		final java.util.List<Object> result = new ArrayList<Object>();
		for (int i = 0; i < selection.length; i++) {
			final Object o = selection[i];
			if (o instanceof Device) {
				Device[] devices = Queries.getDeviceSiblings((Device) o);
				for (Device d : devices) {
					if (!result.contains(d)) {
						result.add(d);
					}
				}
			}

			if (o instanceof Area) {
				Area[] area = Queries.getAreaSiblings((Area) o);
				for (Area d : area) {
					if (!result.contains(d)) {
						result.add(d);
					}
				}
			}

			if (o instanceof Floor) {
				Floor[] floors = Queries.getFloorSiblings((Floor) o);
				for (Floor d : floors) {
					if (!result.contains(d)) {
						result.add(d);
					}
				}
			}
		}

		return result.toArray(new Object[0]);
	}

	/**
	 * Reverts to the original selection.
	 */
	public final void revertToOriginalSelection() {
		reselect(originalBeforeExpansion.toArray());
	}

	/**
	 * Checks if the selection can be reverted.
	 * 
	 * @return true if the selection can be reverted, false otherwise
	 */
	public final boolean canRevertSelection() {
		return isSelectingAll && originalBeforeExpansion != null;
	}

	/**
	 * Executes the select all action.
	 */
	public void run() {
		final Object[] selections = selection.toArray();
		final Object[] newSelections = getExtendedSelection(selections);

		final ISelectionService service = workbenchWindow.getSelectionService();
		final ISelection s = service.getSelection();

		if (s instanceof IStructuredSelection) {
			originalBeforeExpansion = (IStructuredSelection) s;
		} else {
			originalBeforeExpansion = null;
		}

		reselect(newSelections);
	}

	/**
	 * Terminates.<br/>
	 * Removes itself from the selection listener.
	 */
	public void dispose() {
		workbenchWindow.getSelectionService().removeSelectionListener(this);
	}
}
