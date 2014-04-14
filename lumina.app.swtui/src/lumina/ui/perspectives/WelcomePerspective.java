package lumina.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.internal.intro.IIntroConstants;

/**
 * A perspective only with the intro view. This perspective is aims the welcome
 * page.
 */
@SuppressWarnings("restriction")
public class WelcomePerspective implements IPerspectiveFactory {
	/**
	 * Perspective identifier.
	 */
	public static final String PERSPECTIVE_ID = "lumina.perspectives.WelcomePerspective"; //$NON-NLS-1$

	/**
	 * Creates initial layout for operator mode.
	 * 
	 * @param layout
	 *            page layout
	 */
	public void createInitialLayout(IPageLayout layout) {

		/*
		 * Notes: - The intro view id is not public so this may break in future
		 * versions of eclipse. - The layout cannot be fixed because for some
		 * reason (that must make sense in the mind of some eclipse developer)
		 * the rendering of content is only triggered by maximizing or
		 * unmaximizing the view. Fixed view => maximize doesn't work => intro
		 * never renders.
		 */

		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		// Read the comment above before uncommenting:
		// layout.setFixed(true);

		// Place only the intro view
		IFolderLayout folder = layout.createFolder("intro", IPageLayout.RIGHT,
				IPageLayout.RATIO_MAX, editorArea);
		folder.addPlaceholder(IIntroConstants.INTRO_VIEW_ID + ":*");
		folder.addView(IIntroConstants.INTRO_VIEW_ID);
		layout.getViewLayout(IIntroConstants.INTRO_VIEW_ID).setCloseable(false);
		layout.getViewLayout(IIntroConstants.INTRO_VIEW_ID).setMoveable(false);
	}

}
