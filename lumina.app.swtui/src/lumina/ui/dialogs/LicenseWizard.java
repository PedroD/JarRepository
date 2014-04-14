package lumina.ui.dialogs;

import java.io.File;

import lumina.license.License;
import lumina.license.LicenseValidationException;
import lumina.license.VersionService;
import lumina.ui.jface.EnhancedFileDialog;
import lumina.ui.swt.ApplicationImageCache;
import lumina.ui.swt.SimpleDialogs;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard that guides the user through the process of obtaining and installing a
 * license file.
 */
public class LicenseWizard extends Wizard {

	/** Email shown for general support. */
	private static final String CUSTOMER_SUPPORT_EMAIL = "support@sislite.pt"; //$NON-NLS-1$

	/** Email shown for license file requests. */
	private static final String LICENSE_REQ_EMAIL = "support@sislite.pt"; //$NON-NLS-1$

	/** URL for retrieving the license file. */
	private static final String LICENSE_REQ_URL = "www.sislite.pt/icangraph/license/key.php"; //$NON-NLS-1$

	/**
	 * Wizard page 1.
	 */
	private class Page1 extends WizardPage {

		private Button rbRequestLic;
		private Label labInstallDescr;
		private Button rbInstallLic;
		private Label labRequestDescr;

		public Page1() {
			super("page1", Messages.getString("LicenseWizard.page1.title",
					VersionService.getApplicationName()), null);
			setMessage(Messages.getString("LicenseWizard.page1.subtitle"));
		}

		// CHECKSTYLE:OFF - Generated code for the UI
		public void createControl(Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			container.setLayout(new GridLayout());

			rbRequestLic = new Button(container, SWT.RADIO | SWT.LEFT);
			GridData rbRequestLicLData = new GridData();
			rbRequestLic.setLayoutData(rbRequestLicLData);
			rbRequestLic.setText(Messages
					.getString("LicenseWizard.page1.rbRequestLic"));
			rbRequestLic.setSelection(true);

			labRequestDescr = new Label(container, SWT.NONE);
			GridData labRequestDescrLData = new GridData();
			labRequestDescrLData.horizontalIndent = 20;
			labRequestDescr.setLayoutData(labRequestDescrLData);
			labRequestDescr.setText(Messages
					.getString("LicenseWizard.page1.labRequestDescr"));
			labRequestDescr.setForeground(helpTextColor);

			rbInstallLic = new Button(container, SWT.RADIO | SWT.LEFT);
			GridData button1LData = new GridData();
			rbInstallLic.setLayoutData(button1LData);
			rbInstallLic.setText(Messages
					.getString("LicenseWizard.page1.rbInstallLic"));

			labInstallDescr = new Label(container, SWT.NONE);
			GridData labInstallDescrLData = new GridData();
			labInstallDescrLData.horizontalIndent = 20;
			labInstallDescr.setLayoutData(labInstallDescrLData);
			labInstallDescr.setText(Messages
					.getString("LicenseWizard.page1.labInstallDescr"));
			labInstallDescr.setForeground(helpTextColor);

			this.setControl(container);
		}

		// CHECKSTYLE:ON

		@Override
		public boolean isPageComplete() {
			return rbRequestLic.getSelection() || rbInstallLic.getSelection();
		}
	}

	/**
	 * Wizard page 2.
	 */
	private class Page2 extends WizardPage {

		private Label labLicenseNo;
		private Label labLicenseNoHelp1;
		private Link labLicenseNoHelp2;
		private Text tLicenseNo;

		public Page2() {
			super("page2", Messages.getString("LicenseWizard.page2.title"),
					null);
			setMessage(Messages.getString("LicenseWizard.page2.subtitle"));
		}

