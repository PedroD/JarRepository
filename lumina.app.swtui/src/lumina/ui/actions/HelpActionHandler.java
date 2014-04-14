package lumina.ui.actions;

import java.io.IOException;

import lumina.kernel.Logger;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.service.log.LogService;

import codebase.os.SysUtil;

/**
 * Online help invocation.
 */
public class HelpActionHandler implements IWorkbenchWindowActionDelegate {

	private static final String HELP_APPLICATION = "hh";

	private static final String HELP_DOCUMENT = "help.chm";

	private String runCommand = "";

	/**
	 * Constructor.
	 */
	public HelpActionHandler() {
		super();
	}

	/**
	 * Initializes the help action.
	 * 
	 * @param window
	 *            workbench window
	 */
	public void init(IWorkbenchWindow window) {
		this.runCommand = "";
	}

	/**
	 * Invoke i18n help command. Specific locale, language and country is
	 * ignored if present, only language will be used.
	 * 
	 * @param action
	 *            action
	 */
	public void run(IAction action) {
		if (this.runCommand.length() == 0) {
			String helpFilePath = SysUtil.makePath(
					SysUtil.getApplicationPath(), System.getProperty("osgi.nl")
							.substring(0, 2), HELP_DOCUMENT);

			this.runCommand = HELP_APPLICATION + " " + helpFilePath;
		}

		try {
			Runtime.getRuntime().exec(this.runCommand);
		} catch (IOException ex) {
			Logger.getInstance().log(
					LogService.LOG_ERROR,
					"Error on invoking help command '"
							+ this.runCommand.toString() + "'", ex);
		}
	}

	/**
	 * Terminates.
	 */
	public void dispose() {
	}

	/**
	 * Trigger for selection changed.
	 * 
	 * @param action
	 *            action
	 * @param selection
	 *            selection
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
