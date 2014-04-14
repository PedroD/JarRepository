package lumina.ui.views.blueprint.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lumina.kernel.Logger;
import lumina.ui.perspectives.PerspectiveHelper;
import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.osgi.service.log.LogService;

/**
 * Handler for the expand/retract of the plan view.
 */
public class MaximizePlanActivityHandler extends AbstractHandler implements
		IElementUpdater {

	/**
	 * The menu elements for tracking.
	 */
	private List<UIElement> uiElements = new LinkedList<UIElement>();

	/**
	 * The workbench window obtained when the view is activated.
	 */
	private IWorkbenchWindow workbenchWindow;

	/**
	 * Plan view resize listener.
	 * <p>
	 * This listener is responsible for detecting the changes in size of the
	 * plan view triggered by double-clicking the view tab to maximize it.
	 * Whenever this happens the menu state is updated.
	 */
	private final ControlListener controlListener = new ControlListener() {
		public void controlMoved(final ControlEvent e) {
		}

		/**
		 * Detects the expansion of the plan view and updates the state of the
		 * menu status property.
		 * <p>
		 * It is also called upon resize. The update to the menu status is
		 * performed asynchronously because when the event notification occurs
		 * the {@link IWorkbenchPage#isPageZoomed()} returns the old value.
		 * 
		 * @param e
		 *            the resize event
		 */
		public void controlResized(final ControlEvent e) {
			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					updateElements();
				}
			});
		}
	};

	/**
	 * Listener responsible for saving the workbench window.
	 */
	private final IWindowListener windowListener = new IWindowListener() {
		private BlueprintView planView;

		public void windowActivated(IWorkbenchWindow window) {
			workbenchWindow = window;

			if (planView == null) {
				planView = BlueprintView.findBlueprintView();

				if (planView != null) {
					planView.addControlListener(controlListener);
				}
			}
		}

		public void windowClosed(IWorkbenchWindow window) {
		}

		public void windowDeactivated(IWorkbenchWindow window) {
			if (planView != null) {
				planView.removeControlListener(controlListener);
			}
		}

		public void windowOpened(IWorkbenchWindow window) {
		}
	};

	/**
	 * Updates the state of the menu.
	 */
	private void updateElements() {
		if (workbenchWindow == null) {
			return;
		}

		final IWorkbenchPage page = workbenchWindow.getActivePage();
		if (page == null) {
			return;
		}

		final IWorkbenchPart activePart = page.getActivePart();
		final Display display = workbenchWindow.getShell().getDisplay();
		if (display != null && activePart != null
				&& activePart.getSite().getId().equals(BlueprintView.ID)) {
			display.asyncExec(new Runnable() {
				public void run() {
					for (UIElement e : uiElements) {
						if (e != null) {
							e.setChecked(PerspectiveHelper
									.isPlanMaximized(page));
						}
					}
				}
			});
		}
	}

	/**
	 * Toggles the plan view expanded state.
	 * <p>
	 * Switches to the plan view and then toggles its zoom state.
	 * 
	 * @param execEvent
	 *            the execution event
	 * @return <code>null</code>
	 * @throws ExecutionException
	 *             never thrown
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(final ExecutionEvent execEvent)
			throws ExecutionException {
		final IWorkbenchWindow wbWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (wbWindow != null) {
			final IWorkbenchPage page = wbWindow.getActivePage();

			if (page != null) {
				try {
					page.showView(BlueprintView.ID);
				} catch (PartInitException e) {
					Logger.getInstance().log(LogService.LOG_ERROR,
							"Could not show view", e); // NON-NLS-1
				}
			}

			if (page != null) {
				PerspectiveHelper.togglePlanMaximized(page);
				updateElements();
			}
		}

		return null;
	}

	/**
	 * Saves the UI element and hooks a window listener.
	 * 
	 * @param element
	 *            the element to be saved
	 * @param parameters
	 *            ignored
	 */
	@Override
	public void updateElement(final UIElement element,
			@SuppressWarnings("rawtypes") final Map parameters) {
		uiElements.add(element);

		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			workbench.addWindowListener(windowListener);

			updateElements();
		}
	}
}
