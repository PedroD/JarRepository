package lumina.ui.actions.retarget;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.LabelRetargetAction;

/**
 * Center in blueprint command.
 */
public class CenterInBlueprintRetargetAction extends LabelRetargetAction {

	/**
	 * Blueprint center command action identifier.
	 */
	public static final String ID = "lumina.blueprint.actions.centerInBlueprint";

	/**
	 * Blueprint center command identifier.
	 */
	public static final String DEFINITION_ID = "lumina.blueprint.commands.centerInBlueprint";

	/**
	 * Adds itself to the part listener to handle center in the blueprint
	 * command.
	 * 
	 * @param window
	 *            workbench window
	 */
	public CenterInBlueprintRetargetAction(IWorkbenchWindow window) {
		super(ID, "Center in blueprint");

		setActionDefinitionId(DEFINITION_ID);
		window.getPartService().addPartListener(this);
	}
}
