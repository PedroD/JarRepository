package lumina.ui.views.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.Queries;
import lumina.base.model.transfer.DeviceTransfer;
import lumina.ui.jface.SelectionUtils;
import lumina.ui.swt.ApplicationImageCache;
import lumina.ui.swt.SWTImageCanvas;
import lumina.ui.swt.SWTUtils;
import lumina.ui.views.blueprint.actions.AddToBluePrintHandler;
import lumina.ui.views.blueprint.actions.ZoomLevelCombo.Zoomable;
import lumina.ui.views.blueprint.figures.BaseFloorFigure;
import lumina.ui.views.blueprint.figures.BaseFloorFigure.ImageReadyListener;
import lumina.ui.views.blueprint.figures.FloorFigure;
import lumina.ui.views.navigation.NavigationView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * A {@link ViewPart View} that displays the floor plan.
 * <p>
 * When the user is selecting the floor plan this view is also responsible for previewing
 * the floor image.
 */
public class BlueprintView extends
        ViewPart
        implements ISelectionListener, IPropertyChangeListener, Zoomable, ImageReadyListener {

    /**
     * ID of the blueprint view.
     */
    public static final String ID = "lumina.views.blueprint"; //$NON-NLS-1$

    /**
     * Predefined zoom steps, in '%'.
     */
    public static final int[] ZOOM_STEPS = { 25, 50, 100, 200, 300, 400, 500, 600, 800, 1000, 1200,
            1600, 2000, 2400, 2800, 3200, 4000, 4800, 5600, 6400, };

    /**
     * The delta below which we say that two zoom levels are the same.
     */
    private static final double ZOOM_COMPARE_ERROR = 0.001;

    /**
     * The context menu ID.
     */
    private static final String CONTEXT_MENU_ID = "#lumina.ui.views.blueprint.contextMenu"; //$NON-NLS-1$

    /**
     * Title of the plan tab when no floor is selected.
     * <p>
     * Implementation Note: This should point to
     * <code>Messages.getString("BlueprintView.NoFloor")</code> but it doesn't work. This
     * text mest be resolved when the framework is starting up, before Messages is
     * started.
     */
    private static final String NO_FLOOR_SELECTED_TITLE = "           ";

    /**
     * Switch used to create a native canvas. Ugly SWT hack.
     */
    private static final int NATIVE_CANVAS_SWITCH = 536870912;

    /**
     * Keeps the reference to the blueprint view.
     */
    private static BlueprintView bluePrintView;

    /**
     * Reference to the parent of the view.
     */
    private Composite top;

    /**
     * The canvas used to display images.
     */
    private SWTImageCanvas viewCanvas;

    /**
     * Context menu.
     */
    private Menu contextMenu;

    /**
     * Context menu manager.
     */
    private MenuManager menuMgr;

    /**
     * The zoom level combo box the appears on the top right corner of the view.
     */
    private final ZoomComboContribution zoomComboContribution = new ZoomComboContribution();

    /**
     * Associates {@link Floor} objects to {@link FloorFigure}.
     */
    private final Map<Floor, FloorFigure> floorFigures = new HashMap<Floor, FloorFigure>();

    /**
     * Cached reference to the navigation view used to obtain the selection being dragged.
     */
    private NavigationView navigationView;

    /**
     * Holds the currently selected floor.
     */
    private Floor currentFloor;

    /**
     * Holds the current floor figure.
     */
    private FloorFigure currentFloorFigure;

    /**
     * Holds the resolution of the last rendered floor. This necessary when previewing
     * floors.
     */
    private Point referenceResolution;

    /**
     * Holds the current selection.
     */
    private IStructuredSelection selection;

    /**
     * Holds the last selection found in {
     * {@link #selectionChanged(IWorkbenchPart, ISelection)} method. This is necessary to
     * reselect the devices figures when a new image is rendered.
     */
    private Device[] lastDevicesSelected;

    /**
     * Finds the BluePrint view.
     * 
     * @return a reference to the blueprint view or <code>null</code> if the view could
     *         not be found.
     */
    public static final BlueprintView findBlueprintView() {
        if (bluePrintView != null) {
            return bluePrintView;
        } else {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            if (workbench == null) {
                return null;
            }

            final IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
            if (workbenchWindow == null) {
                return null;
            }

            final IWorkbenchPage page = workbenchWindow.getActivePage();
            if (page == null) {
                return null;
            }

            final IViewPart view = page.findView(BlueprintView.ID);
            if (view instanceof BlueprintView) {
                bluePrintView = (BlueprintView) view;
                return bluePrintView;
            }

            return null;
        }
    }

    /**
     * Add the devices of an area to the current floor.
     * 
     * @param areas the areas containing the devices
     */
    private void addVisibleDevicesToPlan(final Area[] areas) {
        for (Area area : areas) {
            final Device[] devices = area.getDevices();
            addVisibleDevicesToPlan(area, devices);
        }
    }

    /**
     * Add the devices of an area to the current floor.
     * 
     * @param area the area that contains the devices
     * @param devices a list of devices to be updated (added)
     */
    private void addVisibleDevicesToPlan(final Area area, final Device[] devices) {
        for (Device d : devices) {
            final Floor floor = Queries.getAncestorFloor(area);
            final FloorFigure figure = findFigure(floor);
            if (figure != null) {
                figure.updateDevice(d, null);
            }
        }
    }

    /**
     * Takes out the floor figure.
     */
    private void doUninstallFloorFigure() {
        if (currentFloorFigure != null) {
            currentFloorFigure.setVisible(false);

            floorFigures.remove(currentFloor);

            currentFloorFigure = null;
            currentFloor = null;
        }
    }

    /**
     * Re-install the floor figure
     * <p>
     * Sets the new floor figure as the selection provider.
     * 
     * @param floor the floor object
     * @param floorFigure the floor figure object
     * @param previewImageData the data to be displayed
     * @param isPreviewMode indicates if we are still in preview mode
     */
    private void doReinstallFloorFigure(final Floor floor,
                                        final FloorFigure floorFigure,
                                        final byte[] previewImageData,
                                        final boolean isPreviewMode) {

        if (currentFloorFigure != null) {
            // clear the currently visible figure
            currentFloorFigure.setVisible(false);

            currentFloorFigure.removeImageReadyListener(this);
            getSite().setSelectionProvider(null);

            currentFloorFigure = null;
        } else if (viewCanvas != null) {
            // special case: even if there is no floor figure we need to clear
            // the
            // canvas image directly, because it may be displaying something
            // (e.g.
            // the initial logo)
            viewCanvas.setImageData(null);
        }

        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                currentFloor = floor;
                currentFloorFigure = floorFigure;

                getSite().setSelectionProvider(currentFloorFigure);

                currentFloorFigure.addImageReadyListener(BlueprintView.this);

                if (!viewCanvas.isDisposed()) {
                    // Logger.getInstance().log(LogService.LOG_ERROR,"NOW STARINTING");
                    currentFloorFigure.setBackgroundColor(SWTUtils.swtColor(
                            viewCanvas.getDisplay(), currentFloor.getBackgroundColor()));

                    if (!currentFloorFigure.isWmfLoaded()) {
                        final byte[] imageData;
                        if (isPreviewMode) {
                            imageData = previewImageData;
                        } else {
                            imageData = ModelUtils.getFloorImageData(floor);
                        }

                        currentFloorFigure.setFigureData(imageData);
                    }

                    doSetReferenceScale(floor, currentFloorFigure);
                    currentFloorFigure.setVisible(true);
                }
            }
        });
    }

    @Override
    public final void dispose() {
        if (contextMenu != null) {
            contextMenu.dispose();
        }

        if (menuMgr != null) {
            menuMgr.dispose();
        }

        super.dispose();
    }

    /**
     * Displays the figure corresponding to a floor.
     * 
     * @param floor the floor object
     * @param floorFigure the figure to be displayed that corresponds to the floor
     */
    private void doDisplayFloorFigure(final Floor floor, final FloorFigure floorFigure) {
        if (floor == currentFloor) {
            if (floor != null && !currentFloorFigure.isVisible()) {
                currentFloorFigure.setVisible(true);
            }
        } else {
            doReinstallFloorFigure(floor, floorFigure, null, false);
        }
    }

    private static void doSetReferenceScale(final Floor floor, final FloorFigure figure) {
        final int referenceZoomPercent = floor.getDefaultZoom();
        final double referenceScale = referenceZoomPercent/100.0;
        if (referenceScale > 0.0 && figure != null) {
            if (figure.getReferenceScale() != referenceScale) {
                figure.setReferenceScale(referenceScale);
            }
        }
    }

    /**
     * Updates the contributions of the view's tool bar.
     * 
     * @param scale the scale the zoom ratio
     * @param enabled a flag that indicates whether or not the contributions should appear
     *            enabled
     */
    private void doUpdateContributions(final double scale, final boolean enabled) {
        if (zoomComboContribution != null) {
            zoomComboContribution.setZoomClient(this);
            zoomComboContribution.updateZoom(scale);
            zoomComboContribution.setEnabled(enabled);
        }
    }

    /**
     * Reselects the items, thus triggering the re-evaluation of the property testers.
     * <p>
     * This will force, the update of the 'enabledness' of the zoom actions.
     */
    private void doReselect() {
        final IWorkbenchWindow window = this.getSite().getWorkbenchWindow();
        if (window != null) {
            final ISelectionService selectionService = window.getSelectionService();
            if (selectionService != null) {
                final ISelection newSelection = selectionService.getSelection();
                this.getSite().getSelectionProvider().setSelection(newSelection);
                SelectionUtils.doUpdateSelectionSourceProvider(window, newSelection);
            }

        }
    }

    /**
     * Finds the floor figure corresponding to a floor object.
     * <p>
     * The figure should have been created.
     * 
     * @param floor the floor
     * @return the corresponding floor figure
     */
    private FloorFigure findFigure(final Floor floor) {
        return floorFigures.get(floor);
    }

    /**
     * Gets the floor figure corresponding to a floor.
     * 
     * @param floor the floor
     * @return the existing floor figure or a newly created figure
     */
    private FloorFigure getFloorFigure(final Floor floor) {
        final FloorFigure existingFigure = floorFigures.get(floor);
        final FloorFigure returnFigure;
        if (existingFigure == null) {
            returnFigure = new FloorFigure(floor, viewCanvas);
            floorFigures.put(floor, returnFigure);
        } else {
            returnFigure = existingFigure;
        }

        assert returnFigure != null;

        return returnFigure;
    }

    /**
     * Computes the zoom level from a zoom level percent.
     * 
     * @param zoomPercent the zoom percent value
     * @return the zoom level where 1.0 = 100%.
     */
    private static double getZoomLevel(final int zoomPercent) {
        return zoomPercent / 100.0;
    }

    /**
     * Checks if two zoom levels are the same.
     * 
     * @param first the first zoom level
     * @param second the second zoom level
     * @return <code>true</code> if abs(first - second) < ZOOM_COMPARE_ERROR; returns
     *         <code>false</code> otherwise.
     */
    private static boolean sameZoomLevel(double first, double second) {
        return Math.abs(first - second) < ZOOM_COMPARE_ERROR;
    }

    /**
     * Removes the devices of the given areas from the current floor.
     * <p>
     * We only update the floor currently showing. The remaining floors don't need update,
     * they will be updated when their image is displayed.
     * <p>
     * We have to provide the floor explicitly since the area can be already detached from
     * its parent the parent floor.
     * 
     * @param floor the currently displaying floor
     * @param areas the ares being removed
     */
    private void removeVisibleAreasFromFloorPlan(final Floor floor, final Area[] areas) {
        /*
         * We iterate through the areas to find the floors.
         */
        for (Area area : areas) {
            final Device[] devices = area.getDevices();
            for (Device d : devices) {
                final FloorFigure figure = findFigure(floor);
                if (figure != null) {
                    figure.removeDevice(d);
                }
            }
        }
    }

    /**
     * Removes the devices from the current plan.
     * <p>
     * The areas have to be supplied since the devices can be already detached from their
     * parent areas.
     * 
     * @param areas the ares being removed
     * @param devices the devices to be removed
     */
    private void removeVisibleDevicesFromFloorPlan(final Area[] areas, final Device[] devices) {
        /*
         * We iterate through the areas to find the floors because the Device
         * object is already detached from the area when propertyChange occurs.
         */
        for (Area area : areas) {
            for (Device d : devices) {
                final Floor floor = Queries.getAncestorFloor(area);
                final FloorFigure figure = findFigure(floor);
                if (figure != null) {
                    figure.removeDevice(d);
                }
            }
        }
    }

    /**
     * Sets the title of the plan in the plan tab.
     * 
     * @param name the name of the current floor on <code>null</code> if no floor is
     *            selected.
     */
    private void setPlanTitleInternal(final String name) {
        if (name != null) {
            this.setPartName(name);
        } else {
            this.setPartName(NO_FLOOR_SELECTED_TITLE);
        }
    }

    /**
     * Gets the selection being dragged from the navigation view.
     * 
     * @return the selection being dragged from the navigation view; or <code>null</code>
     *         if nothing is being dragged from the navigation view.
     */
    protected final ISelection getDraggingSelection() {
        if (navigationView == null) {
            final IWorkbenchWindow workbenchWindow = this.getSite().getWorkbenchWindow();
            if (workbenchWindow != null) {
                final IWorkbenchPage page = workbenchWindow.getActivePage();
                if (page != null) {
                    navigationView = (NavigationView) page.findView(NavigationView.ID);
                }
            }
        }

        if (navigationView != null) {
            return navigationView.getDraggingSelection();
        } else {
            return null;
        }
    }

    /**
     * @return the current floor; or <code>null</code> if no floor is assigned.
     */
    protected final Floor getCurrentFloor() {
        return currentFloor;
    }

    /*
     * Public methods
     */

    /**
     * Add a control listener to the view's composite.
     * <p>
     * This method enables the view to perform notifications when resized. Implemented to
     * enable intercepting maximize and minimize events as well.
     * 
     * @param listener the listener
     */
    public final void addControlListener(final ControlListener listener) {
        if (top == null) {
            throw new IllegalStateException();
        }

        top.addControlListener(listener);
    }

    /**
     * Adds devices to the plan at the given location.
     * <p>
     * The devices are added to the current floor if the current floor in not
     * <code>null</code>. Gets the devices and then executes the
     * {@link AddToBluePrintHandler} action handler.
     * 
     * @param devices the devices to add
     * @param locations the locations where to drop the devices into, must be of the same
     *            size of the devices array
     */
    public final void dropDevicesAt(final Device[] devices, final Point[] locations) {
        assert devices != null;
        assert locations != null;
        assert devices.length == locations.length;

        if (currentFloor != null) {
            /*
             * /!\ Very Important: We have to ensure the right object
             * references. If the devices supplied are the result of a paste
             * operation, they are different instances from those we want to add
             * to the BluePrintView
             */
            final Device[] localDeviceInstances = getDevicesById(devices);
            final Point[] deviceLocations = new Point[devices.length];

            /*
             * Compute the canvas locations and the device locations
             */
            for (int i = 0; i < locations.length; i++) {
                final Point canvasLocation = viewCanvas.toControl(locations[i]);
                deviceLocations[i] = canvasLocation;

                /*
                 * Update the device locations
                 */
                final org.eclipse.draw2d.geometry.Point floorCoordinates = new org.eclipse.draw2d.geometry.Point(
                        canvasLocation.x, canvasLocation.y);
                final org.eclipse.draw2d.geometry.Point deviceCoordinates = currentFloorFigure
                        .getDeviceCoordinates(floorCoordinates);
                if (deviceCoordinates != null) {
                    localDeviceInstances[i].setXCoordinate(deviceCoordinates.x);
                    localDeviceInstances[i].setYCoordinate(deviceCoordinates.y);
                }
            }

            /*
             * Compute the device locations
             */

            final ISelectionProvider provider = this.getSite().getSelectionProvider();
            AddToBluePrintHandler.run(getSite().getWorkbenchWindow(), localDeviceInstances,
                    deviceLocations, provider);
        }
    }

    /**
     * Checks if the image can be centered in the ViewPort.
     * 
     * @return <code>true</code> if a floor figure is assigned.
     */
    public final boolean canCenterViewPort() {
        return (currentFloorFigure != null);
    }

    /**
     * Checks if an object can be dropped in this view at the specified coordinates.
     * <p>
     * We cannot drop an object out of the plant being displayed.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @return <code>true</code> if the object coordinates correspond to a visible part of
     *         a plan being displayed; returns <code>false</code> if the area where the
     *         drop operation is being performed is a "passepartout".
     */
    public final boolean canDropIn(final int x, final int y) {
        /*
         * Translate the shell absolute coordinates into the coordinates of the
         * blueprint view
         */
        final Point c = top.toControl(x, y);

        /*
         * Translates the coordinates of the blueprint view into coordinates of
         * the floor figure being displayed
         */
        final org.eclipse.draw2d.geometry.Point floorCoordinates = currentFloorFigure
                .getDeviceCoordinates(new org.eclipse.draw2d.geometry.Point(c.x, c.y));

        /*
         * Check if the point belongs to the floor figure
         */
        if (floorCoordinates.x >= 0 && floorCoordinates.y >= 0) {
            final Dimension d = currentFloorFigure.getSize();
            final boolean withinBounds = floorCoordinates.x < d.width
                    && floorCoordinates.y < d.height;

            return withinBounds;
        } else {
            return false;
        }
    }

    public final boolean canZoomIn() {
        return isImageReady();
    }

    public final boolean canZoomOut() {
        return isImageReady() && getZoomLevel() > currentFloorFigure.getZoomToFitLevel();
    }

    public final boolean canZoomToDefault() {
        if (currentFloor != null) {
            final boolean isAtDefaultZoom = sameZoomLevel(getZoomLevel(),
                    getZoomLevel(currentFloor.getDefaultZoom()));
            final boolean defaultZoomAssigned = currentFloor.getDefaultZoom() != 0;

            return defaultZoomAssigned && !isAtDefaultZoom && isImageReady();
        } else {
            return false;
        }

    }

    /**
     * Centers the the specified image point in the ViewPort.
     * 
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     */
    public final void centerViewport(final int x, final int y) {
        if (canCenterViewPort()) {
            currentFloorFigure.centerViewPort(x, y);
        }
    }

    /**
     * Call-back that creates the canvas object initializes the view.
     * 
     * @param parent the parent composite.
     */
    @Override
    public final void createPartControl(final Composite parent) {
        top = parent;

        viewCanvas = new SWTImageCanvas(top, SWT.NONE | NATIVE_CANVAS_SWITCH);

        final Color black = top.getDisplay().getSystemColor(SWT.COLOR_BLACK);
        setBackground(black);

        final Image image = ApplicationImageCache.getInstance().getImage("conf/default.jpg");

        if (image != null) {
            /*
             * XXX: A welcome image e a sua cor the background deveriam ser
             * configuraveis!
             */
            final Color white = top.getDisplay().getSystemColor(SWT.COLOR_BLACK);

            viewCanvas.setBackground(white);
            viewCanvas.setImageData(image.getImageData());
        }

        // doShowLabel(NO_FLOOR_SELECTED);
        setPlanTitleInternal(null);

        menuMgr = new MenuManager(CONTEXT_MENU_ID);

        // Create menu.
        contextMenu = menuMgr.createContextMenu(viewCanvas);

        // Every context menu must have this option (see issue #234)
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        viewCanvas.setMenu(contextMenu);

        /*
         * Logically we should register the zoom level combo after the
         * separator. However we will see an exception if we try to do it
         * because the item from the plugin.xml are not yet created when
         * createPartControl is run.
         */
        getViewSite().getActionBars().getToolBarManager().add(zoomComboContribution);

        // ////////////////////////////////////////

        // Register the menu extension.
        getSite().registerContextMenu(CONTEXT_MENU_ID, menuMgr, getSite().getSelectionProvider());

        /* registers the DnD support */
        final DropTarget dropTarget = new DropTarget(viewCanvas, DND.DROP_MOVE | DND.DROP_COPY
                | DND.DROP_DEFAULT);

        final Transfer[] transferTypes = new Transfer[] { DeviceTransfer.getInstance() };
        dropTarget.setTransfer(transferTypes);

        final PlanDropListener dropListener = new PlanDropListener(this);
        dropTarget.addDropListener(dropListener);

        /* registers the view as a selection listener. */
        getViewSite().getPage().addSelectionListener(this);

        /* register the property change listeners of the model */
        ProjectModel.getInstance().addPropertyChangeListener(this);

    }

    /**
     * @return the background color of the parent widget.
     */
    public final Color getBackground() {
        return top.getBackground();
    }

    /**
     * Checks if an image is rendered.
     * 
     * @return <code>true</code> if the canvas is displaying an image; <code>false</code>
     *         otherwise.
     */
    public final boolean isImageReady() {
        return currentFloor != null && currentFloorFigure != null
                && currentFloorFigure.isImageReady();
    }

    /**
     * Gets the devices by Id and ensures that they pertain to the current floor.
     * <p>
     * The devices supplied in the input may can be the result of a drop operation coming
     * form another instance of the application.
     * 
     * @param devices the devices to be filtered.
     * @return an array of devices of the current floor with the same id as those supplied
     *         in the input.
     */
    public final Device[] getDevicesById(final Device[] devices) {
        final Set<String> ids = new HashSet<String>();

        for (Device d : devices) {
            ids.add(d.getId());
        }

        final List<Device> result = new ArrayList<Device>();
        if (currentFloor != null) {
            final Device[] floorDevices = Queries.getAllDevices(currentFloor);
            for (Device d : floorDevices) {
                if (ids.contains(d.getId())) {
                    result.add(d);
                }
            }
        }

        return result.toArray(new Device[0]);
    }

    /**
     * Handles the image ready event sent by {@link BaseFloorFigure}.
     * <p>
     * Called when the background image becomes ready.
     * <p>
     * Implementation note: This method has an implict dependency with
     * {@link #selectionChanged(IWorkbenchPart, ISelection)} method, on the variable
     * {@link #lastDevicesSelected}.
     * 
     * @param figure the figure the floor figure object
     * @param canvas the canvas {@link #selectionChanged(IWorkbenchPart, ISelection)}
     *            method.
     */
    public final void imageReady(final BaseFloorFigure figure, final SWTImageCanvas canvas) {
        if (figure == currentFloorFigure) {
            doSetReferenceScale(currentFloor, currentFloorFigure);
            doUpdateContributions(currentFloorFigure.getZoomScale(),
                    currentFloorFigure.isImageReady());

            setPlanTitleInternal(currentFloor.getName());

            referenceResolution = figure.getCanvas().getImageResolution();

            if (figure instanceof FloorFigure) {
                final FloorFigure f = (FloorFigure) figure;
                if (lastDevicesSelected != null) {
                    f.selectDevices(lastDevicesSelected);
                }
            }
        }

        doReselect();
    }

    /**
     * Property change event handler.
     * 
     * @param event the property change event sent by the framework
     */
    // CHECKSTYLE:OFF
    // FIXME: This code is overly complex. We nnet to revise and simplify
    // this mechanism.
    public final void propertyChange(final PropertyChangeEvent event) {
        final Object subject = event.getSource();
        final String changedProperty = event.getProperty();

        if (PropertyChangeNames.isMetadataChange(changedProperty)) {
            assert ModelUtils.isModelItem(subject);

            if (changedProperty.equals(PropertyChangeNames.DEVICE_DATA_CHANGED)) {
                final Device device = (Device) subject;
                final Floor floor = Queries.getAncestorFloor(device);
                final FloorFigure figure = findFigure(floor);

                if (figure != null) {
                    final org.eclipse.draw2d.geometry.Point eventCoords;
                    if (event.getNewValue() instanceof Point) {
                        eventCoords = new org.eclipse.draw2d.geometry.Point(
                                (Point) event.getNewValue());
                    } else {
                        eventCoords = null;
                    }
                    figure.updateDevice(device, eventCoords);
                }
            }

            if (changedProperty.equals(PropertyChangeNames.FLOOR_DATA_CHANGED)
                    || changedProperty.equals(PropertyChangeNames.FLOOR_ADJUST_PREVIEW_CHANGED)
                    || changedProperty.equals(PropertyChangeNames.FLOOR_ADJUST_COORDS_CHANGED)
                    || changedProperty.equals(PropertyChangeNames.FLOOR_ADJUST_COORDS_ACCEPTED)) {
                if (subject instanceof Floor) {
                    final Floor floor = (Floor) subject;

                    final boolean isFloorDataChange = changedProperty
                            .equals(PropertyChangeNames.FLOOR_DATA_CHANGED);

                    final boolean isPreview = changedProperty
                            .equals(PropertyChangeNames.FLOOR_ADJUST_PREVIEW_CHANGED);

                    final boolean isCoordAdjustment = changedProperty
                            .equals(PropertyChangeNames.FLOOR_ADJUST_COORDS_CHANGED);

                    final boolean isFloorCoordsAccepted = changedProperty
                            .equals(PropertyChangeNames.FLOOR_ADJUST_COORDS_ACCEPTED);

                    final boolean isPreviewMode = isPreview || isCoordAdjustment
                            || isFloorCoordsAccepted;

                    final boolean mustUpdateTitle = isFloorDataChange;
                    if (mustUpdateTitle) {
                        setPlanTitleInternal(floor.getName());
                    }

                    final byte[] imageData;
                    if (isPreviewMode) {
                        /*
                         * Get the plan name from the event and query the
                         * project.
                         */
                        final String planName = (String) event.getNewValue();
                        final Project project = floor.getParentProject();
                        imageData = ModelUtils.getFloorImageData(project, planName);
                    } else {
                        /*
                         * Get the plan data directly using the floor
                         */
                        imageData = ModelUtils.getFloorImageData(floor);
                    }

                    final FloorFigure floorFigure = getFloorFigure(floor);
                    final boolean imageMustChangeForFloorUnderDisplay = floor == currentFloor;

                    if (floorFigure != null) {
                        if (isFloorCoordsAccepted) {
                            floorFigure.acceptPreviewResolution();
                        }

                        final boolean needsImageChange = floorFigure.getFigureData() != imageData;
                        if (needsImageChange) {
                            if (isFloorDataChange) {
                                referenceResolution = floorFigure.getCanvas().getImageResolution();
                            }

                            floorFigure.setVisible(false);
                            floorFigure.setFigureData(imageData);

                            if (floorFigure.getPreviewResolution() == null) {
                                floorFigure.setPreviewResolution(referenceResolution);
                            }
                        }

                        if (needsImageChange || isCoordAdjustment || isFloorCoordsAccepted) {
                            if (!isFloorCoordsAccepted) {
                                floorFigure.previewResolutionChange(ProjectModel.getInstance()
                                        .getAdjustDeviceCoordinates());
                            }

                            if (imageMustChangeForFloorUnderDisplay) {
                                doReinstallFloorFigure(floor, floorFigure, imageData, isPreview);
                            }
                        }

                        floorFigure.setBackgroundColor(SWTUtils.swtColor(viewCanvas.getDisplay(),
                                currentFloor.getBackgroundColor()));
                    }

                    if (imageMustChangeForFloorUnderDisplay) {
                        doSetReferenceScale(currentFloor, currentFloorFigure);

                        if (currentFloorFigure != null) {
                            doUpdateContributions(currentFloorFigure.getZoomScale(),
                                    currentFloorFigure.isImageReady());
                        } else {
                            doUpdateContributions(0.0, false);
                        }
                    }
                }

            }
        } else if (PropertyChangeNames.isAreaAdd(changedProperty)) {
            if (subject instanceof Floor) {
                if (event.getNewValue() instanceof Object[]) {
                    final Area[] areas = ModelUtils.toAreas((Object[]) event.getNewValue());
                    addVisibleDevicesToPlan(areas);
                }
            }
        } else if (PropertyChangeNames.isDeviceAdd(changedProperty)) {
            if (subject instanceof Area) {
                if (event.getNewValue() instanceof Object[]) {
                    final Device[] devices = ModelUtils.toDevices((Object[]) event.getNewValue());
                    addVisibleDevicesToPlan((Area) subject, devices);
                }
            }
        } else if (PropertyChangeNames.isAreaRemove(changedProperty)) {
            if (subject instanceof Object[]) {
                final Area[] areas = ModelUtils.toAreas((Object[]) event.getOldValue());
                if (currentFloor != null) {
                    removeVisibleAreasFromFloorPlan(currentFloor, areas);
                }
            }
        } else if (PropertyChangeNames.isDeviceRemove(changedProperty)) {
            if (subject instanceof Object[]) {
                final Area[] areas = ModelUtils.toAreas((Object[]) subject);
                final Device[] devices = ModelUtils.toDevices((Object[]) event.getOldValue());
                removeVisibleDevicesFromFloorPlan(areas, devices);
            }
        } else if (PropertyChangeNames.isNodeRemove(changedProperty)) {
            if (subject instanceof Project) {
                if (event.getOldValue() instanceof Object[]) {
                    final Floor[] floors = ModelUtils.toFloors((Object[]) event.getOldValue());
                    for (Floor f : floors) {
                        if (f == currentFloor) {
                            doUninstallFloorFigure();
                        }
                    }
                }
            }
        } else if (PropertyChangeNames.isProjectLoading(changedProperty)) {
            if (currentFloorFigure != null) {
                setPlanTitleInternal(null);

                // currentFloorFigure.
                currentFloorFigure.setFigureData(null);
                currentFloorFigure = null;
                currentFloor = null;
            }
        }
    }

    // CHECKSTYLE:OFF

    /**
     * Removes the control listener form the view's composite.
     * <p>
     * Some actions use this method to register themselves to be updated of the views'
     * control updates. For example the resize action must be notified of the maximized
     * state of the view in order to disable itself.
     * 
     * @param listener the control listener.
     */
    public final void removeControlListener(final ControlListener listener) {
        if (top == null) {
            throw new IllegalStateException();
        }

        if (!top.isDisposed()) {
            top.removeControlListener(listener);
        }
    }

    /**
     * Selection change event handler.
     * 
     * @param part the part when the selection is taking place
     * @param incoming the incoming the new selection object
     */
    @Override
    public final void selectionChanged(final IWorkbenchPart part, final ISelection incoming) {
        if (part == this) {
            return;
        }

        if (!(incoming instanceof IStructuredSelection)) {
            return;
        }

        final Set<Floor> floors = new HashSet<Floor>();
        final Set<Area> areas = new HashSet<Area>();
        final Set<Device> devices = new HashSet<Device>();

        selection = (IStructuredSelection) incoming;
        final ModelItem[] items = ModelUtils.toModelItems(selection.toArray());

        if (ModelUtils.areAllFloorsOrDescendants(items)) {
            for (ModelItem i : items) {
                final Floor f = Queries.getAncestorFloor(i);
                if (f != null && !floors.contains(f)) {
                    floors.add(f);
                }

                final Area a = Queries.getAncestorArea(i);
                if (a != null && !areas.contains(a)) {
                    areas.add(a);
                }

                if (ModelUtils.isDevice(i)) {
                    final Device d = (Device) i;
                    if (!devices.contains(d)) {
                        devices.add(d);
                    }
                }
            }
        }

        lastDevicesSelected = null;

        // FIXME: Handle no floor selected and multiple floors selected.

        // if (floors.size() == 0) {
        /*
         * Trouble, we have no valid floor selected
         */
        // if (selection.size() <= 1) {
        // doShowLabel(NO_FLOOR_SELECTED);
        // }
        // else {
        // doShowLabel(NO_VALID_SELECTION);
        // }
        // }

        // if (floors.size() > 1) {
        /*
         * Trouble, selection span multiple floors
         */
        // doShowLabel(MULTIPLE_FLOORS_SELECTED);
        // }

        final Device[] selectedDevices = devices.toArray(new Device[0]);

        if (floors.size() == 1) {
            /*
             * One floor only:
             */
            final Floor[] floor = floors.toArray(new Floor[0]);
            assert floor[0] != null;

            final FloorFigure figure = getFloorFigure(floor[0]);
            assert figure != null;

            lastDevicesSelected = selectedDevices;
            figure.selectDevices(lastDevicesSelected);

            final Floor selectedFloor = floor[0];

            setPlanTitleInternal(selectedFloor.getName());
            doDisplayFloorFigure(selectedFloor, figure);
        }

        /*
         * Unselects all
         */
        if (selectedDevices.length == 0 && currentFloorFigure != null) {
            currentFloorFigure.selectDevices(new Device[0]);
        }

    }

    /**
     * Sets the background color of the view.
     * <p>
     * The background color is also set for all floor figures.
     * 
     * @param color the new background color
     */
    public final void setBackground(final Color color) {
        top.setBackground(color);
        for (FloorFigure f : floorFigures.values()) {
            f.setBackgroundColor(color);
        }
    }

    /**
     * Implemented to respect the interface.
     */
    @Override
    public final void setFocus() {
        viewCanvas.redraw();
    }

    /**
     * @return the current zoom level
     */
    public final double getZoomLevel() {
        if (currentFloorFigure != null) {
            return currentFloorFigure.getZoomScale();
        } else {
            return 0.0;
        }
    }

    /**
     * Sets the current zoom level.
     * 
     * @param ratio the new zoom level
     */
    public final void setZoomLevel(final double ratio) {
        if (ratio > 0.0f) {
            if (currentFloorFigure != null) {
                currentFloorFigure.setZoomScale(ratio);
            }
        }
    }

    /**
     * Method that instructs the image to zoom in.
     * 
     * @param monitor the progress monitor. Ignored.
     */
    public final void zoomIn(final IProgressMonitor monitor) {
        int zoom = (int) Math.round(getZoomLevel() * 100.0);

        // find the next zoom step up
        for (int i = 0; i < ZOOM_STEPS.length; i++) {
            if (ZOOM_STEPS[i] > zoom) {
                zoom = ZOOM_STEPS[i];
                break;
            }
        }

        setZoomLevel(zoom / 100.0);
    }

    /**
     * Instructs the image to zoom out.
     * 
     * @param monitor the progress monitor. Ignored.
     */
    // FIXME: Zoom operation should display progress.
    public final void zoomOut(final IProgressMonitor monitor) {
        if (canZoomOut()) {
            int zoom = (int) Math.round(getZoomLevel() * 100.0);

            // find the next zoom step down
            for (int i = ZOOM_STEPS.length - 1; i >= 0; i--) {
                if (ZOOM_STEPS[i] < zoom) {
                    zoom = ZOOM_STEPS[i];
                    break;
                }
            }

            setZoomLevel(zoom / 100.0);
        }
    }

    /**
     * Instructs the image to return to its default zoom level.
     * 
     * @param monitor the progress monitor object. Ignored.
     */
    // FIXME: Zoom operation should display progress.
    public final void zoomToDefault(final IProgressMonitor monitor) {
        if (canZoomToDefault()) {
            final double referenceZoomLevel = getZoomLevel(currentFloor.getDefaultZoom());
            setZoomLevel(referenceZoomLevel);
        }
    }
}