		// CHECKSTYLE:OFF - Generated code for the UI
		public void createControl(Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			container.setLayout(layout);

			{
				labLicenseNo = new Label(container, SWT.NONE);
				labLicenseNo.setText(Messages
						.getString("LicenseWizard.page2.labLicenseNo"));
			}
			{
				tLicenseNo = new Text(container, SWT.BORDER);
				tLicenseNo.setTextLimit(10);
				tLicenseNo.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent evt) {
						tCustNoModifyText(evt);
					}
				});
				GridData tLicenseNoData = new GridData();
				tLicenseNoData.widthHint = 90;
				tLicenseNo.setLayoutData(tLicenseNoData);
			}
			{
				labLicenseNoHelp1 = new Label(container, SWT.NONE);
				GridData labCustNoDescrLData = new GridData();
				labCustNoDescrLData.horizontalSpan = 2;
				labLicenseNoHelp1.setLayoutData(labCustNoDescrLData);
				labLicenseNoHelp1.setText(Messages.getString(
						"LicenseWizard.page2.labLicenseNoHelp1",
						VersionService.getApplicationName()));
				labLicenseNoHelp1.setForeground(helpTextColor);
			}
			{
				labLicenseNoHelp2 = new Link(container, SWT.NONE);
				GridData labCustNoDescrLData = new GridData();
				labCustNoDescrLData.horizontalSpan = 2;
				labLicenseNoHelp2.setLayoutData(labCustNoDescrLData);
				labLicenseNoHelp2.setText(Messages.getString(
						"LicenseWizard.page2.labLicenseNoHelp2",
						CUSTOMER_SUPPORT_EMAIL));
				labLicenseNoHelp2.setForeground(helpTextColor);
				labLicenseNoHelp2.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						labCustNoHelp2WidgetSelected(evt);
					}
				});
			}

			this.setControl(container);
		}

		// CHECKSTYLE:ON

		@Override
		public boolean isPageComplete() {
			return validateCustNo();
		}

		private void labCustNoHelp2WidgetSelected(SelectionEvent evt) {
			Program.launch(evt.text);
		}

		private void tCustNoModifyText(ModifyEvent evt) {
			if (validateCustNo()) {
				licenseNumber = tLicenseNo.getText().trim();
				setErrorMessage(null);
			} else {
				setErrorMessage("Please enter a valid customer number");
			}

			getWizard().getContainer().updateButtons();
			getWizard().getContainer().updateMessage();
		}

		private boolean validateCustNo() {
			// TODO: will license number have a specific format?
			return tLicenseNo.getText().trim().length() > 0;
		}
	}

	/**
	 * Wizard page 3.
	 */
	private class Page3 extends WizardPage {

		private Text tReqText;
		private Link links;
		private Label labHelpText;

		public Page3() {
			super("page3", Messages.getString("LicenseWizard.page3.title"),
					null);
			setMessage(Messages.getString("LicenseWizard.page3.subtitle"));
		}

		public void createControl(Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			container.setLayout(layout);

			tReqText = new Text(container, SWT.MULTI | SWT.READ_ONLY
					| SWT.BORDER | SWT.H_SCROLL);
			GridData tReqTextLData = new GridData();
			tReqTextLData.grabExcessHorizontalSpace = true;
			tReqTextLData.grabExcessVerticalSpace = true;
			tReqTextLData.horizontalAlignment = GridData.FILL;
			tReqTextLData.verticalAlignment = GridData.FILL;
			tReqText.setLayoutData(tReqTextLData);

			links = new Link(container, SWT.NONE);
			links.setText(Messages.getString("LicenseWizard.page3.links",
					LICENSE_REQ_URL, LICENSE_REQ_EMAIL));
			links.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					linksWidgetSelected(evt);
				}
			});

			labHelpText = new Label(container, SWT.NONE);
			labHelpText.setText("\n"
					+ Messages.getString("LicenseWizard.page3.labHelpText"));
			labHelpText.setForeground(helpTextColor);
			this.setControl(container);
		}

		@Override
		public boolean isPageComplete() {
			return true;
		}

		@Override
		public void setVisible(boolean visible) {
			// generate the license request upon entering the page
			if (visible) {
				String text = generateLicenseRequest();
				tReqText.setText(text);
				tReqText.setSelection(0, text.length());
			}

			super.setVisible(visible);
		}

		private void linksWidgetSelected(SelectionEvent evt) {
			Program.launch(evt.text);
		}
	}

	/**
	 * Wizard page 4.
	 */
	private class Page4 extends WizardPage {

		private Label labLicFile;
		private Text tLicFile;
		private Button bLicFile;

		public Page4() {
			super("page4", Messages.getString("LicenseWizard.page4.title"),
					null);
			setMessage(Messages.getString("LicenseWizard.page4.subtitle"));
		}

		public void createControl(Composite parent) {
			Composite container = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			container.setLayout(layout);

			GridData gridData = new GridData();
			gridData.horizontalSpan = 2;
			labLicFile = new Label(container, SWT.NONE);
			labLicFile.setText(Messages
					.getString("LicenseWizard.page4.labLicFile"));
			labLicFile.setLayoutData(gridData);
			GridData tLicFileLData = new GridData();
			tLicFileLData.horizontalAlignment = GridData.FILL;
			tLicFileLData.grabExcessHorizontalSpace = true;
			tLicFile = new Text(container, SWT.BORDER);
			tLicFile.setLayoutData(tLicFileLData);
			tLicFile.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent evt) {
					tLicFileModifyText(evt);
				}
			});
			bLicFile = new Button(container, SWT.PUSH | SWT.CENTER);
			bLicFile.setText(" "
					+ Messages.getString("LicenseWizard.page4.bLicFile") + " ");
			bLicFile.setImage(ApplicationImageCache.getInstance().getImage(
					"/icons/actions/open.png"));
			bLicFile.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					bLicFileWidgetSelected(evt);
				}
			});
			this.setControl(container);
		}

		@Override
		public boolean isPageComplete() {
			return validatePath();
		}

		private void bLicFileWidgetSelected(SelectionEvent evt) {
			EnhancedFileDialog openDialog = new EnhancedFileDialog(getShell(),
					SWT.OPEN);
			openDialog.setText("Select a license file");

			String name = openDialog.open();
			if (name != null) {
				tLicFile.setText(name);
			}
		}

		private void tLicFileModifyText(ModifyEvent evt) {
			if (validatePath()) {
				licenseFilePath = tLicFile.getText().trim();
				setErrorMessage(null);
			} else {
				setErrorMessage("File not found");
			}

			getWizard().getContainer().updateButtons();
			getWizard().getContainer().updateMessage();
		}

		/**
		 * Checks if the license file path is valid.
		 * 
		 * @return true if the license file path is valid, false otherwise
		 */
		private boolean validatePath() {
			return new File(tLicFile.getText().trim()).isFile();
		}
	}

	/**
	 * Wizard page 5.
	 */
	private class Page5 extends WizardPage {

		private Composite container;
		private Label labResultIcon;
		private Label labResultText;

		public Page5() {
			super("page5", Messages.getString("LicenseWizard.page5.title"),
					null);
			setMessage(Messages.getString("LicenseWizard.page5.subtitle"));
		}

		// CHECKSTYLE:OFF - Generated code for the UI
		public void createControl(Composite parent) {
			container = new Composite(parent, SWT.NONE);
			RowLayout layout = new RowLayout(SWT.HORIZONTAL);
			layout.spacing = 16;
			layout.marginHeight = 8;
			layout.marginWidth = 8;
			container.setLayout(layout);

			labResultIcon = new Label(container, SWT.NONE);
			labResultIcon.setText("icon");
			labResultText = new Label(container, SWT.NONE);
			labResultText.setText("result text ...");
			this.setControl(container);
		}

		// CHECKSTYLE:ON

		@Override
		public boolean isPageComplete() {
			return isLicenseFileValid;
		}

		@Override
		public void setVisible(boolean visible) {
			// validate the license file upon entering the page
			if (visible) {
				String icon;
				String message;

				try {
					License lic = new License(licenseFilePath);

					icon = "/icons/dialogs/license-ok.png";
					message = Messages.getString(
							"LicenseWizard.page5.messageOk", lic
									.getCustomerName(), lic.getLicenseId(), lic
									.getLicenseType().toString());

					isLicenseFileValid = true;
				} catch (LicenseValidationException ex) {
					icon = "/icons/dialogs/license-fail.png";
					message = Messages.getString(
							"LicenseWizard.page5.messageFail",
							ex.getLocalizedMessage());

					isLicenseFileValid = false;
				}

				labResultIcon.setImage(ApplicationImageCache.getInstance()
						.getImage(icon));
				labResultText.setText(message);

				container.layout();
				container.pack();
			}

			super.setVisible(visible);
		}
	}

	// -----------------------------------------------------------------------------------

	private Color helpTextColor;

	private Page1 page1;
	private Page2 page2;
	private Page3 page3;
	private Page4 page4;
	private Page5 page5;

	private String licenseNumber;
	private String licenseFilePath;
	private boolean isLicenseFileValid;

	private boolean isLicenseInstalled;

	private String generateLicenseRequest() {
		return License.generateLicenseRequest(licenseNumber);
	}

	/**
	 * Constructor.<br/>
	 * Creates the license wizard.
	 */
	public LicenseWizard() {
		super();
		this.setWindowTitle(Messages.getString("LicenseWizard.windowTitle"));

		page1 = new Page1();
		page2 = new Page2();
		page3 = new Page3();
		page4 = new Page4();
		page5 = new Page5();
	}

	/**
	 * Adds all necessary pages to the wizard.
	 */
	@Override
	public final void addPages() {
		helpTextColor = getShell().getDisplay().getSystemColor(
				SWT.COLOR_WIDGET_NORMAL_SHADOW);

		addPage(page1);
		addPage(page2);
		addPage(page3);
		addPage(page4);
		addPage(page5);
	}

	/**
	 * Returns the starting wizard page.
	 * 
	 * @return starting page
	 */
	@Override
	public final IWizardPage getStartingPage() {
		return getPages()[0];
	}

	/**
	 * Returns the next page.<br/>
	 * Returns the next page to display to the user depending on the current
	 * location plus user options selection.
	 * 
	 * @param page
	 *            current page
	 * @return next page
	 */
	public final IWizardPage getNextPage(IWizardPage page) {
		if (page == page1) {
			if (page1.rbRequestLic.getSelection()) {
				// go to license request step
				return page2;
			} else {
				// go to license install step
				return page4;
			}
		} else if (page == page2) {
			return page3;
		} else if (page == page3) {
			return page4;
		} else if (page == page4) {
			return page5;
		} else {
			return null;
		}
	}

	/**
	 * Checks if the wizard can finish.<br/>
	 * Wizard will be allowed to finish when all the rules have been validated.
	 * 
	 * @return true if wizard can finish, false otherwise
	 */
	@Override
	public final boolean canFinish() {
		IWizardPage currentPage = getContainer().getCurrentPage();
		if (currentPage instanceof Page3)
			return true;
		else
			return (currentPage instanceof Page5 && isLicenseFileValid);
	}

	/**
	 * Perform the finish action.
	 * 
	 * @return true if the wizard has properly finished, false otherwise
	 */
	@Override
	public final boolean performFinish() {
		if (!(getContainer().getCurrentPage() instanceof Page5)) {
			isLicenseInstalled = false;
			return true;
		}

		try {
			License.installLicenseFile(licenseFilePath);
			isLicenseInstalled = true;
			return true;
		} catch (Exception ex) {
			SimpleDialogs.showError(Messages
					.getString("LicenseWizard.installErrorTitle"), Messages
					.getString("LicenseWizard.installErrorText",
							License.getLicenseFilePath(),
							ex.getLocalizedMessage()), true);
			isLicenseInstalled = false;
			return false;
		}
	}

	/**
	 * After the wizard closes, call this method to determine whether a license
	 * file was successfully installed.
	 * 
	 * @return whether a license file was successfully installed
	 */
	public final boolean isLicenseInstalled() {
		return isLicenseInstalled;
	}
}
