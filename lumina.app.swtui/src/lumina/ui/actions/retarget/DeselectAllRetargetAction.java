package lumina.ui.actions.retarget;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.LabelRetargetAction;

/**
 * Deselect all in blueprint command.
 */
public class DeselectAllRetargetAction extends LabelRetargetAction {

	/**
	 * Blueprint deselect all command action identifier.
	 */
	public static final String ID = "lumina.ui.actions.edit.deselectAll";

	/**
	 * Blueprint deselect all command identifier.
	 */
	public static final String DEFINITION_ID = "lumina.commands.edit.deselectAll";

	/**
	 * Adds itself to the part listener to handle deselect all in the blueprint
	 * command.
	 * 
	 * @param window
	 *            workbench window
	 */
	public DeselectAllRetargetAction(IWorkbenchWindow window) {
		super(ID, "Deselect All" + "  ");

		setActionDefinitionId(DEFINITION_ID);
		window.getPartService().addPartListener(this);
	}
}
