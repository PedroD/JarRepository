package lumina.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lumina.Activator;
import lumina.api.properties.IProperty;
import lumina.base.model.Device;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.Queries;
import lumina.base.model.commands.Action;
import lumina.base.model.commands.CommandProperty;
import lumina.base.model.commands.DeviceCommand;
import lumina.base.model.commands.PanelActions;
import lumina.extensions.base.properties.ChoiceProperty;
import lumina.extensions.base.properties.ProxyProperty;
import lumina.extensions.base.properties.TextProperty;
import lumina.kernel.osgi.factories.PropertyEditorFactory;
import lumina.kernel.osgi.registries.PropertyEditorFactoryRegistry;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.swt.ApplicationImageCache;
import lumina.ui.views.control.panels.UIConstants;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import com.swtdesigner.ResourceManager;

/**
 * Editor for the actions of the control panel.
 */
public class ControlPanelActionEditorDialog extends Dialog {

	private static final int DEFAULT_DIALOG_HEIGHT = 280;

	private static final int DEFAULT_DIALOG_WIDTH = 530;

	/**
	 * The dialog title message.
	 */
	private static final String DIALOG_TITLE = Messages
			.getString("ControlPanelActionEditorDialog.title"); //$NON-NLS-1$

	/**
	 * The path for the icons of the dialog.
	 */
	private static final String DIALOG_ICON_PATH = "icons/devices/control_panel.png"; //$NON-NLS-1$

	/**
	 * The Constant ADD_IMAGE_PATH.
	 */
	private static final String ADD_IMAGE_PATH = "/icons/dialogs/add_obj.gif"; //$NON-NLS-1$

	/**
	 * The Constant REMOVE_IMAGE_PATH.
	 */
	private static final String REMOVE_IMAGE_PATH = "/icons/dialogs/delete_obj.gif"; //$NON-NLS-1$

	/**
	 * Title of the scenario number column.
	 */
	private static final String COLUMN_COMMAND_NAME = Messages
			.getString("ControlPanelActionEditorDialog.action");

	/**
	 * Title of the label column.
	 */
	private static final String COLUMN_LABEL_NAME = Messages
			.getString("ControlPanelActionEditorDialog.label");

	/**
	 * Title of the parameter column.
	 */
	private static final String COLUMN_PARAMETER_NAME = Messages
			.getString("ControlPanelActionEditorDialog.parameter");

	/**
	 * Title of the parameter column.
	 */
	private static final String COLUMN_DEVICE_NAME = Messages
			.getString("ControlPanelActionEditorDialog.device");

	/**
	 * The Constant LABEL_COLUMN_INDEX.
	 */
	private static final int LABEL_COLUMN_INDEX = 0;

	/**
	 * The Constant COMMAND_COLUMN_INDEX.
	 */
	private static final int COMMAND_COLUMN_INDEX = 1;

	/**
	 * The Constant PARAMETER_COLUMN_INDEX.
	 */
	private static final int PARAMETER_COLUMN_INDEX = 2;

	/**
	 * The Constant DEVICE_COLUMN_INDEX.
	 */
	private static final int DEVICE_COLUMN_INDEX = 3;

	/**
	 * The table.
	 */
	private Tree table;

	/**
	 * The table viewer.
	 */
	private TreeViewer tableViewer;

	/**
	 * The sorter of the actions.
	 */
	private final ActionViewerSorter actionViewerSorter = new ActionViewerSorter();

	/**
	 * Holds the list of actions being edited.
	 */
	private PanelActions actionsCurrentlyBeeingEdited;

	/**
	 * The panel actions.
	 */
	private PanelActions panelActions;

	/*
	 * Add & Remove buttons
	 */
	/**
	 * The add action button.
	 */
	private Button addActionButton;

	/**
	 * The remove action button.
	 */
	private Button removeActionButton;

