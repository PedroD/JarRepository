package lumina.ui.perspectives;

import lumina.license.License;
import lumina.license.UserMode;

import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;

/**
 * This class contains auxiliary methods to help deal with the various
 * perspectives.
 * <p>
 * This is based on the marketing functionalities version and operations mode.
 */
public final class PerspectiveHelper {

	/**
	 * Prevent instantiation.
	 */
	private PerspectiveHelper() {
	}

	/**
	 * Determines the appropriate perspective to use, taking into account the
	 * licensed version and the current user mode.
	 * <p>
	 * NOTE: This method depends on the {@link License} and {@link UserMode}
	 * classes having been initialized.
	 * 
	 * @return The perspective id
	 */
	public static String getDefaultPerspectiveId() {
		switch (License.getInstance().getLicenseType()) {
		case DEVELOPER:
			return AdminPerspective.PERSPECTIVE_ID;
		case STANDARD:
			switch (UserMode.getMode()) {
			case ADMIN:
				return AdminPerspective.PERSPECTIVE_ID;
			case OPERATOR:
				return OperatorPerspective.PERSPECTIVE_ID;
			default:
				return null;
			}
		case EXPRESS:
			return ExpressPerspective.PERSPECTIVE_ID;
		default:
			return null;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// Helper methods for the MaxPlanPerspective
	// /////////////////////////////////////////////////////////////////////////////////

	/** Perspective to restore when the plan is un-maximized . */
	private static String restorePerspectiveId = null;

	/**
	 * Checks whether the floor plan view is maximized.
	 * 
	 * @param wbPage
	 *            current workbench page
	 * @return true if the plan is maximized
	 */
	public static boolean isPlanMaximized(final IWorkbenchPage wbPage) {
		final String currPerspId = wbPage.getPerspective().getId();
		return currPerspId.equals(MaxPlanPerspective.PERSPECTIVE_ID);
	}

	/**
	 * Toggles the floor plan view maximization.
	 * 
	 * @param wbPage
	 *            current workbench page
	 * @return true if the plan is now maximized
	 */
	public static boolean togglePlanMaximized(final IWorkbenchPage wbPage) {
		final IPerspectiveRegistry pr = PlatformUI.getWorkbench()
				.getPerspectiveRegistry();

		if (isPlanMaximized(wbPage)) {
			// we're restoring
			wbPage.setPerspective(pr
					.findPerspectiveWithId(restorePerspectiveId));
			restorePerspectiveId = null;
			return false;
		} else {
			// we're maximizing
			restorePerspectiveId = wbPage.getPerspective().getId();
			wbPage.setPerspective(pr
					.findPerspectiveWithId(MaxPlanPerspective.PERSPECTIVE_ID));
			return true;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	// Helper methods for the WelcomePerspective
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Checks whether the welcome page is open.
	 * 
	 * @param wbPage
	 *            current workbench page
	 * @return true if the welcome page is open
	 */
	public static boolean isWelcomePageOpen(final IWorkbenchPage wbPage) {
		final String currPerspId = wbPage.getPerspective().getId();
		return currPerspId.equals(WelcomePerspective.PERSPECTIVE_ID);
	}

	/**
	 * Shows the welcome page.
	 */
	public static void openWelcomePage() {
		final IWorkbench wb = PlatformUI.getWorkbench();
		if (wb == null)
			return;

		// switch to the welcome perspective
		final IWorkbenchWindow wbWindow = wb.getActiveWorkbenchWindow();
		if (wbWindow == null)
			return;

		final IWorkbenchPage wbPage = wbWindow.getActivePage();
		if (wbPage == null)
			return;

		final IPerspectiveRegistry pr = wb.getPerspectiveRegistry();
		if (pr == null)
			return;
		wbPage.setPerspective(pr
				.findPerspectiveWithId(WelcomePerspective.PERSPECTIVE_ID));

		// open the intro
		final IIntroManager im = wb.getIntroManager();
		if (im == null)
			return;
		im.showIntro(wb.getActiveWorkbenchWindow(), false);
	}

	/**
	 * Closes the welcome page.
	 */
	public static void closeWelcomePage() {
		final IWorkbench wb = PlatformUI.getWorkbench();
		if (wb == null)
			return;
		final IWorkbenchWindow wbWindow = wb.getActiveWorkbenchWindow();

		if (wbWindow == null)
			return;

		// close the intro
		final IIntroManager im = wb.getIntroManager();
		im.closeIntro(im.getIntro());

		final IWorkbenchPage wbPage = wbWindow.getActivePage();

		if (isWelcomePageOpen(wbPage)) {
			// switch back to default perspective
			final String id = PerspectiveHelper.getDefaultPerspectiveId();

			final IPerspectiveRegistry pr = wb.getPerspectiveRegistry();
			wbPage.setPerspective(pr.findPerspectiveWithId(id));
		}
	}
}
