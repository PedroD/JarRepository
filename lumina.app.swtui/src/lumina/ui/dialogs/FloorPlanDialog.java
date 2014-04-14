package lumina.ui.dialogs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lumina.base.model.Floor;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.network.LuminaException;
import lumina.ui.jface.EnhancedFileDialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import codebase.Files;

/**
 * Dialog for picking a floor plan image.
 * <p>
 * The images listed are those available in the project. It's also possible to
 * import more images into the project, and to remove existing images.
 */
public class FloorPlanDialog extends Dialog {

	private static final int DIALOG_DEFAULT_HEIGHT = 400;

	private static final int DIALOG_DEFAULT_WIDHT = 400;

	private static final int DEFAULT_DIALOG_HEIGHT = 300;

	private static final int DEFAULT_DIALOG_WIDHT = 300;

	/**
	 * The reference to the parent floor of the property.
	 */
	private final Floor parentFloor;

	/**
	 * Used for displaying the confirmation dialogs.
	 */
	private final Shell parentShell;

	/**
	 * Holds the initial selection value. Necessary for setting the old image
	 * name when the user pressed cancel. Can be <code>null</code> if the user
	 * removes the floor. Then cancel is not possible.
	 */
	private String initialFloorName;

	/**
	 * Keeps track of the selected value so it can be retrieved after the dialog
	 * has been closed.
	 */
	private String selection = Floor.NO_FLOOR_IMAGE_SELECTION;

	// widgets
	private TableViewer tableViewer;
	private Table table;
	private Button bImport;
	private Button bExport;
	private Button bRemove;

	private Button bCancel;

	private Button readjustCoordsButton;

