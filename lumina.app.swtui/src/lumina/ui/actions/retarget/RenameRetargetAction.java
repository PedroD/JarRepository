package lumina.ui.actions.retarget;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.LabelRetargetAction;

/**
 * Rename in blueprint command.
 */
public class RenameRetargetAction extends LabelRetargetAction {

	/**
	 * Blueprint rename command action identifier.
	 */
	public static final String ID = "lumina.ui.actions.properties.rename";

	/**
	 * Blueprint rename command identifier.
	 */
	public static final String DEFINITION_ID = "lumina.commands.properties.rename";

	/**
	 * Adds itself to the part listener to handle rename in the blueprint
	 * command.
	 * 
	 * @param window
	 *            workbench window
	 */
	public RenameRetargetAction(IWorkbenchWindow window) {
		super(ID, "Re&name");

		setActionDefinitionId(DEFINITION_ID);
		window.getPartService().addPartListener(this);
	}
}