	/**
	 * Label provider for {@link Action} objects.
	 */
	private static class ActionLabelProvider implements ITableLabelProvider,
			ITableColorProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.
		 * lang.Object, int)
		 */
		public org.eclipse.swt.graphics.Color getBackground(Object element,
				int columnIndex) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.
		 * lang.Object, int)
		 */
		public org.eclipse.swt.graphics.Color getForeground(Object element,
				int columnIndex) {
			if (element instanceof Action) {
				switch (columnIndex) {
				case LABEL_COLUMN_INDEX:
					return UIConstants.getReadOnlyColor();
				case COMMAND_COLUMN_INDEX:
					return null;
				case PARAMETER_COLUMN_INDEX: {
					final Action action = (Action) element;
					if (action.getCommand().hasParameter())
						return null;
					else
						return UIConstants.getReadOnlyColor();
				}
				default:
					return null;
				}
			}
			return null;
		}

		/**
		 * Gets the image for the specified column.
		 * <p>
		 * Does nothing.
		 * 
		 * @param arg0
		 *            ignored
		 * @param arg1
		 *            ignored
		 * @return <code>null</code>
		 */
		public org.eclipse.swt.graphics.Image getColumnImage(Object arg0,
				int arg1) {
			return null;
		}

		/**
		 * Gets the text for the specified column.
		 * 
		 * @param element
		 *            the player
		 * @param columnIndex
		 *            the column
		 * @return String
		 */
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Action) {
				final Action action = (Action) element;
				final DeviceCommand command = action.getCommand();
				if (command == null) {
					return Messages
							.getString("ControlPanelActionEditorDialog.noCommand");
				}

				switch (columnIndex) {
				case LABEL_COLUMN_INDEX:
					return action.getLabel();
				case COMMAND_COLUMN_INDEX:
					return command.getDescription();
				case PARAMETER_COLUMN_INDEX: {
					if (command.hasParameter())
						return action.getCommand().getParameters().getValue()
								.toString();
					else
						return "--";
				}
				case DEVICE_COLUMN_INDEX: {
					if (command.isNetwork())
						return "--";
					else if (action.getDevice() == null)
						return Messages
								.getString("ControlPanelActionEditorDialog.noCommand");
					else
						return action.getDevice().toString();
				}
				default:
					return null;
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse
		 * .jface. viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener arg0) {
			/* do nothing */
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			/* do nothing */
		}

		/**
		 * Checks whether the specified property affects the label if changed.
		 * 
		 * @param arg0
		 *            the player
		 * @param arg1
		 *            the property
		 * @return boolean
		 */
		public boolean isLabelProperty(Object arg0, String arg1) {
			return false;
		}

		/**
		 * Removes the specified tableRowPropertyTooltip.
		 * 
		 * @param arg0
		 *            the tableRowPropertyTooltip
		 */
		public void removeListener(ILabelProviderListener arg0) {
			/* do nothing */
		}
	}

	/**
	 * This class provides the content for the actions table.
	 * 
	 * @author Ricardo Ferreira
	 */
	static class PanelActionsContentProvider implements ITreeContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
			// nothing to dispose
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang
		 * .Object)
		 */
		@Override
		public Object[] getChildren(Object parentElement) {
			Object[] elements = ((PanelActions) parentElement).getActions()
					.toArray();
			return elements;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang
		 * .Object)
		 */
		@Override
		public Object getParent(Object element) {
			return null;
		}

		/**
		 * Gets the elements for the table.
		 * 
		 * @param item
		 *            the model
		 * @return Object[]
		 */
		public Object[] getElements(Object item) {
			return getChildren(item);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang
		 * .Object)
		 */
		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof PanelActions) {
				return ((PanelActions) element).size() != 0;
			} else
				return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse
		 * .jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	/**
	 * This class implements the sorting for the Action Table.
	 * 
	 * @author Ricardo Ferreira
	 */

	static class ActionViewerSorter extends ViewerSorter {

		/**
		 * The Constant ASCENDING.
		 */
		private static final int ASCENDING = 0;

		/**
		 * The Constant DESCENDING.
		 */
		private static final int DESCENDING = 1;

		/**
		 * The column.
		 */
		private int column;

		/**
		 * The direction.
		 */
		private int direction;

		/**
		 * Does the sort. If it's a different column from the previous sort, do
		 * an ascending sort. If it's the same column as the last sort, toggle
		 * the sort direction.
		 * 
		 * @param column
		 *            the column
		 * @return the int
		 */
		public int doSort(int column) {
			if (column == this.column) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.column = column;
				direction = ASCENDING;
			}
			return direction == DESCENDING ? SWT.DOWN : SWT.UP;
		}

		/**
		 * Compares the object for sorting.
		 * 
		 * @param viewer
		 *            the viewer
		 * @param obj1
		 *            the obj1
		 * @param obj2
		 *            the obj2
		 * @return the int
		 */
		public int compare(Viewer viewer, Object obj1, Object obj2) {
			int rc = 0;
			Action a1 = (Action) obj1;
			Action a2 = (Action) obj2;

			// Determine which column and do the appropriate sorting
			switch (column) {
			case LABEL_COLUMN_INDEX:
				if (a1.getLabel().length() > a2.getLabel().length())
					rc = 1;
				else if (a1.getLabel().length() < a2.getLabel().length())
					rc = -1;
				else
					rc = a1.getLabel().compareToIgnoreCase(a2.getLabel());
				break;
			case COMMAND_COLUMN_INDEX:
				// if(a1.getCommand().length() > a2.getCommand().length())
				// rc = 1;
				// else if(a1.getCommand().length() < a2.getCommand().length())
				// rc = -1;
				// rc = a1.getCommand().compareToIgnoreCase(a2.getCommand());
				rc = a1.getCommand().toString()
						.compareTo(a2.getCommand().toString());
				break;
			}

			// If descending order, flip the direction
			if (direction == DESCENDING)
				rc = -rc;

			return rc;
		}

	}

	/**
	 * Base class for the editing support.
	 */
	private abstract static class ActionColumnEditingSupport extends
			EditingSupport {
		/**
		 * The treeTable viewer.
		 */
		private final TreeViewer viewer;

		/**
		 * Instantiates a new properties editing support.
		 * 
		 * @param v
		 *            the viewer
		 */
		public ActionColumnEditingSupport(final TreeViewer v) {
			super(v);
			this.viewer = v;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
		 */
		@Override
		protected boolean canEdit(Object element) {
			return (element instanceof Action);
		}

		/**
		 * Creates the appropriate property editor for the property
		 * corresponding to this editor column.
		 * 
		 * @param action
		 *            the action object for which we need the property.
		 * @return a property for a label, command or command parameter
		 */
		protected abstract IProperty createLabelPropertyForAction(
				final Action action);

		private final CellEditor createCellEditor(final Composite parent,
				IProperty p) {
			final PropertyEditorFactory factory = PropertyEditorFactoryRegistry.DEFAULT_INSTANCE
					.findEditorFactory("SWT", p.getPropertyType().getClass());
			if (factory != null) {
				return (CellEditor) factory.getNewExtensionInstance(parent, null);
			} else {
				throw new IllegalStateException(
						"Cannot find a registered property editor factory for class:"
								+ getClass());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.
		 * Object)
		 */
		@Override
		protected CellEditor getCellEditor(final Object element) {
			if (element instanceof Action) {
				final Action a = (Action) element;
				final IProperty commandProperty = createLabelPropertyForAction(a);
				return createCellEditor(viewer.getTree(), commandProperty);
			}
			return null;
		}

		// public final Object getValueForCellEditor() {
		// final Object value =
		// SWTPropertyEditorFactories.getValueForCellEditor(getChoices(),
		// this.getValue().toString());
		// if (value.equals(0)) {
		// return Boolean.TRUE;
		// } else {
		// return Boolean.FALSE;
		// }
		// }

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
		 */
		@Override
		protected Object getValue(final Object element) {
			if (element instanceof Action) {
				final Action a = (Action) element;
				final IProperty p = createLabelPropertyForAction(a);
				return p.getValue();
				// return p.getValueForCellEditor();
			} else {
				return element.toString();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object,
		 * java.lang.Object)
		 */
		@Override
		protected void setValue(final Object element, final Object value) {
			if (element instanceof Action) {
				final Action a = (Action) element;
				final IProperty p = createLabelPropertyForAction(a);
				final String[] choices = (String[]) p.getChoices();

				/*
				 * Read the selection from the widget
				 */
				final Object cellWidgetValue;
				if (choices != null && choices.length > 0
						&& value instanceof Integer) {
					final int comboIndex = (Integer) value;
					final boolean validSelection = comboIndex >= 0
							&& comboIndex < choices.length;
					if (validSelection) {
						cellWidgetValue = choices[comboIndex];
					} else {
						cellWidgetValue = value;
					}
				} else {
					cellWidgetValue = value;
				}

				p.setValue(cellWidgetValue);
			}
			getViewer().update(element, null);
		}
	}

	/**
	 * Label editing support object.
	 */
	private static class LabelEditingSupport extends ActionColumnEditingSupport {

		/**
		 * Instantiates a new properties editing support.
		 * 
		 * @param v
		 *            the v
		 */
		public LabelEditingSupport(final TreeViewer v) {
			super(v);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see lumina.ui.dialogs.ControlPanelActionEditorDialog.
		 * ActionColumnEditingSupport
		 * #createLabelPropertyForAction(lumina.base.model.commands.Action)
		 */
		@Override
		protected IProperty createLabelPropertyForAction(final Action action) {
			return new TextProperty() {
				@Override
				public boolean isReadOnly() {
					return false;
				}

				@Override
				public boolean isCloneable() {
					return false;
				}

				@Override
				public Object getValue() {
					return action.getLabel();
				}

				@Override
				public String getName() {
					return "Label";
				}

				@Override
				public String getDescription() {
					return "Action label";
				}

				@Override
				public void helperSetValue(Object value) {
					if (value instanceof String) {
						action.setLabel((String) value);
					}
				}
			};
		}
	}

	/**
	 * Command property editing suport.
	 */
	private static class CommandEditingSupport extends
			ActionColumnEditingSupport {

		/**
		 * Instantiates a new properties editing support.
		 * 
		 * @param v
		 *            the v
		 */
		public CommandEditingSupport(final TreeViewer v) {
			super(v);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see lumina.ui.dialogs.ControlPanelActionEditorDialog.
		 * ActionColumnEditingSupport
		 * #createLabelPropertyForAction(lumina.base.model.commands.Action)
		 */
		@Override
		protected IProperty createLabelPropertyForAction(final Action action) {
			return new CommandProperty() {
				@Override
				public void helperSetValue(Object value) {
					final DeviceCommand command = (DeviceCommand) value;
					action.setCommand(command);
				}

				@Override
				public Object getValue() {
					DeviceCommand c = action.getCommand();
					assert c.toString().equals(c.getDescription());
					return c;
				}

				@Override
				public String getName() {
					return "Action command";
				}

				@Override
				public String getDescription() {
					return action.getCommand().getDescription();
				}
			};
		}
	}

	/**
	 * Command property editing support.
	 */
	private static class CommandParameterEditingSupport extends
			ActionColumnEditingSupport {

		/**
		 * Instantiates a new properties editing support.
		 * 
		 * @param v
		 *            the v
		 */
		public CommandParameterEditingSupport(final TreeViewer v) {
			super(v);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see lumina.ui.dialogs.ControlPanelActionEditorDialog.
		 * ActionColumnEditingSupport#canEdit(java.lang.Object)
		 */
		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof Action) {
				return ((Action) element).getCommand().hasParameter();
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see lumina.ui.dialogs.ControlPanelActionEditorDialog.
		 * ActionColumnEditingSupport
		 * #createLabelPropertyForAction(lumina.base.model.commands.Action)
		 */
		@Override
		protected IProperty createLabelPropertyForAction(final Action action) {
			return new ProxyProperty(action.getCommand().getParameters());
		}
	}

	/**
	 * Device property editing support.
	 */
	private static class ActionDeviceEditingSupport extends
			ActionColumnEditingSupport {

		/**
		 * A readable text indicating that no timer is associated to the device.
		 */
		private static final String NO_DEVICE = Messages
				.getString("ControlPanelActionEditorDialog.actionDeviceProperty.noDevice"); // NON-NLS-1

		/**
		 * The device timer property.
		 */
		private static final class ActionDeviceProperty extends ChoiceProperty {

			/**
			 * The action.
			 */
			private final Action action;

			/**
			 * Instantiates a new action device property.
			 * 
			 * @param action
			 *            the action
			 */
			ActionDeviceProperty(Action action) {
				this.action = action;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see lumina.base.model.Property#getChoices()
			 */
			public String[] getChoices() {
				final Project p = ProjectModel.getInstance().getProject();
				final String[] deviceNames = Queries.getAllDeviceNames(p);

				final List<String> choices = new ArrayList<String>();
				choices.add(NO_DEVICE);
				if (deviceNames.length > 0) {
					choices.addAll(Arrays.asList(deviceNames));
				}

				return choices.toArray(new String[0]);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see lumina.base.model.Property#getDescription()
			 */
			public String getDescription() {
				return Messages
						.getString("ControlPanelActionEditorDialog.actionDeviceProperty.description"); // NON-NLS-1
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see lumina.base.model.Property#getName()
			 */
			public String getName() {
				return Messages
						.getString("ControlPanelActionEditorDialog.actionDeviceProperty.name"); // NON-NLS-1
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see lumina.base.model.Property#getValue()
			 */
			public Object getValue() {
				return action.getDevice();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see lumina.base.model.properties.BaseProperty#toString()
			 */
			public String toString() {
				final Device d = action.getDevice();
				if (d == null) {
					return NO_DEVICE;
				} else {
					return d.getName();
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see lumina.base.model.Property#isReadOnly()
			 */
			public boolean isReadOnly() {
				return !Capabilities
						.canDoAny(Capability.DEVICE_EDIT_PROP_SCENARIO);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * lumina.base.model.properties.BaseProperty#internal(java.lang.Object)
			 */
			@Override
			public Object parse(final String value) {
				final String strValue = value.toString();

				if (strValue.equalsIgnoreCase(NO_DEVICE)) {
					return null;
				} else {
					final Project p = ProjectModel.getInstance().getProject();
					final Device d = Queries.findDeviceByNameOrID(strValue, p);
					return d;
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * lumina.base.model.properties.BaseProperty#setValueInternal(java.lang
			 * .Object)
			 */
			public void helperSetValue(final Object value) {
				if (value != null) {
					final Device d = (Device) value;
					action.setDevice(d);
				}
			}
		};

		/**
		 * Instantiates a new properties editing support.
		 * 
		 * @param v
		 *            the TreeViewer object.
		 */
		public ActionDeviceEditingSupport(final TreeViewer v) {
			super(v);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see lumina.ui.dialogs.ControlPanelActionEditorDialog.
		 * ActionColumnEditingSupport#canEdit(java.lang.Object)
		 */
		@Override
		protected boolean canEdit(Object element) {
			if (element instanceof Action) {
				return !((Action) element).getCommand().isNetwork();
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see lumina.ui.dialogs.ControlPanelActionEditorDialog.
		 * ActionColumnEditingSupport
		 * #createLabelPropertyForAction(lumina.base.model.commands.Action)
		 */
		@Override
		protected IProperty createLabelPropertyForAction(final Action action) {
			return new ActionDeviceProperty(action);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param parentShell
	 *            parent shell
	 */
	public ControlPanelActionEditorDialog(Shell parentShell) {
		super(parentShell);

		this.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL
				| SWT.BORDER | SWT.RESIZE);
	}

	/**
	 * Returns the panel actions.
	 * 
	 * @return the panel actions the panel actions
	 */
	public PanelActions getPanelActions() {
		return this.panelActions;
	}

	/**
	 * Sets the panel actions.
	 * 
	 * @param panelActions
	 *            the new panel actions
	 */
	public void setPanelActions(PanelActions panelActions) {
		this.panelActions = new PanelActions(panelActions);
	}

	/**
	 * Configures the shell.
	 * 
	 * @param newShell
	 *            new shell
	 */
	@Override
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setMinimumSize(new Point(DEFAULT_DIALOG_WIDTH,
				DEFAULT_DIALOG_HEIGHT));
		newShell.setText(DIALOG_TITLE);
		newShell.setImage(ResourceManager.getPluginImage(
				Activator.getDefault(), DIALOG_ICON_PATH));
	}

	/**
	 * Creates the widgets for this editor.
	 * 
	 * @param parent
	 *            parent control
	 * @return control with the dialog area
	 */
	// CHECKSTYLE:OFF - Automatically generated code for the UI
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.marginBottom = -8;
		gridLayout.verticalSpacing = 6;

		// TableViewer
		tableViewer = new TreeViewer(container, SWT.BORDER | SWT.FULL_SELECTION);

		table = tableViewer.getTree();
		table.setHeaderVisible(true);
		table.setBackgroundMode(SWT.INHERIT_FORCE);
		table.setBackground(Display.getCurrent()
				.getSystemColor(SWT.COLOR_WHITE));
		table.setLinesVisible(true);

		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_table.heightHint = 183;
		table.setLayoutData(gd_table);

		// FIXME: Tooltips and messages

		final TreeViewerColumn labelColumn = new TreeViewerColumn(tableViewer,
				SWT.NONE);
		labelColumn.getColumn().setText(COLUMN_LABEL_NAME);
		labelColumn.getColumn().setWidth(200);
		labelColumn
				.getColumn()
				.setToolTipText(
						Messages.getString("ControlPanelActionEditorDialog.labelTooltip"));
		labelColumn.getColumn().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Set the sorter if it's not already active
				if (tableViewer.getSorter() == null)
					tableViewer.setSorter(actionViewerSorter);
				int dir = ((ActionViewerSorter) tableViewer.getSorter())
						.doSort(LABEL_COLUMN_INDEX);
				table.setSortColumn(labelColumn.getColumn());
				table.setSortDirection(dir);
				tableViewer.refresh();

			}
		});

		final TreeViewerColumn commandColumn = new TreeViewerColumn(
				tableViewer, SWT.NONE);
		commandColumn.getColumn().setText(COLUMN_COMMAND_NAME);
		commandColumn.getColumn().setToolTipText(
				Messages.getString("ControlPanelActionEditorDialog.idTooltip"));
		commandColumn.getColumn().setWidth(100);
		commandColumn.getColumn().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Set the sorter if it's not already active
				if (tableViewer.getSorter() == null)
					tableViewer.setSorter(actionViewerSorter);
				int dir = ((ActionViewerSorter) tableViewer.getSorter())
						.doSort(COMMAND_COLUMN_INDEX);
				table.setSortColumn(commandColumn.getColumn());
				table.setSortDirection(dir);
				tableViewer.refresh();

			}
		});

		final TreeViewerColumn parameterColumn = new TreeViewerColumn(
				tableViewer, SWT.NONE);
		parameterColumn.getColumn().setText(COLUMN_PARAMETER_NAME);
		parameterColumn.getColumn().setToolTipText(
				Messages.getString("ControlPanelActionEditorDialog.idTooltip"));
		parameterColumn.getColumn().setWidth(100);
		parameterColumn.getColumn().addSelectionListener(
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						// Set the sorter if it's not already active
						if (tableViewer.getSorter() == null)
							tableViewer.setSorter(actionViewerSorter);
						int dir = ((ActionViewerSorter) tableViewer.getSorter())
								.doSort(PARAMETER_COLUMN_INDEX);
						table.setSortColumn(commandColumn.getColumn());
						table.setSortDirection(dir);
						tableViewer.refresh();
					}
				});

		final TreeViewerColumn deviceColumn = new TreeViewerColumn(tableViewer,
				SWT.NONE);
		deviceColumn.getColumn().setText(COLUMN_DEVICE_NAME);
		deviceColumn.getColumn().setToolTipText(
				Messages.getString("ControlPanelActionEditorDialog.idTooltip"));
		deviceColumn.getColumn().setWidth(100);
		deviceColumn.getColumn().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Set the sorter if it's not already active
				if (tableViewer.getSorter() == null)
					tableViewer.setSorter(actionViewerSorter);
				int dir = ((ActionViewerSorter) tableViewer.getSorter())
						.doSort(DEVICE_COLUMN_INDEX);
				table.setSortColumn(commandColumn.getColumn());
				table.setSortDirection(dir);
				tableViewer.refresh();

			}
		});

		// Set the editors, cell modifier, and column properties

		tableViewer.setColumnProperties(new String[] { COLUMN_LABEL_NAME,
				COLUMN_COMMAND_NAME });
		// tableViewer.setCellModifier(new LabelCellModifier(tableViewer));

		tableViewer.setContentProvider(new PanelActionsContentProvider());
		tableViewer.setLabelProvider(new ActionLabelProvider());

		// Set up the temporary actions panel object
		actionsCurrentlyBeeingEdited = new PanelActions(panelActions);

		tableViewer.setInput(actionsCurrentlyBeeingEdited);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Cell Editors
		// tableViewer.setCellEditors(new CellEditor[] { new
		// ChoiceCellEditor(table),
		// new TextCellEditor(table) });
		labelColumn.setEditingSupport(new LabelEditingSupport(tableViewer));
		commandColumn.setEditingSupport(new CommandEditingSupport(tableViewer));
		parameterColumn.setEditingSupport(new CommandParameterEditingSupport(
				tableViewer));
		deviceColumn.setEditingSupport(new ActionDeviceEditingSupport(
				tableViewer));

		// commandColumn.
		// Create the types
		Transfer[] types = new Transfer[] { LocalSelectionTransfer
				.getTransfer() };

		// Create the drag source
		DragSource source = new DragSource(table, DND.DROP_MOVE);
		source.setTransfer(types);
		source.addDragListener(new DragSourceAdapter() {
			public void dragStart(DragSourceEvent event) {
				super.dragStart(event);
				tableViewer.setSorter(null);
				table.setSortColumn(null);
			}

			public void dragSetData(DragSourceEvent event) {

				// Set the transfer data
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				LocalSelectionTransfer.getTransfer().setSelection(selection);
			}

		});

		// Create the drop target
		DropTarget target = new DropTarget(table, DND.DROP_MOVE
				| DND.DROP_DEFAULT);

		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					event.detail = (event.operations & DND.DROP_MOVE) != 0 ? DND.DROP_MOVE
							: DND.DROP_NONE;
				}

				// Allow dropping table items only
				for (int i = 0, n = event.dataTypes.length; i < n; i++) {
					if (LocalSelectionTransfer.getTransfer().isSupportedType(
							event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				// Provide visual feedback
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

			public void drop(DropTargetEvent event) {
				// If any item was dropped . . .
				if (LocalSelectionTransfer.getTransfer().isSupportedType(
						event.currentDataType)) {
					// Get the dropped data
					IStructuredSelection selection = (IStructuredSelection) event.data;
					Action sourceItem = (Action) selection.getFirstElement();

					if (event.item != null) {
						Action destinationItem = (Action) event.item.getData();
						actionsCurrentlyBeeingEdited.moveTo(
								actionsCurrentlyBeeingEdited
										.indexOf(sourceItem),
								actionsCurrentlyBeeingEdited
										.indexOf(destinationItem));

						// update viewer
						tableViewer.refresh();
						return;
					} else {
						actionsCurrentlyBeeingEdited.moveTo(
								actionsCurrentlyBeeingEdited
										.indexOf(sourceItem),
								actionsCurrentlyBeeingEdited.size() - 1);

						// update viewer
						tableViewer.refresh();
						return;
					}
				}
			}
		});
		return container;
	}

	// CHECKSTYLE:ON
	/**
	 * Creates and returns the contents of this dialog's button bar.
	 * 
	 * @param parent
	 *            the parent composite to contain the button bar
	 * @return the button bar control
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 2; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		mainComposite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
		mainComposite.setLayoutData(data);
		mainComposite.setFont(parent.getFont());

		// leftComposite
		Composite leftComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.makeColumnsEqualWidth = true;
		leftComposite.setLayout(layout1);

		GridData data1 = new GridData(SWT.LEFT, SWT.NONE, true, false);
		leftComposite.setLayoutData(data1);
		leftComposite.setFont(mainComposite.getFont());

		// rightComposite
		Composite rightComposite = new Composite(mainComposite, SWT.NONE);
		GridLayout layout2 = new GridLayout();
		layout2.numColumns = 2;
		layout2.makeColumnsEqualWidth = true;
		rightComposite.setLayout(layout2);

		GridData data2 = new GridData(SWT.RIGHT, SWT.NONE, true, false);
		rightComposite.setLayoutData(data2);
		rightComposite.setFont(mainComposite.getFont());

		// Add the buttons to the button bar.
		createButtonsForButtonBar(rightComposite);

		// Create add action button
		addActionButton = new Button(leftComposite, SWT.PUSH);
		//        addActionButton.setText("+"); //$NON-NLS-1$
		addActionButton.setImage(ApplicationImageCache.getInstance().getImage(
				ADD_IMAGE_PATH));
		addActionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				actionsCurrentlyBeeingEdited.add(new Action());
				tableViewer.refresh();
			}
		});

		// Create remove action button
		removeActionButton = new Button(leftComposite, SWT.PUSH);
		//        removeActionButton.setText("-"); //$NON-NLS-1$
		removeActionButton.setImage(ApplicationImageCache.getInstance()
				.getImage(REMOVE_IMAGE_PATH));
		removeActionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (table.getSelection() != null) {
					IStructuredSelection selection = (IStructuredSelection) tableViewer
							.getSelection();
					Action selectedItem = (Action) selection.getFirstElement();
					actionsCurrentlyBeeingEdited.remove(selectedItem);
					tableViewer.refresh();
				}
			}
		});

		return mainComposite;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 *            control.
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);

	}

	/**
	 * Creates buttons OK and Cancel. When OK is pressed the contents of the
	 * temporary actions are saved in the original panel actions object. Cancel
	 * discards any changes that might have occurred.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param id
	 *            the id of the button (see IDialogConstants.*_ID constants for
	 *            standard dialog button ids)
	 * @param label
	 *            the label of the button
	 * @param defaultButton
	 *            true if the button is to be the default button, and false
	 *            otherwise
	 * @return the button
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label,
			boolean defaultButton) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(JFaceResources.getDialogFont());
		button.setData(Integer.valueOf(id));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				int buttonID = ((Integer) event.widget.getData()).intValue();
				switch (buttonID) {
				case IDialogConstants.OK_ID:
					setPanelActions(actionsCurrentlyBeeingEdited);
					buttonPressed(buttonID);
					break;
				case IDialogConstants.CANCEL_ID:
					actionsCurrentlyBeeingEdited.clear();
					buttonPressed(buttonID);
					break;
				default:
					break;
				}
			}
		});
		if (defaultButton) {
			Shell shell = parent.getShell();
			if (shell != null) {
				shell.setDefaultButton(button);
			}
		}
		// buttons.put(new Integer(id), button);
		setButtonLayoutData(button);
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		return new Point(DEFAULT_DIALOG_WIDTH, DEFAULT_DIALOG_HEIGHT);
	}
}
