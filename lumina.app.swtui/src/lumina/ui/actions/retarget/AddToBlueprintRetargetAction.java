package lumina.ui.actions.retarget;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.LabelRetargetAction;

/**
 * Add to blueprint command.
 */
public class AddToBlueprintRetargetAction extends LabelRetargetAction {

	/**
	 * Blueprint add command action identifier.
	 */
	public static final String ID = "lumina.blueprint.actions.addToBlueprint";

	/**
	 * Blueprint add command identifier.
	 */
	public static final String DEFINITION_ID = "lumina.blueprint.commands.addToBlueprint";

	/**
	 * Adds itself to the part listener to handle add to the blueprint command.
	 * 
	 * @param window
	 *            workbench window
	 */
	public AddToBlueprintRetargetAction(final IWorkbenchWindow window) {
		super(ID, "Add to blueprint");

		setActionDefinitionId(DEFINITION_ID);
		window.getPartService().addPartListener(this);
	}
}
