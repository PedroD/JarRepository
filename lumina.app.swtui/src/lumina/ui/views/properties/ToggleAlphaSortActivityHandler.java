package lumina.ui.views.properties;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lumina.kernel.Logger;

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
import org.osgi.service.log.LogService;

/**
 * An command handler for toggling the ordering of properties by name on the
 * {@link PropertiesView}.
 */
public class ToggleAlphaSortActivityHandler extends AbstractHandler implements
		IElementUpdater {

	/**
	 * The menu element for tracking.
	 */
	private List<UIElement> uiElements = new LinkedList<UIElement>();

	/**
	 * Cached reference to the parent properties view.
	 */
	private PropertiesView propertiesView;

	/**
	 * Listener responsible for saving the workbench window.
	 */
	private final IWindowListener windowListener = new IWindowListener() {
		private final IPropertyListener propertyListener = new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				updateElements();
			}
		};

		public void windowActivated(IWorkbenchWindow window) {
			// if (window != null) {
			// window.getPartService().addPartListener(partListener);
			// }

			if (propertiesView == null) {
				propertiesView = (PropertiesView) window.getActivePage()
						.findView(PropertiesView.ID);
			}
			if (propertiesView != null) {
				propertiesView.addPropertyListener(propertyListener);
				updateElements();
			}
		}

		public void windowClosed(IWorkbenchWindow window) {
		}

		public void windowDeactivated(IWorkbenchWindow window) {
			if (propertiesView != null) {
				propertiesView.removePropertyListener(propertyListener);
			}
		}

		public void windowOpened(IWorkbenchWindow window) {
		}
	};

	/**
	 * Updates the state of the menu.
	 */
	private void updateElements() {
		if (propertiesView != null) {
			for (UIElement e : uiElements) {
				e.setChecked(propertiesView.isAlphaOrdered());
			}
		}
	}

	/**
	 * Toggles the plan view expanded state. <br/>
	 * Switches to the plan view and then toggles its zoom state.
	 * 
	 * @param execEvent
	 *            the execution event
	 * @return <code>null</code>
	 * @throws ExecutionException
	 *             never thrown
	 */
	@Override
	public final Object execute(final ExecutionEvent execEvent)
			throws ExecutionException {
		if (propertiesView != null) {
			try {
				propertiesView.setAlphaOrdering(!propertiesView
						.isAlphaOrdered());
			} catch (Exception e) {
				Logger.getInstance().log(LogService.LOG_ERROR,
						"Could not set alpha ordering", e); // NON-NLS-1
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
	public final void updateElement(final UIElement element,
			@SuppressWarnings("rawtypes") final Map parameters) {
		uiElements.add(element);

		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) {
			workbench.addWindowListener(windowListener);
			updateElements();
		}
	}
}
