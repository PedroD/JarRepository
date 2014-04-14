package lumina.ui.dialogs;

import java.util.LinkedList;

import lumina.network.LuminaException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * LuminaErrorDialog is a dialog that can show multiple errors. The dialog can
 * be blocking or non-blocking. In non-blocking mode the addStatus() method can
 * be called to add new errors to a visible dialog. Initial code was based on
 * {link org.eclipse.jface.dialogs.ErrorDialog} and {link
 * org.eclipse.jface.util.SafeRunnableDialog}
 */
public class LuminaErrorDialog extends IconAndMessageDialog {

	/** Height in lines for the error list. */
	private static final int ERRORS_SIZE = 6;

	/** Height in lines for the error details. */
	private static final int DETAILS_SIZE = 8;

	/** The title of the dialog. */
	private String title = Messages.getString("LuminaErrorDialog.titleSingle"); //$NON-NLS-1$

	/** The currently selected status object. */
	private IStatus status;

	/** The list of status objects being displayed. */
	private LinkedList<IStatus> statuses = new LinkedList<IStatus>();

	/** The SWT control that displays the error details. */
	private Text details;

	/** Indicates whether the error details viewer is currently created. */
	private boolean detailsCreated = false;

	/** The list of errors, used in multiple-error mode. */
	private TableViewer statusListViewer;

	/**
	 * Create a new instance of the dialog on a status.
	 * 
	 * @param parentShell
	 *            The shell under which to create this dialog. May be null.
	 * @param status
	 *            The status to display.
	 * @param blocking
	 *            Whether the dialog should be blocking.
	 */
	public LuminaErrorDialog(Shell parentShell, IStatus status, boolean blocking) {
		super(parentShell);

		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MIN
				| getDefaultOrientation());

		setBlockOnOpen(blocking);

		this.message = getFullDescription(status, true);

