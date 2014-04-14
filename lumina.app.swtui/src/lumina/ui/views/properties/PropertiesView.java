package lumina.ui.views.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lumina.api.properties.IProperty;
import lumina.api.properties.IPropertySet;
import lumina.base.model.Area;
import lumina.base.model.BaseNamedModelItem;
import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.DeviceTimer;
import lumina.base.model.Floor;
import lumina.base.model.IDeviceDriver;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelItemPropertyChangeEvent;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.Queries;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.kernel.Logger;
import lumina.kernel.osgi.factories.PropertyEditorFactory;
import lumina.kernel.osgi.registries.PropertyEditorFactoryRegistry;
import lumina.network.gateways.api.IGateway;
import lumina.ui.actions.ChangePropertyAction;
import lumina.ui.actions.MoveAreasAction;
import lumina.ui.actions.MoveDevicesAction;
import lumina.ui.actions.MoveSchedulesAction;
import lumina.ui.actions.MoveTasksAction;
import lumina.ui.celleditors.AbstractCellEditorWrapper;
import lumina.ui.views.AbstractTreeItemTooltip;
import lumina.ui.views.Messages;
import lumina.ui.views.ViewUtils;
import lumina.ui.views.control.panels.UIConstants;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.log.LogService;


// TODO: Auto-generated Javadoc
/**
 * Properties view.
 */
