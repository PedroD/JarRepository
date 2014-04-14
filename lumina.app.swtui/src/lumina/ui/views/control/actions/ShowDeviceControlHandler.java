package lumina.ui.views.control.actions;

import lumina.kernel.Logger;
import lumina.ui.views.control.ControlView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.log.LogService;

/**
 * Handler for displaying the control for the currently selected device.
 * <p>
 * Brings the {@link ControlView} to front and restores the focus to the current
 * view.
 */
public class ShowDeviceControlHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent execEvent) throws ExecutionException {
		final IWorkbenchWindow wbWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (wbWindow != null) {
			final IWorkbenchPage page = wbWindow.getActivePage();

			if (page != null) {
				try {
					IWorkbenchPart activePart = page.getActivePart();
					IViewPart view = page.findView(ControlView.ID);

					if (activePart != view) {
						page.showView(ControlView.ID);
						page.showView(activePart.getSite().getId());
					}
				} catch (PartInitException ex) {
					Logger.getInstance().log(LogService.LOG_ERROR,
							"Could not show view", ex); // NON-NLS-1
				}
			}
		}

		return null;

	}
}