		statuses.addFirst(status);
		setStatus(status);
	}

	/**
	 * Create a new instance of the dialog on a status.
	 * 
	 * @param parentShell
	 *            The shell under which to create this dialog. May be null.
	 * @param status
	 *            The status to display.
	 * @param blocking
	 *            Whether the dialog should be blocking.
	 * @param includeCause
	 *            Whether the cause should be included in the description
	 */
	public LuminaErrorDialog(Shell parentShell, IStatus status,
			boolean blocking, boolean includeCause) {
		super(parentShell);

		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MODELESS | SWT.MIN
				| getDefaultOrientation());

		setBlockOnOpen(blocking);

		this.message = getFullDescription(status, includeCause);

		statuses.addFirst(status);
		setStatus(status);
	}

	/**
	 * Add the status to the dialog.
	 * 
	 * @param status
	 *            the status
	 */
	public void addStatus(IStatus status) {
		addStatus(status, true);
	}

	/**
	 * Add the status to the dialog.
	 * 
	 * @param status
	 *            the status
	 * @param includeCause
	 *            whether the cause message should be included
	 */
	public void addStatus(IStatus status, boolean includeCause) {
		statuses.addFirst(status);
		setStatus(status, includeCause);

		createStatusList((Composite) dialogArea);
		getShell().setActive(); // bring dialog to front
	}

	/**
	 * Builds a description of an error from the status message and the
	 * exception messages.
	 * 
	 * @param status
	 *            The status to get the error description for
	 * @param includeCause
	 *            Whether the cause should be included in the description
	 * @return Formatted error description
	 */
	protected String getFullDescription(IStatus status, boolean includeCause) {
		StringBuffer result = new StringBuffer();

		final Throwable t = status.getException();
		assert t != null;

		final String exceptionMessage;
		if (t.getLocalizedMessage() != null) {
			exceptionMessage = t.getLocalizedMessage();
		} else {
			if (t.getMessage() != null) {
				exceptionMessage = t.getMessage();
			} else {
				exceptionMessage = ""; // NON-NLS-1
			}
		}

		if (exceptionMessage.equals(status.getMessage())) {
			result.append(status.getMessage());
		} else {
			result.append(status.getMessage() + "\n\n" + exceptionMessage); //$NON-NLS-1$
		}

		if (includeCause) {
			for (Throwable cause = t.getCause(); cause != null; cause = cause
					.getCause()) {
				result.append("\n"
						+ Messages.getString("LuminaErrorDialog.cause")
						+ cause.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$            
			}
		}

		if (t instanceof LuminaException) {
			String extraInfo = ((LuminaException) t).getExtraInfo();
			if (extraInfo != null && extraInfo.length() > 0)
				result.append("\n\n" + extraInfo); //$NON-NLS-1$
		}

		return result.toString();
	}

	/**
	 * Set the status displayed by this error dialog to the given status. This
	 * only affects the status displayed by the Details list.
	 * 
	 * @param status
	 *            The status to be displayed in the details list
	 */
	protected final void setStatus(IStatus status) {
		setStatus(status, true);
	}

	/**
	 * Set the status displayed by this error dialog to the given status. This
	 * only affects the status displayed by the Details list.
	 * 
	 * @param status
	 *            The status to be displayed in the details list
	 * @param includeCause
	 *            indicates if the cause should be included
	 */
	protected final void setStatus(IStatus status, boolean includeCause) {
		if (this.status != status) {
			this.status = status;

			if (detailsCreated)
				populateDetails(status, includeCause);
		}
	}

	/**
	 * Populate the Details field with the messages from the given status.
	 * 
	 * @param status
	 *            The status being displayed
	 */
	protected void populateDetails(IStatus status) {
		populateDetails(status, true);
	}

	/**
	 * Populate the Details field with the messages from the given status.
	 * 
	 * @param status
	 *            The status being displayed
	 * @param includeCause
	 *            indicates if the cause should be included
	 */
	protected void populateDetails(IStatus status, boolean includeCause) {
		final String description = getFullDescription(status, includeCause);

		details.setText(description);
	}

	/**
	 * Configure shell.
	 * 
	 * @param shell
	 *            new shell
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	/**
	 * Returns the image.
	 * 
	 * @return image
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#getImage()
	 */
	protected Image getImage() {
		return getErrorImage();
	}

	/**
	 * Creates the Lumina error dialog window.
	 * 
	 * @param parent
	 *            parent component
	 * @return error dialog area
	 * @see org.eclipse.jface.dialogs.ErrorDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		createMessageArea(parent);

		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData childData = new GridData(GridData.FILL_BOTH);
		childData.horizontalSpan = 2;
		composite.setLayoutData(childData);
		composite.setFont(parent.getFont());
		return composite;
	}

	/**
	 * Create buttons for button toolbar.
	 * 
	 * @param parent
	 *            parent component
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Details buttons
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	/**
	 * Creates the dialog area and button area.
	 * 
	 * @param parent
	 *            parent component
	 * @see IconAndMessageDialog#createDialogAndButtonArea(Composite)
	 */
	protected void createDialogAndButtonArea(Composite parent) {
		super.createDialogAndButtonArea(parent);
		if (this.dialogArea instanceof Composite) {
			// Create a label if there are no children to force a smaller layout
			Composite dialogComposite = (Composite) dialogArea;
			if (dialogComposite.getChildren().length == 0) {
				new Label(dialogComposite, SWT.NULL);
			}
		}
	}

	/**
	 * Create the error list if required.
	 * 
	 * @param parent
	 *            the Control to create it in
	 */
	private void createStatusList(Composite parent) {
		if (statusListViewer == null) {
			// The error list doesn't exist so create it.
			setMessage(Messages.getString("LuminaErrorDialog.multipleErrors")); //$NON-NLS-1$
			getShell().setText(
					Messages.getString("LuminaErrorDialog.titleMultiple")); //$NON-NLS-1$
			createStatusListArea(parent);
			showDetailsArea();
		}
		refreshStatusList();
	}

	/**
	 * This method sets the message in the message label.
	 * 
	 * @param messageString
	 *            the String for the message area
	 */
	private void setMessage(String messageString) {
		// must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed()) {
			return;
		}
		messageLabel.setText(message);
	}

	/**
	 * Create an area that allow the user to select one of multiple jobs that
	 * have reported errors.
	 * 
	 * @param parent
	 *            the parent of the area
	 */
	private void createStatusListArea(Composite parent) {
		// Display a list of jobs that have reported errors
		statusListViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		Control control = statusListViewer.getControl();
		GridData data = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		data.heightHint = convertHeightInCharsToPixels(ERRORS_SIZE);
		control.setLayoutData(data);
		statusListViewer.setContentProvider(getStatusContentProvider());
		statusListViewer.setLabelProvider(getStatusListLabelProvider());
		statusListViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						handleSelectionChange();
					}
				});
		applyDialogFont(parent);
		statusListViewer.setInput(this);
	}

	/**
	 * Return the label provider for the status list.
	 * 
	 * @return cell label provider
	 */
	private CellLabelProvider getStatusListLabelProvider() {
		return new CellLabelProvider() {
			public void update(ViewerCell cell) {
				cell.setText(((IStatus) cell.getElement()).getMessage());
			}
		};
	}

	/**
	 * Return the content provider for the statuses.
	 * 
	 * @return structured content provider
	 */
	private IStructuredContentProvider getStatusContentProvider() {
		return new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return statuses.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		};
	}

	/**
	 * Create this dialog's drop-down list component.
	 * 
	 * @param parent
	 *            the parent composite
	 * @return the drop-down list component
	 */
	protected Text createDetailsField(Composite parent) {
		details = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL);
		data.heightHint = details.getLineHeight() * DETAILS_SIZE;
		data.horizontalSpan = 2;
		details.setLayoutData(data);
		details.setFont(parent.getFont());

		populateDetails(status);
		detailsCreated = true;

		return details;
	}

	/**
	 * Show the details portion of the dialog if it is not already visible. This
	 * method will only work when it is invoked after the control of the dialog
	 * has been set. In other words, after the <code>createContents</code>
	 * method has been invoked and has returned the control for the content area
	 * of the dialog. Invoking the method before the content area has been set
	 * or after the dialog has been disposed will have no effect.
	 * 
	 * @since 3.1
	 */
	protected final void showDetailsArea() {
		if (!detailsCreated) {
			Control control = getContents();
			if (control != null && !control.isDisposed()) {
				toggleDetailsArea();
			}
		}
	}

	/**
	 * Toggles the unfolding of the details area. This is triggered by more than
	 * one error being displayed.
	 */
	private void toggleDetailsArea() {
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (detailsCreated) {
			details.dispose();
			detailsCreated = false;
		} else {
			details = createDetailsField((Composite) getContents());
		}
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getShell()
				.setSize(
						new Point(windowSize.x, windowSize.y
								+ (newSize.y - oldSize.y)));
	}

	/**
	 * Refresh the contents of the viewer.
	 */
	private void refreshStatusList() {
		if (statusListViewer != null
				&& !statusListViewer.getControl().isDisposed()) {
			statusListViewer.refresh();
			statusListViewer.getTable().select(0);
			Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
			getShell().setSize(newSize);
		}
	}

	/**
	 * Get the single selection.<br/>
	 * Return null if the selection is not just one element.
	 * 
	 * @return status or <code>null</code>.
	 */
	private IStatus getSingleSelection() {
		ISelection rawSelection = statusListViewer.getSelection();
		if (rawSelection != null
				&& rawSelection instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) rawSelection;
			if (selection.size() == 1) {
				return (IStatus) selection.getFirstElement();
			}
		}
		return null;
	}

	/**
	 * The selection in the multiple job list has changed. Update the error
	 * details.
	 */
	private void handleSelectionChange() {
		IStatus newSelection = getSingleSelection();
		setStatus(newSelection);
	}

}
