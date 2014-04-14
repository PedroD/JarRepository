package lumina.ui.perspectives;

import lumina.ui.views.blueprint.BlueprintView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * A perspective with only the BlueprintView.<br/>
 * By switching to this perspective we fake maximizing the BlueprintView.
 * 
 * @see lumina.ui.views.blueprint.BlueprintView
 */
public class MaxPlanPerspective implements IPerspectiveFactory {

	/**
	 * Perspective identifier.
	 */
	public static final String PERSPECTIVE_ID = "lumina.perspectives.MaxPlanPerspective"; //$NON-NLS-1$

	/**
	 * Creates initial layout for blueprint perspective.
	 * 
	 * @param layout
	 *            page layout
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		// Place only the BlueprintView
		IFolderLayout folder = layout.createFolder("floorplans",
				IPageLayout.RIGHT, IPageLayout.RATIO_MAX, editorArea);
		folder.addPlaceholder(BlueprintView.ID + ":*");
		folder.addView(BlueprintView.ID);
		layout.getViewLayout(BlueprintView.ID).setCloseable(false);
		layout.getViewLayout(BlueprintView.ID).setMoveable(false);
	}

}
