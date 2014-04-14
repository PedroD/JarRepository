package lumina.ui.actions.retarget;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.LabelRetargetAction;

/**
 * Select all in blueprint command.
 */
public class SelectAllRetargetAction extends LabelRetargetAction {

	/**
	 * Blueprint select all command action identifier.
	 */
	public static final String ID = "lumina.ui.actions.edit.selectAll";
	/**
	 * Select all center command identifier.
	 */
	public static final String DEFINITION_ID = "lumina.commands.edit.selectAll";

	/**
	 * Adds itself to the part listener to handle select all in the blueprint
	 * command.
	 * 
	 * @param window
	 *            workbench window
	 */
	public SelectAllRetargetAction(IWorkbenchWindow window) {
		super(ID, "Select All");

		setActionDefinitionId(DEFINITION_ID);
		window.getPartService().addPartListener(this);
	}
}