public class PropertiesView extends
        ViewPart
        implements ISelectionListener, IPropertyChangeListener {

    /**
     * Properties editing support for the TreeViewer.
     */
    private class PropertiesEditingSupport extends
            EditingSupport {

        /**
         * The treeTable viewer.
         */
        private final TreeViewer viewer;

        /**
         * Indicates if editing of a field is in progress.
         */
        private boolean isEditing;

        /**
         * The current property editor.
         */
        private AbstractCellEditorWrapper currentPropertyEditor;

        /**
         * Instantiates a new properties editing support.
         * 
         * @param parent the parent
         * @param p the p
         * @return the i property editor
         */
        private final AbstractCellEditorWrapper createCellEditor(final Composite parent, IProperty p) {
            final PropertyEditorFactory factory = PropertyEditorFactoryRegistry.DEFAULT_INSTANCE
                    .findEditorFactory("SWT", p.getPropertyType().getClass());
            if (factory != null) {
                return (AbstractCellEditorWrapper) factory.getNewExtensionInstance(parent, p);
            } else {
                throw new IllegalStateException(
                        "Cannot find a registered property editor factory for class:" + getClass());
            }
        }

        /**
         * Instantiates a new properties editing support.
         * 
         * @param v the v
         */
        public PropertiesEditingSupport(final TreeViewer v) {
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
            if (element instanceof IProperty) {
                final IProperty p = (IProperty) element;
                final boolean canEdit = !p.isReadOnly();
                return canEdit;
            } else {
                return false;
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
            if (element instanceof IProperty) {
                final IProperty property = (IProperty) element;
                final AbstractCellEditorWrapper propertyEditor = createCellEditor(viewer.getTree(),
                        property);
                final CellEditor cellEditor = (CellEditor) propertyEditor.getWidget();

                /*
                 * Listener responsible for canceling the edit mode.
                 */
                cellEditor.addListener(new ICellEditorListener() {
                	private boolean newValidState;
                	@Override
                	public void applyEditorValue() {
                		if (property != null) {
                            // XXX: setValue must be reafactored.
                            setValue(element, newValidState);
                        }
                        isEditing = false;
                    }

                    @Override
                    public void cancelEditor() {
                        isEditing = false;
                    }

                    @Override
                    public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                    	this.newValidState = newValidState;
                    }
                });

                /*
                 * Save a reference to the current property.
                 * 
                 * Logically, this initialization should be made on
                 * initializeCellEditorValue(). However, there we don't have the
                 * reference to the property anymore.
                 */
                propertyUnderInspection = property;

                /*
                 * Save a reference to the current property editor.
                 */
                currentPropertyEditor = propertyEditor;

                return cellEditor;
            } else {
                /*
                 * Prevent the interface from becoming defaced if the
                 */
                return new TextCellEditor(viewer.getTree());
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
         */
        @Override
        protected Object getValue(final Object element) {
            if (element instanceof IProperty) {
                final IProperty p = (IProperty) element;
                return currentPropertyEditor.getValueForWidget(p);
            } else {
                return element.toString();
            }
        }

        /*
         * Hack that intercepts the start of value editing of a property editor.
         * 
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.EditingSupport#initializeCellEditorValue
         * (org.eclipse .jface.viewers.CellEditor,
         * org.eclipse.jface.viewers.ViewerCell)
         */
        @Override
        protected void initializeCellEditorValue(CellEditor cellEditor, ViewerCell cell) {
            isEditing = true;
            super.initializeCellEditorValue(cellEditor, cell);
        }

        /**
         * Checks if is editing.
         * 
         * @return true, if is editing
         */
        public boolean isEditing() {
            return isEditing;
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
            if (element instanceof IProperty) {
                final IProperty p = (IProperty) element;
                final Object cellWidgetValue = currentPropertyEditor.getValueForProperty(p);

                // // XXX: necessita de refactoring urgente!
                /*
                 * Change the property of the selected object
                 */
                p.setValue(cellWidgetValue);
                
                final Object selectedElement = selection.getFirstElement();

                // ///XXX: Refactor move item!!!!!
                if (p.getName().equals(Device.PARENT_AREA_PROPERTY_NAME)) {
                    assert selectedElement instanceof Device;

                    if (selectedElement != null) {
                        final Device device = (Device) selectedElement;

                        final Area origin = device.getParentArea();
                        final Project project = Queries.getAncestorProject(origin);

                        assert cellWidgetValue instanceof String;

                        final String areaName = Area.stripParentName((String) cellWidgetValue);
                        final Area newArea = Queries.getAreaByNameOrID(areaName, project);

                        if (newArea != null && newArea != origin) {
                            assert newArea != null;
                            final Area destination = newArea;
                            final MoveDevicesAction moveDeviceAction = new MoveDevicesAction(
                                    new Device[] { device }, destination, null, PropertiesView.this
                                            .getSite().getWorkbenchWindow());
                            moveDeviceAction.run();
                        }
                    }
                } else if (p.getName().equals(Area.PARENT_FLOOR_PROPERTY_NAME)) {
                    assert selectedElement instanceof Area;
                    if (selectedElement != null) {
                        final Area area = (Area) selectedElement;
                        final Floor origin = area.getParentFloor();

                        final Project project = Queries.getAncestorProject(origin);

                        assert cellWidgetValue instanceof String;

                        final String floorName = (String) cellWidgetValue;
                        final Floor newFloor = Queries.getFloorByNameOrID(floorName, project);

                        if (newFloor != null && newFloor != origin) {
                            assert newFloor != null;
                            final Floor destination = newFloor;
                            final MoveAreasAction moveAreasAction = new MoveAreasAction(
                                    new Area[] { area }, destination, null, PropertiesView.this
                                            .getSite().getWorkbenchWindow());
                            moveAreasAction.run();
                        }
                    }
                } else if (p.getName().equals(Schedule.PARENT_DEVICE_TIMER_PROPERTY_NAME)) {
                    assert selectedElement instanceof Schedule;
                    if (selectedElement != null) {
                        final Schedule schedule = (Schedule) selectedElement;
                        final DeviceTimer origin = schedule.getParentTimer();

                        final Project project = Queries.getAncestorProject(origin);

                        assert cellWidgetValue instanceof String;

                        final String timerName = (String) cellWidgetValue;
                        final DeviceTimer newTimer = Queries.getDeviceTimerByName(project,
                                timerName);

                        if (newTimer != null && newTimer != origin) {
                            assert newTimer != null;
                            final DeviceTimer destination = newTimer;
                            final MoveSchedulesAction moveAction = new MoveSchedulesAction(
                                    new Schedule[] { schedule }, destination, null,
                                    PropertiesView.this.getSite().getWorkbenchWindow());
                            moveAction.run();
                        }
                    }
                } else if (p.getName().equals(Task.PARENT_SCHEDULE_PROPERTY_NAME)) {
                    assert selectedElement instanceof Task;
                    if (selectedElement != null) {
                        final Task task = (Task) selectedElement;
                        final Schedule origin = task.getParentSchedule();

                        final Project project = Queries.getAncestorProject(origin);

                        assert cellWidgetValue instanceof String;

                        final String scheduleName = Area
                                .stripParentName((String) cellWidgetValue);
                        final Schedule newSchedule = Queries.getScheduleByNameOrID(scheduleName,
                                project);

                        if (newSchedule != null && newSchedule != origin) {
                            assert newSchedule != null;
                            final Schedule destination = newSchedule;
                            final MoveTasksAction moveTasksAction = new MoveTasksAction(
                                    new Task[] { task }, destination, null, PropertiesView.this
                                            .getSite().getWorkbenchWindow());
                            moveTasksAction.run();
                        }
                    }
                } else {
                    if (selectedElement instanceof ModelItem) {
                        final ChangePropertyAction changeAction = new ChangePropertyAction(
                                (ModelItem) selectedElement, p, cellWidgetValue,
                                PropertiesView.this);
                        changeAction.run();
                    }
                }
            }
            getViewer().update(element, null);
        }
    }

    /**
     * Tracks changes in property values, descriptions and read-only status.
     * <p>
     * This class in used by {@link PropertyLabelProvider} and provide appropriate
     * highlighting of the property editor's cells.
     */
    private static final class PropertyChangeManager {
        /**
         * Maintains the values of a property in order to check whether the property has
         * changed and should be refreshed.
         */
        private static class CachedPropertyInfo {

            /**
             * The name.
             */
            private final String name;

            /**
             * The value.
             */
            private final Object value;

            /**
             * The is read only.
             */
            private final boolean isReadOnly;

            /**
             * Instantiates a new cached property info.
             * 
             * @param p the p
             */
            CachedPropertyInfo(final IProperty p) {
                name = p.getName();
                value = p.getValue();
                isReadOnly = p.isReadOnly();
            }

            /**
             * Checks for changed.
             * 
             * @param p the p
             * @return true, if successful
             */
            boolean hasChanged(final IProperty p) {
                final boolean valuesAreEqual = (p.getValue() == value)
                        || (p.getValue() != null && value != null && p.getValue().toString()
                                .equals(value.toString()));
                final boolean namesAreEqual = name.equals(p.getName());
                return !valuesAreEqual || !namesAreEqual || isReadOnly != p.isReadOnly();
            }
        }

        /**
         * The object being inspected.
         */
        private Object cachedInputObject;

        /**
         * The text values of the properties of the object being inspected. Used for
         * determining which properties have changed.
         */
        private Map<IProperty, CachedPropertyInfo> cachedInputObjectPropertyValues;

        /**
         * Maintains the set of properties last changed so that its background gets
         * colored.
         */
        private Set<IProperty> hilightedProperties;

        /**
         * Updates the value of a property.
         * <p>
         * The property must be one of the properties associated with the input object.
         * 
         * @param p the property to be updated
         * @return <code>true</code> if the property was updated; <code>false</code> if
         *         the property could not be found.
         */
        public boolean cachePropertyValue(final IProperty p) {
            if (cachedInputObjectPropertyValues != null
                    && cachedInputObjectPropertyValues.containsKey(p)) {
                cachedInputObjectPropertyValues.put(p, new CachedPropertyInfo(p));
                return true;
            } else {
                return false;
            }
        }

        /**
         * Caches the all the properties of the given input object.
         * <p>
         * Should be called whenever a new object is being inspected by the properties
         * view. Caches the initial values of the properties of the input object and
         * clears the properties to be hilighted.
         * 
         * @param input the input object
         */
        public void doCacheInputPropertyValues(final Object input) {
            cachedInputObject = input;
            final List<IProperty> properties = getPropertyList(input);
            cachedInputObjectPropertyValues = new HashMap<IProperty, CachedPropertyInfo>();

            for (IProperty p : properties) {
                cachedInputObjectPropertyValues.put(p, new CachedPropertyInfo(p));
            }

            hilightedProperties = null;
        }

        /**
         * Obtains the properties that have changed.
         * <p>
         * This list is computed based on a cache of property values.
         * 
         * @param input the input
         * @return the list of properties that have changed since the object was last
         *         cached; <code>null</code> if the input object is not the object that is
         *         cached.
         */
        public List<IProperty> getChangedProperties(final Object input) {
            if (input == cachedInputObject) {
                /*
                 * Check which properties of the new input have a different text
                 * from what we have previously stored.
                 */
                final List<IProperty> properties = getPropertyList(input);
                final List<IProperty> result = new ArrayList<IProperty>();
                for (int i = 0; i < properties.size(); i++) {
                    final IProperty p = properties.get(i);
                    if (p != null && cachedInputObjectPropertyValues != null) {
                        final CachedPropertyInfo cachedProperty = cachedInputObjectPropertyValues
                                .get(p);
                        if (cachedProperty != null && cachedProperty.hasChanged(p)) {
                            result.add(p);
                        }
                    }
                }
                return result;
            } else {
                return null;
            }
        }

        /**
         * Get the set of changed properties.
         * 
         * @return the set of properties currently set to be colored or <code>null</code>
         *         if no properties have been set to be colored.
         */
        public Set<IProperty> getHilightedProperties() {
            return hilightedProperties;
        }

        /**
         * Obtains the list of properties for a given model item.
         * 
         * @param item the model item.
         * @return a list with properties.
         */
        private List<IProperty> getPropertyList(final Object item) {
            final List<IProperty> result = new ArrayList<IProperty>();
            if (item instanceof Device) {
                final Device device = (Device) item;
                result.addAll(Arrays.asList(device.getPropertyManager().getProperties()));
                final DeviceStatus status = device.getStatus();
                final IDeviceDriver driver = device.getDriver();
                result.addAll(Arrays.asList(status.getPropertyManager().getProperties()));
                result.addAll(Arrays.asList(driver.getPropertyManager().getProperties()));
            } else if (item instanceof BaseNamedModelItem) {
                final BaseNamedModelItem i = (BaseNamedModelItem) item;
                result.addAll(Arrays.asList(i.getPropertyManager().getProperties()));
            }
            return result;
        }

        /**
         * Sets the properties to be background colored.
         * 
         * @param properties the list of properties that has changed, or <code>null</code>
         *            to reset.
         */
        public void setHilightedProperties(final List<IProperty> properties) {
            if (properties == null) {
                hilightedProperties = null;
            } else {
                hilightedProperties = new HashSet<IProperty>();
                hilightedProperties.addAll(properties);
            }
        }
    }

    /**
     * Label provider.
     */
    private class PropertyLabelProvider
            implements ITableLabelProvider, ITableColorProvider {

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

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.
         * lang.Object, int)
         */
        public Color getBackground(Object element, int columnIndex) {
            final Set<IProperty> changedProperties = propertyChangeManager.getHilightedProperties();
            if (changedProperties == null) {
                return null;
            } else {
                if (columnIndex == 1 && element instanceof IProperty
                        && changedProperties.contains(element)) {
                    propertyChangeManager.cachePropertyValue((IProperty) element);
                    return UIConstants.getChangedColor();
                } else {
                    return null;
                }
            }
        }

        /**
         * Gets the image for the specified column.
         * <p>
         * Does nothing.
         * 
         * @param arg0 the arg0
         * @param arg1 the arg1
         * @return the column image
         */
        public org.eclipse.swt.graphics.Image getColumnImage(Object arg0, int arg1) {
            return null;
        }

        /**
         * Gets the text for the specified column.
         * 
         * @param element the player
         * @param col the column
         * @return String
         */
        public String getColumnText(Object element, int col) {
            if (element instanceof IProperty) {
                final IProperty p = (IProperty) element;
                if (col == 0) {
                    return p.getName();
                } else if (col == 1) {
                    return p.toString();
                } else {
                    return ""; //$NON-NLS-1$
                }
            } else if (col == 0 && element instanceof DeviceStatus) {
                return DeviceStatus.NAME;
            } else if (col == 0 && element instanceof IDeviceDriver) {
                final IDeviceDriver p = (IDeviceDriver) element;
                final Device d = p.getParentDevice();
                return d.getDeviceInterface().getGatewayConnectionName();
            } else {
                return ""; //$NON-NLS-1$
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.
         * lang.Object, int)
         */
        public Color getForeground(Object element, int columnIndex) {
            if (element instanceof IProperty) {
                final IProperty p = (IProperty) element;
                if (p.isReadOnly()) {
                    return UIConstants.getReadOnlyColor();
                }
            }
            return null;
        }

        /**
         * Checks whether the specified property affects the label if changed.
         * 
         * @param arg0 the player
         * @param arg1 the property
         * @return boolean
         */
        public boolean isLabelProperty(Object arg0, String arg1) {
            return false;
        }

        /**
         * Removes the specified tableRowPropertyTooltip.
         * 
         * @param arg0 the tableRowPropertyTooltip
         */
        public void removeListener(ILabelProviderListener arg0) {
            /* do nothing */
        }
    }

    /**
     * Provides the content for the TableTreeViewer.
     * <p>
     * Tacks the properties whose text has changed.
     */
    private class PropertyTreeContentProvider
            implements ITreeContentProvider {

        /**
         * Disposes any resources.
         */
        @Override
        public void dispose() {
            /*
             * We don't create any resources, so we don't dispose any.
             */
        }

        /**
         * Gets all the children of a {@link ModelItem}.
         * 
         * @param element a named model item
         * @return Object[] containing the properties to be inspected.
         */
        private Object[] getAllChildren(final Object element) {
            if (element instanceof Device) {
                final Device device = (Device) element;
                final IPropertySet pm = device.getPropertyManager();
                final IProperty[] properties = pm.getProperties();

                final List<IProperty> allProperties = new ArrayList<IProperty>();

                Collections.addAll(allProperties, properties);

                Collections.addAll(allProperties, device.getStatus().getPropertyManager()
                        .getProperties());

                Collections.addAll(allProperties, device.getDriver().getPropertyManager()
                        .getProperties());

                return allProperties.toArray(new IProperty[0]);
            } else if (element instanceof ModelItem) {
                final ModelItem item = (ModelItem) element;
                return item.getPropertyManager().getProperties();
            } else {
                return new Object[0];
            }
        }

        /**
         * Gets the children.
         * 
         * @param element the team or player
         * @return Object[]
         */
        @Override
        public Object[] getChildren(final Object element) {
            if (PropertiesView.this.isAlphaOrdered()) {
                codebase.Debug.dump(getAllChildren(element));
                return getAllChildren(element);
            } else {
                return getChildrenStrict(element);
            }
        }


        /**
         * Gets the children of an element in the treeTable.
         * 
         * @param element a property manager, a status or a driver
         * @return the child elements or properties
         */
        private Object[] getChildrenStrict(final Object element) {
            // XXX: must be refactored to accomodate property groups
            if (element instanceof Device) {
                final Device device = (Device) element;
                final IPropertySet pm = device.getPropertyManager();
                final IProperty[] properties = pm.getProperties();

                final int totalLength = properties.length + 2;
                final Object[] result = new Object[totalLength];
                for (int i = 0; i < totalLength - 2; i++) {
                    result[i] = properties[i];
                }
                result[totalLength - 2] = device.getStatus();
                result[totalLength - 1] = device.getDriver();

                return result;
            } else if (element instanceof DeviceStatus) {
                final DeviceStatus s = (DeviceStatus) element;
                return s.getPropertyManager().getProperties();
            } else if (element instanceof IDeviceDriver) {
                final IDeviceDriver p = (IDeviceDriver) element;
                return p.getPropertyManager().getProperties();
            } else if (element instanceof BaseNamedModelItem) {
                final BaseNamedModelItem item = (BaseNamedModelItem) element;
                return item.getPropertyManager().getProperties();
            } else {
                return new Object[0];
            }
        }

        /**
         * Gets the elements for the table.
         * 
         * @param item the model
         * @return Object[]
         */
        @Override
        public Object[] getElements(Object item) {
            return getChildren(item);
        }

        /**
         * Returns the parent.
         * 
         * @param arg0 object
         * @return null
         */
        @Override
        public Object getParent(Object arg0) {
            return null;
        }



        /**
         * Gets whether this team or player has children.
         * 
         * @param arg0 the team or player
         * @return boolean
         */
        @Override
        public boolean hasChildren(Object arg0) {
            return getChildren(arg0).length > 0;
        }

        /**
         * Called when the item changes.
         * 
         * @param viewer the parent viewer
         * @param oldInput the old input
         * @param newInput the new input
         */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            propertyChangeManager.doCacheInputPropertyValues(newInput);
        }
    }

    /**
     * Implements the filtering of properties.F
     */
    private static class PropertyTreeViewerFilter extends
            ViewerFilter {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
         * .Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (element instanceof IProperty) {
                final IProperty p = (IProperty) element;
                // XXX: rever isto:
                final boolean mustFilter = !true; // old p.isVisible().F
                return !mustFilter;
            } else {
                return true;
            }
        }
    }

    /**
     * Implements the properties sorting.
     */
    private class PropertyTreeViewerSorter extends
            ViewerSorter {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface
         * .viewers .Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(final Viewer viewer, final Object e1, final Object e2) {
            if (e1 instanceof IProperty && e2 instanceof IProperty && useItemOrdering) {
                final IProperty p1 = (IProperty) e1;
                final IProperty p2 = (IProperty) e2;
                return p1.getName().compareToIgnoreCase(p2.getName());
            } else {
                return 0;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.
         * lang.Object, java.lang.String)
         */
        @Override
        public boolean isSorterProperty(Object element, String property) {
            if (PropertiesView.this.isAlphaOrdered()) {
                return super.isSorterProperty(element, property);
            } else {
                return false;
            }
        }
    }

    /**
     * Properties view identifier.
     */
    public static final String ID = "lumina.views.properties"; //$NON-NLS-1$

    /**
     * The tag that corresponds to the status of the info toggle to be saved.
     */
    protected static final String TAG_TOGGLE_ALPHA_ORDERING_ENABLED = "toggleAlphaEnabled";

    /**
     * The default width for the name column of the grid.
     */
    private static final int PROPERTY_NAME_COLUMN_DEFAULT_WIDTH = 120;

    /**
     * Size in pixels of the name column. Its size must be enough in order for property
     * names to be readable.
     */
    private static final int NAME_COLUMN_WIDTH = 350;

    /**
     * The Constant PROPERTY_COLUMN_NAME.
     */
    private static final String PROPERTY_COLUMN_NAME = Messages
            .getString("PropertiesView.property"); //$NON-NLS-1$

    /**
     * The Constant VALUE_COLUMN_NAME.
     */
    private static final String VALUE_COLUMN_NAME = Messages.getString("PropertiesView.value"); //$NON-NLS-1$

    /**
     * The Constant NO_VALID_SELECTION.
     */
    private static final String NO_VALID_SELECTION = Messages
            .getString("PropertiesView.noValidSelection"); //$NON-NLS-1$

    /**
     * The Constant MULTIPLE_ITEMS_SELECTED.
     */
    private static final String MULTIPLE_ITEMS_SELECTED = Messages
            .getString("PropertiesView.multipleItemsSelected"); //$NON-NLS-1$

    /**
     * Memento object used to set and get the preferences of the view. Used for querying
     * preferences when creating the controls. Can be <code>null</code> if the preference
     * store is deleted.
     */
    private IMemento viewPrefrenceMemento;

    /**
     * The {@link TreeViewer} objects that renders the table. Used to show grouped
     * properties.
     */
    private TreeViewer treeTableViewer;

    /**
     * The content provider of the tree that presents the properties. Initialized each
     * time the viewer is recreated. Made global to allow determine precisely which
     * properties have changed and need to be refreshed when Property change event occurs.
     */
    private PropertyTreeContentProvider propertyTreeContentProvider;

    /**
     * The label provider of the tree that presents the properties. Initialized each time
     * the viewer is recreated. Made global to allow coloring the background of the
     * properties that have changed.
     */
    private PropertyLabelProvider propertyLabelProvider;

    /**
     * Editing support object of the treeTable viewer.
     */
    private PropertiesEditingSupport edditingSupport;

    /**
     * The tree that contains the items.
     */
    private Tree treeTable;

    /**
     * The top composite specified by createPartControl used for the lazy creation of
     * other widgets.
     */
    private Composite parentComposite;

    /**
     * Current selection.
     */
    private IStructuredSelection selection;

    /**
     * The object under inspection. Assumes the value <code>null</code> if no valid
     * selection was give. For the moment we only allow one object.
     */
    private Object objectUnderInspection;

    /**
     * The property currently being edited. Assumes the value <code>null</code> if no
     * valid selection was given. Changes when the user selects a new row in the viewer.
     * Used to track which row is being edited.
     */
    private IProperty propertyUnderInspection;

    /**
     * The info toggle button that appears in the upper right corner of the view.
     */
    private CommandContributionItem alphaToggleButton;

    /**
     * Flag that indicates whether the property items should appear ordered by name or
     * logically ordered.
     */
    private boolean useItemOrdering;;

    /**
     * The property change manager.
     */
    private final PropertyChangeManager propertyChangeManager = new PropertyChangeManager();

    /**
     * Displays the tooltips of the properties. Note: This is a hack to an Eclipse bug.
     * Although Eclipse 3.3 introduced {@link import
     * org.eclipse.jface.viewers.CellLabelProvider} we have to do this with a
     * tableRowPropertyTooltip as in Eclipse 3.2 because if a {@link CellLabelProvider}
     * implements {@link ITableLabelProvider} the tooltips are not shown.
     */
    private AbstractTreeItemTooltip tableRowPropertyTooltip = new AbstractTreeItemTooltip() {
        @Override
        protected String getTooltipText(Object data) {
            if (data instanceof IProperty) {
                final IProperty p = (IProperty) data;
                return p.getDescription();
            }
            return null;
        }
    };

    /**
     * Creates the part control.
     * 
     * @param parent parent component
     */
    @Override
    public final void createPartControl(final Composite parent) {
        parentComposite = parent;

        // Undo and Redo actions
        final UndoActionHandler undoAction = new UndoActionHandler(getSite(),
                IOperationHistory.GLOBAL_UNDO_CONTEXT);
        final RedoActionHandler redoAction = new RedoActionHandler(getSite(),
                IOperationHistory.GLOBAL_UNDO_CONTEXT);

        getViewSite().getActionBars()
                .setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);

        getViewSite().getActionBars()
                .setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);

        // Create a label for the view to indicate no item is selected
        ViewUtils.createLabelPane(parentComposite, NO_VALID_SELECTION);

        /*
         * register the property change listeners of the model
         */
        ProjectModel.getInstance().addPropertyChangeListener(this);

        /*
         * save the alpha mode
         */
        if (viewPrefrenceMemento != null) {
            final String setAlphaOrdering = viewPrefrenceMemento
                    .getString(TAG_TOGGLE_ALPHA_ORDERING_ENABLED);
            if (setAlphaOrdering != null) {
                setAlphaOrdering(Boolean.parseBoolean(setAlphaOrdering));
            }
        }
    }

    /**
     * Terminate.
     */
    @Override
    public final void dispose() {
        tableRowPropertyTooltip.unregister(treeTable);
        ProjectModel.getInstance().addPropertyChangeListener(this);

        super.dispose();
    }

    /**
     * Clears the hilighting of properties.
     * 
     * @param save a list of properties to be saved from clearing, can be
     *            <code>null</code>
     */
    private void doClearHilight(final List<IProperty> save) {
        final Set<IProperty> propertiesToTurnOffHilight = propertyChangeManager
                .getHilightedProperties();
        if (propertiesToTurnOffHilight != null) {
            // We are not clearing the properties that are to be saved.
            // For example, because they are going to get selected again
            if (save != null) {
                propertiesToTurnOffHilight.removeAll(save);
            }

            propertyChangeManager.setHilightedProperties(null);
            for (IProperty p : propertiesToTurnOffHilight) {
                doRefreshTreeItemInternal(p);
            }
        }
    }

    /**
     * Creates the viewer grid on the top composite.
     * 
     * @param top the composite that will hold the grid.
     */
    private void doRecreateViewer(final Composite top) {
        top.setLayout(new FillLayout());

        treeTableViewer = new TreeViewer(top, SWT.BORDER | SWT.FULL_SELECTION);
        treeTableViewer.setAutoExpandLevel(2);
        ColumnViewerToolTipSupport.enableFor(treeTableViewer, ToolTip.NO_RECREATE);

        treeTable = treeTableViewer.getTree();
        treeTable.setHeaderVisible(true);
        treeTable.setBackgroundMode(SWT.INHERIT_FORCE);
        treeTable.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        treeTable.setLinesVisible(true);

        final TreeViewerColumn propertyColumn = new TreeViewerColumn(treeTableViewer, SWT.NONE);
        propertyColumn.getColumn().setWidth(PROPERTY_NAME_COLUMN_DEFAULT_WIDTH);
        propertyColumn.getColumn().setText(PROPERTY_COLUMN_NAME);

        final TreeViewerColumn valueColumn = new TreeViewerColumn(treeTableViewer, SWT.NONE);
        valueColumn.getColumn().setWidth(NAME_COLUMN_WIDTH);
        valueColumn.getColumn().setText(VALUE_COLUMN_NAME);

        propertyTreeContentProvider = new PropertyTreeContentProvider();
        treeTableViewer.setContentProvider(propertyTreeContentProvider);

        tableRowPropertyTooltip.register(treeTable);

        propertyLabelProvider = new PropertyLabelProvider();
        treeTableViewer.setLabelProvider(propertyLabelProvider);

        treeTableViewer
                .setColumnProperties(new String[] { PROPERTY_COLUMN_NAME, VALUE_COLUMN_NAME });

        treeTableViewer.setFilters(new ViewerFilter[] { new PropertyTreeViewerFilter() });
        treeTableViewer.setSorter(new PropertyTreeViewerSorter());
        edditingSupport = new PropertiesEditingSupport(treeTableViewer);
        valueColumn.setEditingSupport(edditingSupport);

        top.getShell().layout(false, true);
    }

    /**
     * Refreshes the treeTable.
     * <p>
     * This operation is carried on only if the treeTable is assigned and not disposed.
     */
    private void doRefreshInternal() {
        if (treeTableViewer != null && !treeTableViewer.getTree().isDisposed()) {

            /*
             * If we are in editing mode we do not update the view. This
             * prevents that status updates triggered by the network cancel the
             * current edit operation by refreshing the PropertiesView.
             * 
             * Note that when the cell editor applies its value the update will
             * take place automatically.
             */
            final boolean canCheckEditingSupport = edditingSupport != null;
            if (canCheckEditingSupport) {
                if (!edditingSupport.isEditing()) {
                    // refresh
                    treeTableViewer.refresh(true);
                    treeTableViewer.expandAll();
                }
            } else {
                // refresh
                treeTableViewer.refresh(true);
                treeTableViewer.expandAll();
            }
        }
    }

    /**
     * Refreshes a tree item.
     * <p>
     * This operation is carried on only if the treeTable is assigned and not disposed.
     * 
     * @param property the property
     */
    private void doRefreshTreeItemInternal(final IProperty property) {
        if (property != null && treeTableViewer != null && !treeTableViewer.getTree().isDisposed()) {
            if (property != propertyUnderInspection) {
                treeTableViewer.refresh(property, true);
            } else {
                /*
                 * If we are in editing mode we do not update the cell being
                 * updated.
                 * 
                 * This prevents that status updates triggered by the network
                 * cancel the current edit operation in progress
                 */
                if (edditingSupport != null) {
                    if (!edditingSupport.isEditing()) {
                        treeTableViewer.refresh(property, true);
                    }
                } else {
                    treeTableViewer.refresh(property, true);
                }
            }
        }
    }

    /**
     * Checks if the current object can be shown in the properties view.
     * 
     * @param object the object selected
     * @return <code>true</code> if the object is valid model item and <code>false</code>
     *         otherwise.
     */
    private boolean hasProperties(final Object object) {
        return ModelUtils.isModelItem(object);
    }

    /**
     * Initialize the view and keep the memento refrence.
     * 
     * @param site view site
     * @param memento the memento to read the state form
     * @throws PartInitException the in the same conditions as the parent class
     * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite,
     *      org.eclipse.ui.IMemento)
     */
    @Override
    public final void init(final IViewSite site, final IMemento memento) throws PartInitException {
        super.init(site, memento);
        viewPrefrenceMemento = memento;
    }

    /**
     * Checks if it's alphabetically ordered.
     * 
     * @return alphabetically ordered status
     */
    public final boolean isAlphaOrdered() {
        return useItemOrdering;
    }

    /**
     * Checks if the current selection refers to multiple objects.
     * 
     * @param incoming the current selection
     * @return true if the selection is a multiple selection
     */
    private boolean isMultipleSelection(final ISelection incoming) {
        if (incoming instanceof IStructuredSelection) {
            final IStructuredSelection s = (IStructuredSelection) incoming;
            return s.size() > 1;
        } else {
            return false;
        }
    }

    /**
     * Checks if the current selection is a valid item to be displayed.
     * 
     * @param incoming a selection
     * @return <code>true</code> if the first item is a {@link ModelItem}
     */
    private boolean isValidItem(final ISelection incoming) {
        if (incoming instanceof IStructuredSelection) {
            final IStructuredSelection s = (IStructuredSelection) incoming;
            final Object obj = s.getFirstElement();

            final boolean isValid = obj != null && hasProperties(obj);
            return isValid;
        } else {
            return false;
        }
    }

    /**
     * Reacts to property change events.<br/>
     * Called every time a network message results in a status change.
     * 
     * @param event property change event
     */
    public final void propertyChange(final PropertyChangeEvent event) {
        final String changedProperty = event.getProperty();

        /*
         * /!\ This condition prevents a nasty bug. If refresh internal is
         * called, the cell editor listeners are removed and the cell editor
         * will not notify the cell edit support to
         * PropertiesEditingSupport.setValue() will not be called.
         */
        if (!PropertyChangeNames.isPreview(changedProperty)) {
            /*
             * A project change is a special case.
             * 
             * It does not matter if we are in editing mode, if a project change
             * occurs, we have to update.
             */
            if (PropertyChangeNames.isProjectLoading(changedProperty)) {
                final Object projectSubject = event.getSource();
                if (objectUnderInspection != null && projectSubject != objectUnderInspection) {
                    /*
                     * Reset the selection because, if we changed project, the
                     * current selection is meaningless.
                     */
                    selection = null;
                    objectUnderInspection = null;
                    propertyUnderInspection = null;
                    treeTableViewer.setInput(objectUnderInspection);

                    doRefreshInternal();
                }
            } else {
                /*
                 * When a status change or data change comes, only update if the
                 * event subject is the object being inspected. This prevents
                 * superfluous refresh operations and
                 */
                final boolean isDataChange = PropertyChangeNames.isMetadataChange(changedProperty)
                        || PropertyChangeNames.isStatusChange(changedProperty);

                if (isDataChange) {
                    final Object subject = event.getSource();
                    final boolean isUpdate = objectUnderInspection != null
                            && subject == objectUnderInspection;
                    if (isUpdate) {
                        final List<IProperty> changedProperties = propertyChangeManager
                                .getChangedProperties(subject);
                        // CHECKSTYLE:OFF
                        // FIXME: The implementation of the property change
                        // mechanism is overly complex. We should simplify this
                        // code.

                        if (changedProperties == null) {
                            /*
                             * _ /!\_ In general, this should never happen:
                             * changed properties should always be assigned.
                             * 
                             * We are paranoic and we play safe: let's try to
                             * determine the changed properties anyway.
                             */
                            if (event instanceof ModelItemPropertyChangeEvent) {
                                final ModelItemPropertyChangeEvent propertyChange = (ModelItemPropertyChangeEvent) event;

                                final IProperty[] props = propertyChange.getChangedProperties();
                                propertyChangeManager.setHilightedProperties(Arrays.asList(props));
                                for (IProperty p : props) {
                                    doRefreshTreeItemInternal(p);
                                }
                            } else {
                                doRefreshInternal();
                            }
                            // CHECKSTYLE:ON
                        } else {
                            /*
                             * Lets update the changed properties here.
                             */
                            doClearHilight(changedProperties);
                            propertyChangeManager.setHilightedProperties(changedProperties);
                            for (IProperty p : changedProperties) {
                                doRefreshTreeItemInternal(p);
                            }
                        }
                    } else {
                        /*
                         * If its not an update, then a new object must have
                         * been set for inspection. In that case, refresh
                         * everything.
                         */
                        doRefreshInternal();
                    }
                } else {
                    /*
                     * If its not a data change, what is it? Lets play safe and
                     * refresh everything.
                     */
                    doRefreshInternal();
                }
            }
        }
    }

    /**
     * Saves the state.
     * 
     * @param memento memento
     */
    @Override
    public final void saveState(final IMemento memento) {
        super.saveState(memento);
        final Boolean isEnabled = isAlphaOrdered();
        memento.putString(TAG_TOGGLE_ALPHA_ORDERING_ENABLED, isEnabled.toString());
    }

    /**
     * Reacts to selection change events.
     * 
     * @param part workbench part
     * @param incoming selection
     */
    public final void selectionChanged(final IWorkbenchPart part, final ISelection incoming) {
        if (incoming instanceof IStructuredSelection) {
            final boolean panelIsActive = isMultipleSelection(selection) || !isValidItem(selection);

            if (selection == null || !incoming.equals(selection)) {
                selection = (IStructuredSelection) incoming;

                objectUnderInspection = null;
                propertyUnderInspection = null;

                if (isMultipleSelection(selection)) {
                    ViewUtils.disposeExistingControls(parentComposite);
                    ViewUtils.createLabelPane(parentComposite, MULTIPLE_ITEMS_SELECTED);
                } else {
                    if (isValidItem(selection)) {
                        assert selection.size() == 1;

                        objectUnderInspection = selection.getFirstElement();

                        // TODO: DELETE
                        if (objectUnderInspection instanceof Device) {
                            Device d = (Device) objectUnderInspection;
                            Map<IGateway, IDeviceDriver> x = d.deviceDrivers;

                            Logger.getInstance().log(LogService.LOG_ERROR,"A device was selected {");
                            for (IGateway i : x.keySet()) {
                                IDeviceDriver dd = x.get(i);
                                Logger.getInstance().log(LogService.LOG_ERROR,"::: " + d.getName() + ":"
                                        + i.getGatewayConnectionName() + " -> " + dd.getName());
                            }
                            Logger.getInstance().log(LogService.LOG_ERROR,"}");
                        }

                        if (panelIsActive) {
                            ViewUtils.disposeExistingControls(parentComposite);
                            doRecreateViewer(parentComposite);
                        }

                        /*
                         * set the input
                         */
                        treeTableViewer.setInput(objectUnderInspection);

                        doRefreshInternal();
                    } else {
                        ViewUtils.disposeExistingControls(parentComposite);
                        ViewUtils.createLabelPane(parentComposite, NO_VALID_SELECTION);
                    }
                }
            }
        }
    }

    /**
     * Sets the ordering of items in the treeTable to true/false. <br/>
     * When this property is changed, a property change event is propagated.
     * 
     * @param mustOrder a boolean values indicating whether the items should appear
     *            ordered.
     */
    public final void setAlphaOrdering(final boolean mustOrder) {
        this.useItemOrdering = mustOrder;

        this.firePropertyChange(0);
        doRefreshInternal();
    }

    /**
     * Passing the focus request to the treeViewer's control.
     */
    @Override
    public void setFocus() {
        if (treeTableViewer != null && treeTableViewer.getControl() != null
                && !treeTableViewer.getControl().isDisposed()) {
            treeTableViewer.getControl().setFocus();
        }

        /*
         * We have to create this object in here and take it out of the
         * plugin.xml file because of an Eclipse bug where the button would be
         * rendered twice.
         */
        if (alphaToggleButton == null) {
            alphaToggleButton = new CommandContributionItem(new CommandContributionItemParameter(
                    getViewSite(), "lumina.menus.toggleAlphaSort",
                    "lumina.properties.commands.toggleAlphaSort",
                    CommandContributionItem.STYLE_CHECK));

            if (alphaToggleButton != null) {
                final IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
                manager.add(alphaToggleButton);
                manager.update(true);
            }
        }
    }
}
