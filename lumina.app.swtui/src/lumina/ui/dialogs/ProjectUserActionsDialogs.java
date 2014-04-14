package lumina.ui.dialogs;

import lumina.base.model.ProjectModel;
import lumina.ui.actions.SaveProjectActionDelegate;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Project action that user interaction through dialogs.
 * 
 * @author Fernando Martins
 */
public final class ProjectUserActionsDialogs {

	/**
	 * Prevent the instantiation of this utility class.
	 */
	private ProjectUserActionsDialogs() {
	}

	/**
	 * Save dialog types.
	 */
	public static enum SaveReason {
		/** Application is about to close. */
		APPLICATION_EXIT,
		/** Another project is about to be opened. */
		OPEN_PROJECT,
		/** A new project is about to be created. */
		NEW_PROJECT
	}

	/**
	 * Results for save.
	 */
	public static enum SaveResult {
		/** Project saved. */
		PROJECT_SAVED_OK,
		/** Project not saved due to error. */
		PROJECT_SAVED_ERROR,
		/** Project not saved due to user cancellation. */
		USER_CANCEL
	}

	/**
	 * Checks if it is necessary to save before exiting.
	 * 
	 * @param reason
	 *            reason for save
	 * @return true if project has been saved, false otherwise
	 */
	public static SaveResult saveBefore(final SaveReason reason) {
		boolean saveBefore = false;

		// ask the user to save if the project has unsaved changes
		if (ProjectModel.getInstance().isProjectDirty()) {
			String reasonMsg;
			String titleMsg;
			if (reason == SaveReason.APPLICATION_EXIT) {
				titleMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveProjectExitTitle");
				reasonMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveProjectExitMessage");
			} else if (reason == SaveReason.NEW_PROJECT) {
				titleMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveNewProjectTitle");
				reasonMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveNewProjectMessage");
			} else if (reason == SaveReason.OPEN_PROJECT) {
				titleMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveOpenProjectTitle");
				reasonMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveOpenProjectMessage");
			} else {
				titleMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveProjectTitle");
				reasonMsg = Messages
						.getString("ApplicationWorkbenchAdvisor.saveProjectMessage");
			}

			MessageDialog msgDialog = new MessageDialog(Display.getCurrent()
					.getActiveShell(), titleMsg, null, reasonMsg,
					MessageDialog.QUESTION, new String[] {
							IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL,
							IDialogConstants.CANCEL_LABEL }, 2);
			msgDialog.setBlockOnOpen(true);
			int result = msgDialog.open();

			if (result == 2) {
				// cancel the exit
				return SaveResult.USER_CANCEL;
			}
			saveBefore = result == 0;
		}

		if (saveBefore) {
			SaveProjectActionDelegate saveAction = new SaveProjectActionDelegate();
			saveAction.init(null);
			saveAction.run(null);

			if (!saveAction.getActionSuccess()) {
				// Save failed
				return SaveResult.PROJECT_SAVED_ERROR;
			}
		}
		return SaveResult.PROJECT_SAVED_OK;
	}

	/**
	 * Informs the user about an unclean shutdown.
	 */
	public static void informUncleanShutdown() {
		final String titleMsg = Messages
				.getString("Shutdown.uncleanShutdownTitle");
		final String message = Messages
				.getString("Shutdown.uncleanShutdownMessage");

		MessageDialog msgDialog = new MessageDialog(Display.getCurrent()
				.getActiveShell(), titleMsg, null, message,
				MessageDialog.WARNING,
				new String[] { IDialogConstants.OK_LABEL }, 2);
		msgDialog.setBlockOnOpen(true);
		msgDialog.open();
	}

}
