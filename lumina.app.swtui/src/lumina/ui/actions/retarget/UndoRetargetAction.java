package lumina.ui.actions.retarget;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.LabelRetargetAction;

/**
 * Undo in blueprint command.
 */
public class UndoRetargetAction extends LabelRetargetAction {

	/**
	 * Adds itself to the part listener to handle undo in the blueprint command.
	 * 
	 * @param window
	 *            workbench window
	 */
	public UndoRetargetAction(final IWorkbenchWindow window) {
		super(ActionFactory.UNDO.getId(), "undo action name");

		if (window == null)
			throw new IllegalArgumentException("window cannot be null"); //$NON-NLS-1$

		setToolTipText("Undo2 tooltip");
		setActionDefinitionId(ActionFactory.UNDO.getId());

		window.getPartService().addPartListener(this);

		ISharedImages sharedImages = window.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
	}

}