	/**
	 * Content provider for floor plans.
	 */
	private static class FloorPlansContentProvider implements
			IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			final List<String> imagePlanNames = new ArrayList<String>();
			final String[] imageNames = ProjectModel.getInstance().getProject()
					.listFloorPlanImages();
			Collections.addAll(imagePlanNames, imageNames);
			imagePlanNames.add(Floor.NO_FLOOR_IMAGE_SELECTION);
			return imagePlanNames.toArray(new Object[0]);
		}

		/**
		 * Terminate.
		 */
		public void dispose() {
		}

		/**
		 * Input has changed.
		 * 
		 * @param viewer
		 *            viewer
		 * @param oldInput
		 *            object
		 * @param newInput
		 *            object
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private void doChangeToFloorImage(final String imageName) {
		ProjectModel.getInstance().changeFloorPlanPreview(parentFloor,
				imageName);
	}

	private void doUpdateSelection() {
		if (tableViewer != null && table != null) {
			final TableItem[] items = table.getItems();

			for (int i = 0; i < items.length; i++) {
				final Object data = items[i].getData();
				if (data != null && data instanceof String) {
					final String imageName = (String) data;

					if (selection == null
							|| imageName.equals(Floor.NO_FLOOR_IMAGE_SELECTION)) {
						table.select(i);
					}

					if (selection != null && imageName.equals(selection)) {
						table.select(i);
						break;
					}
				}
			}
		}

		doUpdateButtons();
	}

	private void doUpdateButtons() {
		if (bExport != null && bRemove != null) {
			if (selection == null
					|| selection.equals(Floor.NO_FLOOR_IMAGE_SELECTION)) {
				bExport.setEnabled(false);
				bRemove.setEnabled(false);
			} else {
				bExport.setEnabled(true);
				bRemove.setEnabled(true);
			}
		}

		if (bCancel != null) {
			/*
			 * Canceling is only possible if the initial value exists
			 */
			bCancel.setEnabled(false);
			if (tableViewer != null && table != null) {
				final TableItem[] items = table.getItems();

				for (int i = 0; i < items.length; i++) {
					final Object data = items[i].getData();
					if (data != null && data instanceof String) {
						final String imageName = (String) data;
						if (imageName.equals(initialFloorName)) {
							bCancel.setEnabled(true);
						}
					}
				}
			}
		}

		if (readjustCoordsButton != null) {
			if ((initialFloorName != null && initialFloorName
					.equals(Floor.NO_FLOOR_IMAGE_SELECTION))
					|| selection == null
					|| (selection != null && selection
							.equalsIgnoreCase(Floor.NO_FLOOR_IMAGE_SELECTION))) {
				readjustCoordsButton.setEnabled(false);
			} else {
				readjustCoordsButton.setEnabled(true);
			}
		}
	}

	private void doRefreshTable() {
		if (tableViewer != null && table != null) {
			tableViewer.refresh();
		}

		doUpdateSelection();
	}

	/**
	 * Constructor for the floor plan dialog.
	 * 
	 * @param shell
	 *            shell
	 * @param floor
	 *            floor
	 */
	public FloorPlanDialog(final Shell shell, final Floor floor) {
		super(shell);

		parentShell = shell;
		parentFloor = floor;

		this.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL
				| SWT.BORDER | SWT.RESIZE);
	}

	/**
	 * Configure the shell.
	 * 
	 * @param newShell
	 *            new shell
	 */
	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setMinimumSize(DEFAULT_DIALOG_WIDHT, DEFAULT_DIALOG_HEIGHT);
		final Object[] messageArguments = { parentFloor.getName() };
		newShell.setText(Messages.getString(
				"FloorPlanDialog.selectFloorPlan", messageArguments)); //$NON-NLS-1$
	}

	/**
	 * Creates the dialog area.
	 * 
	 * @param parent
	 *            parent component
	 * @return dialog area
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);

		final Group group = new Group(container, SWT.NONE);
		group.setText(Messages.getString("FloorPlanDialog.images")); //$NON-NLS-1$
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		group.setLayout(gridLayout);

		tableViewer = new TableViewer(group, SWT.BORDER);
		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						doTableViewerSelectionChanged(event);
					}
				});
		tableViewer.setContentProvider(new FloorPlansContentProvider());
		tableViewer.setInput(new Object());
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite composite = new Composite(group, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
		rowLayout.fill = true;
		composite.setLayout(rowLayout);

		bImport = new Button(composite, SWT.NONE);
		bImport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doButtonImportWidgetSelected(e);
			}
		});
		bImport.setText(Messages.getString("FloorPlanDialog.import")); //$NON-NLS-1$

		bExport = new Button(composite, SWT.NONE);
		bExport.setEnabled(false);
		bExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doButtonExportWidgetSelected(e);
			}
		});
		bExport.setText(Messages.getString("FloorPlanDialog.export")); //$NON-NLS-1$

		bRemove = new Button(composite, SWT.NONE);
		bRemove.setEnabled(false);
		bRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doButtonRemoveWidgetSelected(e);
			}
		});
		bRemove.setText(Messages.getString("FloorPlanDialog.remove")); //$NON-NLS-1$

		final Group whenTheImageGroup = new Group(container, SWT.NONE);
		whenTheImageGroup.setText("When changing the image resolution");
		whenTheImageGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false));
		whenTheImageGroup.setLayout(new GridLayout());

		readjustCoordsButton = new Button(whenTheImageGroup, SWT.CHECK);
		readjustCoordsButton.setSelection(true);
		final GridData gdReadjustCoordsButton = new GridData(SWT.LEFT,
				SWT.CENTER, true, true);
		readjustCoordsButton.setLayoutData(gdReadjustCoordsButton);
		readjustCoordsButton
				.setText("Adjust the coordinates of device figures automatically");
		readjustCoordsButton.setSelection(ProjectModel.getInstance()
				.getAdjustDeviceCoordinates());

		readjustCoordsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doButtonAdjustCoordsButtonWidgetSelected(e);
			}
		});

		doRefreshTable();

		return container;
	}

	/**
	 * Create buttons for button bar.
	 * 
	 * @param parent
	 *            parent component
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		final Button bOk = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);

		bOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (selection != null && !selection.equals(initialFloorName)) {
					ProjectModel.getInstance().acceptDeviceCoordinates(
							parentFloor, selection);
				} else {
					ProjectModel.getInstance().setAdjustDeviceCoordinates(
							parentFloor, selection, false);
				}
			}
		});

		bCancel = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);

		bCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (initialFloorName != null) {
					ProjectModel.getInstance().setAdjustDeviceCoordinates(
							parentFloor, selection, false);

					doChangeToFloorImage(initialFloorName);
				}
			}
		});
	}

	/**
	 * Returns the predefined initial size.
	 * 
	 * @return initial size
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(DIALOG_DEFAULT_WIDHT, DIALOG_DEFAULT_HEIGHT);
	}

	/**
	 * Gets the currently selected plan image.
	 * 
	 * @return the selected floor plan.
	 */
	public String getSelection() {
		if (selection == null) {
			return Floor.NO_FLOOR_IMAGE_SELECTION;
		} else {
			return selection;
		}
	}

	/**
	 * Returns the initial selection.
	 * 
	 * @param imageName
	 *            image name
	 */
	public void setInitialSelection(final String imageName) {
		selection = imageName;
		initialFloorName = selection;

		doRefreshTable();
	}

	/**
	 * Event handlers for selection change.
	 * 
	 * @param event
	 *            event
	 */
	protected void doTableViewerSelectionChanged(SelectionChangedEvent event) {
		if (table.getSelectionCount() == 0) {
			selection = null;
		} else {
			assert table.getSelectionCount() > 0;

			selection = table.getSelection()[0].getText();
		}

		doUpdateButtons();
		doChangeToFloorImage(selection);
	}

	/**
	 * Import image.
	 * 
	 * @param e
	 *            event
	 */
	protected void doButtonImportWidgetSelected(SelectionEvent e) {
		EnhancedFileDialog dialog = new EnhancedFileDialog(this.getShell(),
				SWT.OPEN);
		dialog.setText(Messages.getString("FloorPlanDialog.importImage")); //$NON-NLS-1$
		dialog.setFilterExtensions(new String[] { "*.wmf" }); //$NON-NLS-1$
		dialog.setFilterNames(new String[] { "Windows Meta File (*.wmf)" }); //$NON-NLS-1$

		String filePath = dialog.open();
		if (filePath != null) {
			try {
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				FileInputStream in = new FileInputStream(filePath);
				Files.copyStream(in, byteStream);
				in.close();

				String name = new File(filePath).getName();
				byte[] bytes = byteStream.toByteArray();
				ProjectModel.getInstance().getProject()
						.addFloorPlanImage(name, bytes);

				tableViewer.refresh();
			} catch (IOException ex) {
				throw new LuminaException("Could not import image.", ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Expoer imagem.
	 * 
	 * @param e
	 *            event
	 */
	protected void doButtonExportWidgetSelected(SelectionEvent e) {
		if (selection == null)
			return;

		EnhancedFileDialog dialog = new EnhancedFileDialog(this.getShell(),
				SWT.SAVE);
		dialog.setText(Messages.getString("FloorPlanDialog.exportImage")); //$NON-NLS-1$
		dialog.setFileName(selection);

		String filePath = dialog.open();
		if (filePath != null) {
			byte[] bytes = ProjectModel.getInstance().getProject()
					.getFloorPlanImage(selection);
			try {
				FileOutputStream out = new FileOutputStream(filePath);
				out.write(bytes);
				out.close();
			} catch (IOException ex) {
				throw new LuminaException("Could not export image.", ex); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Adjusts image coordenates.
	 * 
	 * @param e
	 *            event
	 */
	protected void doButtonAdjustCoordsButtonWidgetSelected(SelectionEvent e) {
		if (selection != null) {
			final boolean adjust = readjustCoordsButton.getSelection();
			ProjectModel.getInstance().setAdjustDeviceCoordinates(parentFloor,
					selection, adjust);
		}
	}

	/**
	 * Revome image.
	 * 
	 * @param e
	 *            event
	 */
	protected void doButtonRemoveWidgetSelected(SelectionEvent e) {
		if (selection != null) {
			final Project p = ProjectModel.getInstance().getProject();
			final Floor[] floors = Queries.getAllFloorsWithImage(selection, p);

			final boolean souldRemove;
			if (floors.length == 1 && floors[0] == parentFloor) {
				souldRemove = true;
			} else if (floors.length > 0) {
				souldRemove = MessageDialog
						.openConfirm(
								parentShell,
								"Realy delete?",
								"The image '"
										+ selection
										+ "' is used by other floors in the project.\nDo you really want to remove it? \n \nNote: This operation cannot be undone.");
			} else {
				souldRemove = true;
			}

			if (souldRemove) {
				if (selection.equals(initialFloorName)) {
					initialFloorName = null;
				}

				ProjectModel.getInstance().removeFloorPlanImage(selection, p);
				doRefreshTable();
			}
		}
	}
}
