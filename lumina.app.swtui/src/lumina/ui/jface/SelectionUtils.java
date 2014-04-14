package lumina.ui.jface;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.services.WorkbenchSourceProvider;
import org.eclipse.ui.services.ISourceProviderService;

/**
 * Utility class for dealing with selections.
 * 
 * @see ISelection
 * @see IStructuredSelection
 * @see ITreeSelection
 */
@SuppressWarnings("restriction")
// Discouraged access to CurrentSelectionSourceProvider
public final class SelectionUtils {

	/**
	 * The name of the {@link ISourceProvider} that deals with selections.
	 */
	private static final String SELECTION_PROVIDER_NAME = "selection";

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private SelectionUtils() {
	}

	/**
	 * Returns the selection provider from the specified workbench window.
	 * 
	 * @param workbenchWindow
	 *            workbench window
	 * @return selection provider or null if not available
	 */
	public static ISelectionProvider getSelectionProvider(
			final IWorkbenchWindow workbenchWindow) {
		ISelectionProvider selectionProvider = null;

		final IWorkbenchPart workbenchPart = workbenchWindow.getPartService()
				.getActivePart();

		if (workbenchPart != null) {
			final IWorkbenchPartSite site = workbenchPart.getSite();
			if (site != null) {
				selectionProvider = workbenchPart.getSite()
						.getSelectionProvider();
			}
		}

		return selectionProvider;
	}

	/**
	 * Obtains the elements contained on a selection.
	 * 
	 * @param selection
	 *            a selection
	 * @return the elements contained in the selection if the selection is an
	 *         instance of {@link IStructuredSelection} or an instance of
	 *         {@link ITreeSelection}; returns <code>null</code> otherwise.
	 */
	public static Object[] getSelection(final ISelection selection) {
		final Object[] selections;
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			selections = structuredSelection.toArray();
		} else if (selection instanceof ITreeSelection) {
			final ITreeSelection treeSelection = (ITreeSelection) selection;
			selections = treeSelection.toArray();
		} else {
			selections = null;
		}

		return selections;
	}

	/**
	 * Selects items based on a {@link ISelection selection} object.
	 * 
	 * @param selection
	 *            the structure containing the selection.
	 * @param selectionProvider
	 *            the selection provider.
	 */
	public static void doSelectItems(final ISelection selection,
			final ISelectionProvider selectionProvider) {
		if (selection != null && selectionProvider != null) {
			selectionProvider.setSelection(selection);
		}
	}

	/**
	 * Selects items based on a list of objects.
	 * 
	 * @param selection
	 *            the objects ot be selected
	 * @param selectionProvider
	 *            the selection provider
	 */
	public static void doSelectItems(final Object[] selection,
			final ISelectionProvider selectionProvider) {
		if (selection != null && selection.length > 0
				&& selectionProvider != null) {
			doSelectItems(new StructuredSelection(selection), selectionProvider);
		}
	}

	/**
	 * Updates the selection source provider.
	 * <p>
	 * Updates the selection of <tt>CurrentSelectionSourceProvider</tt>
	 * triggering the re-evaluation core expressions.
	 * <p>
	 * If the selection argument is <code>null</code>, the current selection of
	 * the workbench window is used if it was specified <code>null</code>.
	 * 
	 * @param window
	 *            the window to get the service from, can be <code>null</code>
	 * @param selection
	 *            the selection to be set, can be <code>null</code>
	 */
	public static void doUpdateSelectionSourceProvider(
			final IWorkbenchWindow window, final ISelection selection) {
		/*
		 * This code has to run thread-protected because it updates the display.
		 * 
		 * In addition, if the code does not run on the display thread,
		 * workbench.getActiveWorkbenchWindow() will return null and the
		 * selection will not be set.
		 */
		final Display display = Display.getDefault();
		display.asyncExec(new Runnable() {
			public void run() {

				final IWorkbench workbench = PlatformUI.getWorkbench();
				if (workbench != null && !workbench.isClosing()) {
					final org.eclipse.ui.services.ISourceProviderService sourceProviderService = (ISourceProviderService) workbench
							.getService(org.eclipse.ui.services.ISourceProviderService.class);
					final ISourceProvider provider = sourceProviderService
							.getSourceProvider(SELECTION_PROVIDER_NAME);

					final ISelection reselectSelection;
					if (selection != null) {
						reselectSelection = selection;
					} else {
						/*
						 * try finding the currently active window
						 */
						final IWorkbenchWindow activeWindow;
						if (window == null) {
							activeWindow = workbench.getActiveWorkbenchWindow();
						} else {
							activeWindow = window;
						}

						/*
						 * try using the window selection service
						 */
						if (activeWindow != null) {
							final ISelectionService selectionService = activeWindow
									.getSelectionService();
							/*
							 * take the current selection from the selection
							 * service
							 */
							reselectSelection = selectionService.getSelection();
						} else {
							reselectSelection = null;
						}
					}

					// FIXME: This code is not injecting the selection
					if (provider instanceof WorkbenchSourceProvider) {
						WorkbenchSourceProvider csp = (WorkbenchSourceProvider) provider;

						/*
						 * The part can be null but if we have no
						 * reselectSelection then do nothing
						 */
						if (reselectSelection != null) {
							csp.selectionChanged(null, reselectSelection);
						}
					}

				} // provider
			} // workbench != null
		});

	}
}
