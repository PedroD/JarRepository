package lumina.ui.actions;

import lumina.base.model.ProjectModel;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Scan devices action.
 * 
 * @author Fernando Martins
 */
public class ScanDevicesHandler extends AbstractHandler {

	/**
	 * Performs the action of scanning the devices.
	 * 
	 * @param event
	 *            event
	 * @return null
	 * @throws ExecutionException
	 *             not thrown
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ISelection selection = HandlerUtil.getCurrentSelection(event);

		ProjectModel.getInstance().runManualScan(selection);

		return null;
	}
}
