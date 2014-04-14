package lumina.ui.actions;

import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.dialogs.ProjectUserActionsDialogs;
import lumina.ui.jface.SelectionUtils;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for project creation.
 */
public class CreateProjectHandler extends AbstractHandler {

	/**
	 * Creates a project.
	 * 
	 * @param execEvent
	 *            event
	 * @return null
	 * @throws ExecutionException
	 *             not thrown
	 * @see org.eclipse.core.commands.AbstractHandler#execute(ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent execEvent) throws ExecutionException {
		if (!Capabilities.canDo(Capability.PROJECT_NEW)) {
			return null;
		}

		if (Capabilities.canDo(Capability.PROJECT_SAVE)) {
			ProjectUserActionsDialogs.SaveResult result = ProjectUserActionsDialogs
					.saveBefore(ProjectUserActionsDialogs.SaveReason.NEW_PROJECT);
			if (result != ProjectUserActionsDialogs.SaveResult.PROJECT_SAVED_OK) {
				return null;
			}
		}

		final Project project = ProjectModel.getInstance().newProject();
		final ISelectionProvider selectionProvider = HandlerUtil
				.getActivePart(execEvent).getSite().getSelectionProvider();
		SelectionUtils.doSelectItems(new Object[] { project },
				selectionProvider);

		return null;
	}
}
