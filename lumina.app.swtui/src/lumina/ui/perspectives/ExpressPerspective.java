package lumina.ui.perspectives;

import lumina.ui.views.blueprint.BlueprintView;
import lumina.ui.views.control.ControlView;
import lumina.ui.views.navigation.NavigationView;
import lumina.ui.views.timer.TimerView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This is the perspective used for the Express version.<br/>
 * This is based on the marketing functionalities version and operations mode.
 */
public class ExpressPerspective implements IPerspectiveFactory {

	/**
	 * Perspective identifier.
	 */
	public static final String PERSPECTIVE_ID = "lumina.perspectives.ExpressPerspective"; //$NON-NLS-1$

	/**
	 * Creates initial layout for express perspective.
	 * 
	 * @param layout
	 *            page layout
	 */
	// CHECKSTYLE:OFF
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		//
		// Place the views
		//

		// Top folder
		IFolderLayout folderNavigationTimer = layout.createFolder(
				"navigationTimer", IPageLayout.LEFT, 0.30f, editorArea);
		folderNavigationTimer.addPlaceholder(TimerView.ID + ":*");

		// Navigation View
		folderNavigationTimer.addView(NavigationView.ID);
		layout.getViewLayout(NavigationView.ID).setCloseable(false);
		layout.getViewLayout(NavigationView.ID).setMoveable(false);

		// Bottom folder
		IFolderLayout folderProperties = layout.createFolder("properties",
				IPageLayout.BOTTOM, 0.5f, NavigationView.ID);
		folderProperties.addPlaceholder(ControlView.ID + ":*");

		// Control View
		folderProperties.addView(ControlView.ID);
		layout.getViewLayout(ControlView.ID).setCloseable(false);
		layout.getViewLayout(ControlView.ID).setMoveable(false);

		// Right folder
		IFolderLayout folder = layout.createFolder("floorplans",
				IPageLayout.RIGHT, 0.7f, editorArea);
		folder.addPlaceholder(BlueprintView.ID + ":*");

		// BlueprintView
		folder.addView(BlueprintView.ID);
		layout.getViewLayout(BlueprintView.ID).setCloseable(false);
		layout.getViewLayout(BlueprintView.ID).setMoveable(false);
	}
	// CHECKSTYLE:ON
}
