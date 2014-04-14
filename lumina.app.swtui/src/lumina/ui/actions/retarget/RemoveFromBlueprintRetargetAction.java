package lumina.ui.actions.retarget;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.LabelRetargetAction;

/**
 * Remove from blueprint command.
 */
public class RemoveFromBlueprintRetargetAction extends LabelRetargetAction {

	/**
	 * Blueprint remove command action identifier.
	 */
	public static final String ID = "lumina.blueprint.actions.removeFromBlueprint";

	/**
	 * Blueprint remove command identifier.
	 */
	public static final String DEFINITION_ID = "lumina.blueprint.commands.removeFromBlueprint";

	/**
	 * Adds itself to the part listener to handle remove from the blueprint
	 * command.
	 * 
	 * @param window
	 *            workbench window
	 */
	public RemoveFromBlueprintRetargetAction(final IWorkbenchWindow window) {
		super(ID, "Remove from blueprint");

		setActionDefinitionId(DEFINITION_ID);
		window.getPartService().addPartListener(this);
	}
}
