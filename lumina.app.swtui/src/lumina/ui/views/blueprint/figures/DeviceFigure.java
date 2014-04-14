package lumina.ui.views.blueprint.figures;

import lumina.api.properties.IProperty;
import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.ProjectModel;
import lumina.base.model.PropertyChangeNames;
import lumina.base.model.devices.PowerPropertyDevice;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.jface.CommandUtils;
import lumina.ui.swt.ApplicationImageCache;

import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * The base class for device figures displayed in the plan.
 * <p>
 * Devices are currently displayed as yellow shapes over the floor plan and have
 * different drawing according the status changes.
 * <p>
 * When a device figure is create it installs itself as a property change
 * listener in the project to receive updates about the status changes of the
 * device it is representing.
 * <p>
 * This class handles:
 * <ol>
 * <li>Dragging devices over the plan</li>
 * <li>Reacts to mouse right-click, showing a context menu with actions than can
 * be performed on the device</li>
 * </ol>
 */
public abstract class DeviceFigure extends Figure implements
		IPropertyChangeListener, ISelectionListener {

	/**
	 * ID of the show device panel command.
	 */
	private static final String SHOW_DEVICE_PANEL_COMMAND_ID = "lumina.control.commands.showDeviceControl";

	private static final int TOOLTIP_DEFAULT_SPACING = 3;

	private static final int MOUSE_RIGHT_EVENT_BUTTON = 3;

	private static final int MOUSE_LEFT_EVENT_BUTTON = 1;

	private static final Color TOOLTIP_BORDER_COLOR = ColorConstants.buttonDarker;

	private static final int TOOLTIP_BORDER_INSETS = 2;

	private static final int BORDER_WIDTH = 2;

	private static final Color SELECTED_COLOR = ColorConstants.red;

	private final Device representedDevice;

	private final Shell currentShell;

	private Color deviceOffColor;

	private Color deviceOnColor;

	/**
	 * The property that represents the name of the device.
	 */
	private final IProperty deviceNameProperty;

	/**
	 * The property that represents the type of the device.
	 */
	private final IProperty deviceTypeProperty;

	/**
	 * The property that represents the x coordinate of the device.
	 */
	private final IProperty xCoordinateProperty;

	/**
	 * The property that represents the y coordinate of the device.
	 */
	private final IProperty yCoordinateProperty;

	/**
	 * The property that represents the name of the area where the device is
	 * located.
	 */
	private IProperty deviceAreaNameProperty;

	/**
	 * The property that represents the power of the device.
	 */
	private IProperty devicePowerProperty;

	private Dimension delta = new Dimension();

	private Point initialClick;

	private Cursor lastMouseCursor;

	private FloorFigure floorFigure;

	private final IFigure deviceFigure;

	/**
	 * Flag that tracks whether this device is selected.
	 */
	private boolean isSelected;

	/**
	 * Flag that indicates whether the figure is being moved.
	 */
	private boolean isMovingMode;

	/**
	 * The red border that indicated that the device is selected.
	 */
	private final Border selectionBorder;

	/**
	 * The mouse motion listener.
	 */
	private final MouseMotionListener mouseMotionListener = new MouseMotionListener() {
		/**
		 * Changes the mouse hand cursor.
		 * 
		 * @param event
		 *            the mouse event
		 */
		public final void mouseDragged(MouseEvent event) {
			if (isMovingMode) {
				final Point p = getCenterCoordinates().getCopy();
				p.x = event.x - delta.width;
				p.y = event.y - delta.height;

				if (!getParent().getBounds().contains(p)) {
					setNotAllowedCursor();
				} else {
					setHandMouseCursor();
					DeviceFigure.this.setCenterCoordinates(p);
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			// do nothing
		}

		@Override
		public final void mouseExited(MouseEvent event) {
			restoreMouseCursor();
			isMovingMode = false;
		}

		@Override
		public final void mouseMoved(MouseEvent event) {
			if (isMovingMode) {
				final Point p = getCenterCoordinates().getCopy();
				p.x = event.x - delta.width;
				p.y = event.y - delta.height;

				DeviceFigure.this.setCenterCoordinates(p);
			}
		}

		@Override
		public void mouseHover(MouseEvent mouseevent) {
			// do nothing
		}
	};

	/**
	 * Instantiates a new device figure.
	 * 
	 * @param d
	 *            the device to be represented
	 * @param f
	 *            the floor figure object
	 * @param shell
	 *            the shell object
	 */
	public DeviceFigure(final Device d, FloorFigure f, final Shell shell) {
		super();

		representedDevice = d;
		floorFigure = f;
		currentShell = shell;

		deviceOffColor = Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_YELLOW);
		deviceOnColor = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);

		deviceNameProperty = representedDevice.getPropertyManager()
				.findPropertyByName(Device.NAME_PROPERTY_NAME);

		deviceTypeProperty = representedDevice.getPropertyManager()
				.findPropertyByName(Device.DEVICE_TYPE_PROPERTY_NAME);

		xCoordinateProperty = representedDevice.getPropertyManager()
				.findPropertyByName(Device.DEVICE_XCOORDINATE_PROPERTY_NAME);

		yCoordinateProperty = representedDevice.getPropertyManager()
				.findPropertyByName(Device.DEVICE_YCOORDINATE_PROPERTY_NAME);

		deviceFigure = createDeviceFigure(representedDevice);
		deviceFigure.setSize(deviceFigure.getPreferredSize());

		this.setSize(deviceFigure.getSize());
		this.add(deviceFigure);

		final LineBorder b = new LineBorder();
		b.setColor(SELECTED_COLOR);
		b.setWidth(BORDER_WIDTH);
		selectionBorder = b;

		ProjectModel.getInstance().addPropertyChangeListener(this);
		addMouseMotionListener(mouseMotionListener);

		this.revalidate();
		repaint();
	}

	/**
	 * Loads the properties that are optional or those that can change suddenly.
	 */
	private void ensureVolatileProperties() {
		final Area parentArea = representedDevice.getParentArea();
		if (parentArea != null) {
			deviceAreaNameProperty = parentArea.getPropertyManager()
					.findPropertyByName(Area.NAME_PROPERTY_NAME);
		}

		devicePowerProperty = representedDevice.getPropertyManager()
				.findPropertyByName(
						PowerPropertyDevice.DEVICE_POWER_PROPERTY_NAME);

	}

	private void saveMouseCursor() {
		lastMouseCursor = currentShell.getCursor();
	}

	private void restoreMouseCursor() {
		currentShell.setCursor(lastMouseCursor);
	}

	private void setHandMouseCursor() {
		final Cursor handCursor = new Cursor(currentShell.getDisplay(),
				SWT.CURSOR_HAND);
		currentShell.setCursor(handCursor);
	}

	private void setNotAllowedCursor() {
		final Cursor handCursor = new Cursor(currentShell.getDisplay(),
				SWT.CURSOR_NO);
		currentShell.setCursor(handCursor);
	}

	protected final void updateBorder() {
		if (isSelected) {
			this.setBorder(selectionBorder);
		} else {
			this.setBorder(null);
		}

		repaint();
	}

	protected final void setSelected(final boolean status) {
		if (status != isSelected) {
			isSelected = status;
			updateBorder();
		}
	}

	/**
	 * Computes the coordinates of the figure center.
	 * <p>
	 * Descending classes should override this method to provide the figure
	 * center.
	 * 
	 * @return the relative coordinates of the figure center
	 */
	protected abstract Point getFigureCenter();

	/**
	 * Creates figure representing the device.
	 * <p>
	 * This is a default implementation that creates a figure object with the
	 * device picture. Descending classes are expected to override this method.
	 * 
	 * @param device
	 *            the device object
	 * @return an {@link IFigure} object
	 */
	public IFigure createDeviceFigure(final Device device) {
		final String iconImagePath = device.getDeviceType().getDefaultIcon();

		final Image image = ApplicationImageCache.getInstance().getImage(
				iconImagePath);
		final ImageFigure iconFigure = new ImageFigure(image);

		return iconFigure;
	}

	public final void dispose() {
		removeMouseMotionListener(mouseMotionListener);
		ProjectModel.getInstance().removePropertyChangeListener(this);
	}

	@Override
	public final void handleMousePressed(MouseEvent event) {
		setFocusTraversable(true);
		event.getState();

		final boolean leftClick = event.button == MOUSE_LEFT_EVENT_BUTTON;
		if (leftClick) {
			if (Capabilities.canDo(Capability.DEVICE_EDIT_PROPERTIES)) {
				final Point center = DeviceFigure.this.getCenterCoordinates();
				initialClick = center;

				delta.width = event.x - center.x;
				delta.height = event.y - center.y;

				isMovingMode = true;

				saveMouseCursor();
				setHandMouseCursor();
			}
		}

		final boolean rightClick = event.button == MOUSE_RIGHT_EVENT_BUTTON;
		if (rightClick) {
			final Menu contextMenu = new Menu(floorFigure.getCanvas());
			representedDevice.getStatus().getContributionContextMenu(
					contextMenu);
			new MenuItem(contextMenu, SWT.SEPARATOR);
			CommandUtils.createContributionMenu(SHOW_DEVICE_PANEL_COMMAND_ID,
					SWT.PUSH, contextMenu);
			floorFigure.getCanvas().setMenu(contextMenu);

			/*
			 * TODO: Allow other plugins to add actions to the context menu of a
			 * device in the floorplan by adding command to the URI of the popup
			 * menu.
			 * 
			 * MenuManager contextMenu = new
			 * MenuManager("lumina.floorplan.device"); contextMenu.add(new
			 * Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			 * contextMenu.setRemoveAllWhenShown(true); Menu menu =
			 * contextMenu.createContextMenu(floorFigure.getCanvas());
			 * floorFigure.getCanvas().setMenu(menu);
			 * representedDevice.getStatus().getContributionContextMenu(menu);
			 */
		}

		if (leftClick || rightClick) {
			floorFigure.selectDevice(representedDevice);
		}

		if (leftClick && event.button == MouseEvent.CONTROL) {
			floorFigure.addDeviceToSelection(representedDevice);
		}

		/*
		 * Instructs SWT to forward further mouse events to this widget even if
		 * they are generated outside the widget.
		 */
		event.consume();

		super.handleMousePressed(event);
	}

	@Override
	public final void handleMouseReleased(MouseEvent event) {
		if (isMovingMode) {
			isMovingMode = false;
			final Point center = DeviceFigure.this.getCenterCoordinates();

			final Dimension delta2 = new Dimension();
			delta2.width = event.x - center.x;
			delta2.height = event.y - center.y;

			final boolean figureHasMoved = initialClick != null
					&& !center.equals(initialClick);

			if (figureHasMoved) {
				DeviceFigure.this.fireDeviceLocationChanged(floorFigure
						.getDeviceCoordinates(center));
			}

			delta.width = 0;
			delta.height = 0;

			restoreMouseCursor();
		}

		super.handleMouseReleased(event);
	}

	/**
	 * Shows the control panel of the selected device.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void handleMouseDoubleClicked(MouseEvent event) {
		CommandUtils.executeCommand(SHOW_DEVICE_PANEL_COMMAND_ID);
		super.handleMouseReleased(event);
	}

	/**
	 * Computes the coordinates that correspond to the center of the figure.
	 * <p>
	 * Computes the figure location and then performs a translation using the
	 * coordinates of the center of the figure.
	 * 
	 * @return the center of the figure.
	 */
	public final Point getCenterCoordinates() {
		final Point location = getLocation().getCopy();
		return location.translate(getFigureCenter());
	}

	/**
	 * Sets the device figure at the given center.
	 * <p>
	 * Computes the figure location by performs a translation using the
	 * coordinates of the center of the figure.
	 * 
	 * @param newCenter
	 *            the new center coordinates
	 */
	public final void setCenterCoordinates(final Point newCenter) {
		final Point center = getFigureCenter();
		final Point location = newCenter.getCopy().translate(center.negate());
		setLocation(location);
	}

	public final Figure getToolTip() {
		final String sep = ": ";

		final Figure toolTip = new Figure();
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		layout.setStretchMinorAxis(false);
		layout.setSpacing(TOOLTIP_DEFAULT_SPACING);

		toolTip.setLayoutManager(layout);

		final Border border = new CompoundBorder(new LineBorder(
				TOOLTIP_BORDER_COLOR), new MarginBorder(TOOLTIP_BORDER_INSETS));

		toolTip.setBorder(border);

		final String deviceNameString = deviceNameProperty.getValue()
				.toString();

		final Label deviceName;

		ensureVolatileProperties();
		if (deviceAreaNameProperty != null) {
			deviceName = new Label(deviceNameString + " ["
					+ deviceAreaNameProperty.getValue() + "]");
		} else {
			deviceName = new Label(deviceNameString);
		}
		toolTip.add(deviceName);

		final String deviceTypeString = deviceTypeProperty.getName() + sep
				+ deviceTypeProperty.getValue();

		final Label deviceType;
		if (representedDevice.getPower() == 0) {
			deviceType = new Label(deviceTypeString);
		} else {
			final String power = devicePowerProperty.getValue().toString();
			deviceType = new Label(deviceTypeString + " [" + power + "]");
		}
		toolTip.add(deviceType);

		/*
		 * Note: Device power is sent in the device hint
		 */
		final String status = DeviceStatus.NAME + sep
				+ representedDevice.getStatus().getHint();
		final Label deviceStatus = new Label(status);
		toolTip.add(deviceStatus);

		return toolTip;
	}

	public final void fireDeviceLocationChanged(final Point p) {
		// perform a single x coordinate change
		if (representedDevice.getXCoordinate() != p.x
				&& representedDevice.getYCoordinate() == p.y) {
			ProjectModel.getInstance().changeProperty(representedDevice,
					xCoordinateProperty, p.x);
		}

		// perform a single y coordinate change
		if (representedDevice.getYCoordinate() != p.y
				&& representedDevice.getXCoordinate() == p.x) {
			ProjectModel.getInstance().changeProperty(representedDevice,
					yCoordinateProperty, p.y);
		}

		// perform a simultaneous change
		if (representedDevice.getYCoordinate() != p.y
				&& representedDevice.getXCoordinate() != p.x) {
			ProjectModel.getInstance()
					.changeProperty(
							representedDevice,
							new IProperty[] { xCoordinateProperty,
									yCoordinateProperty },
							new Object[] { p.x, p.y });
		}
	}

	public final Color getDeviceOffColor() {
		return deviceOffColor;
	}

	public final Color getDeviceOnColor() {
		return deviceOnColor;
	}

	@Override
	public final Dimension getPreferredSize(int wHint, int hHint) {
		return deviceFigure.getPreferredSize();
	}

	@Override
	public final Dimension getMinimumSize(int hint, int hint2) {
		return getSize();
	}

	public final void scale(final double scaleFactor) {
		final Dimension dims = getPreferredSize().getCopy();
		dims.scale(scaleFactor);
		setSize(dims);
		deviceFigure.setSize(dims);

		repaint();
	}

	public final void selectionChanged(final IWorkbenchPart part,
			final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			final Object subject = structuredSelection.getFirstElement();
			if (subject == representedDevice) {
				setSelected(true);
			} else {
				setSelected(false);
			}
		}
	}

	protected void updateStatus(DeviceStatus status) {
	}

	public final void propertyChange(final PropertyChangeEvent event) {
		final Object subject = event.getSource();
		if (subject == representedDevice) {
			final String changedProperty = event.getProperty();
			// Logger.getInstance().log(LogService.LOG_DEBUG,"Figure catched: Property change:" +
			// event.toString());
			// Logger.getInstance().log(LogService.LOG_DEBUG,"Property name:" + changedProperty);

			// FIXME: Properties system must undergo major refactoring.

			// if (PropertyChangeNames.isMetadataChange(changedProperty)) {
			// final Point newLocation = floorFigure.getFloorCoordinates(new
			// Point(
			// device.getXCoordinate(), device.getYCoordinate()));
			//
			// if (!getLocation().equals(newLocation)) {
			// setLocation(newLocation);
			// }

			// bug: status change not being sent by properties view
			// updateStatus(device.getStatus());
			// }

			if (PropertyChangeNames.isStatusChange(changedProperty)) {
				updateStatus(representedDevice.getStatus());
			}
		}
	}

	/**
	 * Sets the device color for off or inactive status.
	 * 
	 * @param color
	 *            the new color
	 */
	public final void setDeviceOffColor(Color color) {
		this.deviceOffColor = color;
	}

	/**
	 * Sets the device color for on or active status.
	 * 
	 * @param color
	 *            the new color
	 */
	public final void setDeviceOnColor(Color color) {
		this.deviceOnColor = color;
	}
}
