package lumina;

import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.actions.retarget.RenameRetargetAction;
import lumina.ui.actions.retarget.UndoRetargetAction;
import lumina.ui.views.status.NetworkMonitorStatusLineContribution;
import lumina.ui.views.status.NetworkStatusMenuContributionFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.menus.IMenuService;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	/**
	 * URI of the menu where the network extensions will be placed.
	 */
	private static final String INTERFACE_ADDITION_URI = "menu:lumina.menus.network?after=connections";

	/**
	 * Reference to the menu contribution factory.
	 */
	private NetworkStatusMenuContributionFactory networkMenuContributionFactory;

	/**
	 * Holds the reference to the.
	 */
	private NetworkEventEvaluatorBridge networkReselectBridge;

	public ApplicationActionBarAdvisor(final IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.
		register(ActionFactory.QUIT.create(window));

		register(ActionFactory.UNDO.create(window));
		register(ActionFactory.REDO.create(window));

		register(ActionFactory.CUT.create(window));
		register(ActionFactory.COPY.create(window));
		register(ActionFactory.PASTE.create(window));
		register(ActionFactory.DELETE.create(window));
		register(new RenameRetargetAction(window));

		// testing here
		register(new UndoRetargetAction(window));

		// register(ActionFactory.HELP_CONTENTS.create(window));
		// register(ActionFactory.HELP_SEARCH.create(window));
		// register(ActionFactory.ABOUT.create(window));

		if (window.getWorkbench().getIntroManager().hasIntro()) {
			final IAction introAction = ActionFactory.INTRO.create(window);
			register(introAction);
		}
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		final IMenuService menuService = (IMenuService) PlatformUI
				.getWorkbench().getService(IMenuService.class);

		if (Capabilities.canDo(Capability.NETWORK_EDIT)) {
			// add the dynamic interface list to the Network menu
			networkMenuContributionFactory = new NetworkStatusMenuContributionFactory(
					INTERFACE_ADDITION_URI);

			menuService.addContributionFactory(networkMenuContributionFactory);
		}

		/*
		 * Assign the network reselect bridge
		 */
		networkReselectBridge = NetworkEventEvaluatorBridge.getInstance();
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
	}

	@Override
	protected void fillStatusLine(final IStatusLineManager statusLine) {
		this.getActionBarConfigurer().getWindowConfigurer().getWindow()
				.getSelectionService();

		statusLine.add(NetworkMonitorStatusLineContribution.getInstance()); // NON-NLS-1

		statusLine.update(true);
	}

	@Override
	public void dispose() {
		networkReselectBridge.dispose();

		final IMenuService menuService = (IMenuService) PlatformUI
				.getWorkbench().getService(IMenuService.class);

		menuService.removeContributionFactory(networkMenuContributionFactory);
		super.dispose();
	}

}
