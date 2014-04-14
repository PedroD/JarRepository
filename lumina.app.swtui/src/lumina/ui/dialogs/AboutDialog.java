package lumina.ui.dialogs;

import lumina.Activator;
import lumina.license.VersionService;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.swtdesigner.ResourceManager;

/**
 * About Box.
 * 
 * @author Fernando Martins
 */
public class AboutDialog extends Dialog {

	/**
	 * Text to build the about dialog title.
	 */
	private static final String ABOUT_TEXT = Messages.getString(
			"AboutDialog.title", // $NON-NLS-1$
			VersionService.getFormattedApplicationName());

	/**
	 * Text to display the 'Licensed to:' message.
	 */
	private static final String LICENSED_TO_TEXT = Messages
			.getString("AboutDialog.licensedTo"); // $NON-NLS-1$;

	/**
	 * Path to the file that contains the logo.
	 */
	private static final String LOGO_PATH = "nl/en/splash.bmp";

	/**
	 * Width of the image logo to be displayed.
	 */
	// private static final int LOGO_IMAGE_WIDTH = 454;

	/**
	 * Height of the image logo to be displayed.
	 */
	// private static final int LOGO_IMAGE_HEIGHT = 160;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 *            parent shell
	 */
	public AboutDialog(Shell parentShell) {
		super(parentShell);
		super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL);

	}

	/**
	 * Creates the about box dialog area.
	 * 
	 * @param parent
	 *            composite
	 * @return controls
	 */
	// CHECKSTYLE:OFF
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 10;
		gridLayout.verticalSpacing = 4;
		container.setLayout(gridLayout);
		Canvas logo = new Canvas(container, SWT.NONE);
		final GridData gd_logo = new GridData(SWT.CENTER, SWT.FILL, true, true);
		gd_logo.heightHint = 188;
		gd_logo.widthHint = 454;
		logo.setLayoutData(gd_logo);
		logo.setRedraw(true);
		logo.setBackgroundImage(ResourceManager.getPluginImage(
				Activator.getDefault(), LOGO_PATH));
		final Label hline = new Label(container, SWT.HORIZONTAL | SWT.SEPARATOR);
		final GridData gd_hline = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd_hline.widthHint = 250;
		hline.setLayoutData(gd_hline);

		final Label versionLabel = new Label(container, SWT.NONE);
		final GridData gd_versionLabel = new GridData();
		gd_versionLabel.horizontalIndent = 10;
		versionLabel.setLayoutData(gd_versionLabel);
		versionLabel.setText(VersionService.getInstance().getVersionNotice());

		final Label copyrightLabel = new Label(container, SWT.NONE);
		final GridData gd_copyrightLabel = new GridData();
		gd_copyrightLabel.horizontalIndent = 10;
		copyrightLabel.setLayoutData(gd_copyrightLabel);
		copyrightLabel.setText(VersionService.getCopyrightNotice());

		final Label licensedToLabel = new Label(container, SWT.NONE);
		final GridData gd_licensedToLabel = new GridData();
		gd_licensedToLabel.horizontalIndent = 10;
		gd_licensedToLabel.verticalIndent = 10;
		licensedToLabel.setLayoutData(gd_licensedToLabel);
		licensedToLabel.setText(LICENSED_TO_TEXT);

		final Label licenseOwnerLabel = new Label(container, SWT.WRAP);
		licenseOwnerLabel.setDragDetect(false);
		// licenseOwnerLabel.setFont(SWTResourceManager.getFont("Arial", 7,
		// SWT.NONE));
		final GridData gd_componentNoticeLabel = new GridData(SWT.FILL,
				SWT.CENTER, true, false);
		gd_componentNoticeLabel.widthHint = 437;
		gd_componentNoticeLabel.horizontalIndent = 20;
		gd_componentNoticeLabel.verticalIndent = -1;
		licenseOwnerLabel.setLayoutData(gd_componentNoticeLabel);
		licenseOwnerLabel.setText(VersionService.getInstance()
				.getLicenseOwnerNotice());

		parent.pack();
		return container;
	}

	// CHECKSTYLE:ON

	/**
	 * Configures the window shell.
	 * 
	 * @param newShell
	 *            new shell
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ABOUT_TEXT);
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 *            parent object
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
	}
}
