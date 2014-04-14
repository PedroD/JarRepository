package lumina;

import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.license.VersionService;
import lumina.ui.perspectives.PerspectiveHelper;
import lumina.ui.swt.ApplicationImageCache;
import lumina.ui.views.control.ControlView;
import lumina.ui.views.control.panels.UIConstants;
import lumina.ui.views.navigation.NavigationView;
import lumina.ui.views.properties.PropertiesView;
import lumina.ui.views.timer.TimerView;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Configures the workbench window (i.e. main application window) and overrides
 * some window life-cycle events.
 * <p>
 * Installs the tray icon.
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final int MIN_WORKBENCH_W = 300;
	private static final int MIN_WORKBENCH_H = 300;

	/**
	 * Format of the file path in the title bar.
	 */
	private static final String TITLE_FILE_PATH_FORMAT = "%s [%s]"; // $NON-NLS$

	private IWorkbenchWindowConfigurer configurer;

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		// To remove the application bar
		// configurer.setShellStyle(SWT.NONE);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	/**
	 * Change listener that handles changes in the application title.
	 * <p>
	 * It also updates the title with the information about whether the project
	 * is saved or not.
	 */
	private final IPropertyChangeListener saveStatusListener = new IPropertyChangeListener() {
		/**
		 * Reacts to changes in the application name.
		 */
		public void propertyChange(final PropertyChangeEvent event) {
			final String changedProperty = event.getProperty();

			if (PropertyChangeNames.PROJECT_SAVE_STATUS_CHANGED
					.equals(changedProperty)
					|| PropertyChangeNames.PROJECT_LOADED
							.equals(changedProperty)) {
				if (configurer != null) {
					final Project project = ProjectModel.getInstance()
							.getProject();

					final String projectFileName;
					final String fullFilePath;

					if (project != null
							&& project != Project.UNASSIGNED_PROJECT) {
						projectFileName = project.getFileName();
						fullFilePath = ProjectModel.getInstance().getProject()
								.getProjectFilePath();
					} else {
						projectFileName = "";
						fullFilePath = null;
					}

					final String fileNameWithDetails;
					if (fullFilePath != null) {
						fileNameWithDetails = String.format(
								TITLE_FILE_PATH_FORMAT, projectFileName,
								fullFilePath);
					} else {
						fileNameWithDetails = projectFileName;
					}

					configurer
							.setTitle(VersionService.getInstance()
									.getMainWindowTitle(
											fileNameWithDetails,
											ProjectModel.getInstance()
													.isProjectDirty()));
				}
			}
		}
	};

	@Override
	public void preWindowOpen() {
		configurer = getWindowConfigurer();

		configurer.setShowCoolBar(true);
		configurer.setShowMenuBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowProgressIndicator(true);

		// allows switching perspectives, for debugging:
		// configurer.setShowPerspectiveBar(true);

		ProjectModel.getInstance()
				.addPropertyChangeListener(saveStatusListener);

		final ISelectionService selectionService = getWindowConfigurer()
				.getWindow().getSelectionService();
		ProjectModel.getInstance().setSelectionService(selectionService);
	}

	/**
	 * Installs a tray icon and performs minimize to tray when close button is
	 * clicked.
	 */
	private void installTrayIcon() {
		final Display display = Display.getCurrent();

		final TrayItem trayItem = new TrayItem(display.getSystemTray(),
				SWT.NONE);
		final Image image = ApplicationImageCache.getInstance().getImage(
				Constants.APPLICATION_ICON_PATH);

		if (image != null && !image.isDisposed()) {
			trayItem.setImage(image);
		}

		trayItem.setToolTipText(VersionService.getFormattedApplicationName());

		/*
		 * Restore workbench window when tray icon is double clicked.
		 */
		trayItem.addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				final Shell workbenchWindowShell = getWindowConfigurer()
						.getWindow().getShell();
				workbenchWindowShell.setVisible(true);
				workbenchWindowShell.setActive();
				workbenchWindowShell.setFocus();
				workbenchWindowShell.setMinimized(false);
			}
		});

		/*
		 * Show exit/open menu when tray icon is right-clicked
		 */
		Shell workbenchWindowShell = getWindowConfigurer().getWindow()
				.getShell();

		/*
		 * Create a Menu with the open and exit menu items.
		 */
		final Menu menu = new Menu(workbenchWindowShell, SWT.POP_UP);

		final MenuItem open = new MenuItem(menu, SWT.PUSH);
		open.setText(Messages.getString("Application.Tray.open",
				Constants.PRODUCT_NAME)); // $NON-NLS$
		open.setImage(lumina.ui.swt.ApplicationImageCache.getInstance()
				.getImage(UIConstants.APPLICATION_ICON_IMAGE_PATH));

		final MenuItem separator = new MenuItem(menu, SWT.SEPARATOR);

		final MenuItem exit = new MenuItem(menu, SWT.PUSH);
		exit.setText(Messages.getString("Application.Tray.exit")); // $NON-NLS$
		exit.setImage(lumina.ui.swt.ApplicationImageCache.getInstance()
				.getImage("/icons/actions/exit.png"));

		exit.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (getWindowConfigurer().getWorkbenchConfigurer()
						.getWorkbench().close()) {
					// Dispose only if user has confirmed application exit
					image.dispose();
					trayItem.dispose();
					open.dispose();
					separator.dispose();
					exit.dispose();
					menu.dispose();
				}
			}
		});

		// Make the workbench visible in the event handler for open menu item.
		open.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final Shell workbenchWindowShell = getWindowConfigurer()
						.getWindow().getShell();
				workbenchWindowShell.setVisible(true);
				workbenchWindowShell.setActive();
				workbenchWindowShell.setFocus();
				workbenchWindowShell.setMinimized(false);
			}
		});

		// Add the menu listener to the tray icon
		trayItem.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				menu.setVisible(true);
			}
		});
	}

	@Override
	public boolean preWindowShellClose() {
		getWindowConfigurer().getWindow().getShell().setVisible(false);
		return false;
	}

	@Override
	public void postWindowOpen() {
		IWorkbenchPage wbPage = configurer.getWindow().getActivePage();

		/*
		 * Restrict main window size.
		 */
		configurer.getWindow().getShell()
				.setMinimumSize(MIN_WORKBENCH_W, MIN_WORKBENCH_H);

		/*
		 * Switch to the desired initial perspective.
		 */
		final String currPerspId = wbPage.getPerspective().getId();
		final String desiredPerspId = PerspectiveHelper
				.getDefaultPerspectiveId();
		if (!currPerspId.equals(desiredPerspId)) {
			wbPage.setPerspective(PlatformUI.getWorkbench()
					.getPerspectiveRegistry()
					.findPerspectiveWithId(desiredPerspId));
		}

		/*
		 * Add views as selection listeners. This guarantees that all views are
		 * initialized.
		 */
		final IViewPart navigationView = wbPage.findView(NavigationView.ID);
		if (navigationView instanceof NavigationView) {
			wbPage.addSelectionListener((NavigationView) navigationView);
		}

		final IViewPart timerView = wbPage.findView(TimerView.ID);
		if (timerView instanceof TimerView) {
			wbPage.addSelectionListener((TimerView) timerView);
		}

		final IViewPart controlView = wbPage.findView(ControlView.ID);
		if (controlView instanceof ControlView) {
			wbPage.addSelectionListener((ControlView) controlView);
		}

		final IViewPart propertyView = wbPage.findView(PropertiesView.ID);
		if (propertyView instanceof PropertiesView) {
			wbPage.addSelectionListener((PropertiesView) propertyView);
		}

		/*
		 * Install the tray icon from the start of the application.
		 */
		installTrayIcon();
	}
}
