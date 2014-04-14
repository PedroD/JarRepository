package lumina.ui.views;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lumina.ui.views.navigation.NavigationView;
import lumina.ui.views.timer.TimerView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

/**
 * Handler for toggling the display of information details the Navigation and
 * Timer tree views.
 */
public class ToggleInfoActivityHandler extends AbstractHandler implements
		IElementUpdater {

	/**
	 * The menu element for tracking.
	 */
	private List<UIElement> uiElements = new LinkedList<UIElement>();
	private NavigationView navigationView;
	private TimerView timerView;
	private boolean checked;

	/**
	 * Listener responsible for saving the workbench window.
	 */
	private final IWindowListener windowListener = new IWindowListener() {
		private final IPropertyListener propertyListener = new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				updateElements();
			}
		};

		/**
		 * Window activated event.
		 * 
		 * @param window
		 *            workbench window
		 */
		public void windowActivated(IWorkbenchWindow window) {
			if (navigationView == null) {
				navigationView = (NavigationView) window.getActivePage()
						.findView(NavigationView.ID);

				if (navigationView != null) {
					navigationView.addPropertyListener(propertyListener);
					checked = navigationView.isInfoMode();
				}
			}

			if (timerView == null) {
				timerView = (TimerView) window.getActivePage().findView(
						TimerView.ID);

				if (timerView != null) {
					timerView.addPropertyListener(propertyListener);
					checked = navigationView.isInfoMode();
				}
			}
		}

		/**
		 * Window closed event.
		 * 
		 * @param window
		 *            workbench window
		 */
		public void windowClosed(IWorkbenchWindow window) {
		}

		/**
		 * Window deactivated event.
		 * 
		 * @param window
		 *            workbench window
		 */
		public void windowDeactivated(IWorkbenchWindow window) {
			if (navigationView != null) {
				navigationView.removePropertyListener(propertyListener);
			}

			if (timerView != null) {
				timerView.removePropertyListener(propertyListener);
			}
		}

		/**
		 * Window opened event.
		 * 
		 * @param window
		 *            workbench window
		 */
		public void windowOpened(IWorkbenchWindow window) {
		}
	};

	/**
	 * Updates the state of the menu.
	 */
	private void updateElements() {
		for (UIElement e : uiElements) {
			e.setChecked(checked);
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
	 */
	@Override
	public Object execute(final ExecutionEvent execEvent)
			throws ExecutionException {
		checked = !checked;

		if (navigationView != null) {
			navigationView.setInfoMode(checked);
		}

		if (timerView != null) {
			timerView.setInfoMode(checked);
		}

		updateElements();
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
	@SuppressWarnings("rawtypes")
	public void updateElement(final UIElement element, final Map parameters) {
		uiElements.add(element);

		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			workbench.addWindowListener(windowListener);
			updateElements();
		}
	}

}
