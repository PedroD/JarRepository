package lumina.ui.views.blueprint.figures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lumina.base.model.Device;
import lumina.base.model.Floor;
import lumina.base.model.Queries;
import lumina.ui.swt.SWTImageCanvas;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

/**
 * Adds the list of devices.
 */
public class FloorFigure extends BaseFloorFigure implements ISelectionProvider,
		SWTImageCanvas.ViewportMotionListener {

	/**
	 * The edit part that is being represented by this figure.
	 */
	private final Floor floorUnderDisplay;

	/**
	 * The figures of the devices under display on the current floor.
	 */
	private final Map<Device, DeviceFigure> deviceFigures = new HashMap<Device, DeviceFigure>();

	/**
	 * The selection listeners registered for this floor.
	 */
	private final Set<ISelectionChangedListener> selectionChangeListeners = new HashSet<ISelectionChangedListener>();

	/**
	 * The figure that contains all the device figures.
	 */
	private final Figure deviceParentFigure;

	/**
	 * The reference scale used to adjust the size of the figures.
	 */
	private double referenceScale = 1.0;

	/**
	 * Tracks the current selection.
	 */
	private Set<Object> currentSelection;

	/**
	 * Indicates whether the locations of the figures should be corrected for
	 * preview or not. When this flag is <code>false</code>, the scale
	 * correction variables are set to 1.0.
	 */
	private boolean shouldPreviewResolution;

	/**
	 * The initial reference resolution. This is the resolution of the initial
	 * image. The floor figure is the target showing the preview.
	 */
	private org.eclipse.swt.graphics.Point previewReferenceResolution;

	/**
	 * Computed x-axis correction for the device positions based on the
	 * reference resolution. This variable is needed for computing the device
	 * positions preview positions when the aspect ratio of the image in preview
	 * is not the same as the aspect ration of the original image. The value is
	 * used by several coordinates methods of this class but only updated in one
	 * method.
	 * 
	 * @see #updateScaleCorrection()
	 */
	private double widthScaleCorrection = 1.0;

	/**
	 * Computed y-axis correction for the device positions based on the
	 * reference resolution. This variable is needed for computing the device
	 * positions preview positions when the aspect ratio of the image in preview
	 * is not the same as the aspect ration of the original image. The value is
	 * used by several coordinates methods of this class but only updated in one
	 * method.
	 * 
	 * @see #updateScaleCorrection()
	 */
	private double heightScaleCorrection = 1.0;

	/**
	 * Hash table table that contains the last seen coordinate of each device.
	 * Needed for restoring a device to the last place it was seen, when an item
	 * is re-add to the plan.
	 */
	private final Map<Device, Point> deviceCoordinatesMemory = new HashMap<Device, Point>();

	public FloorFigure(final Floor floor, final SWTImageCanvas canvas) {
		super(canvas);

		getRootFigure().setOpaque(false);
		getRootFigure().setLayoutManager(new XYLayout());

		floorUnderDisplay = floor;
		deviceParentFigure = new Viewport();
		deviceParentFigure.setOpaque(false);
		deviceParentFigure.setBounds(getViewportArea());
		deviceParentFigure.setVisible(false);

		canvas.addMoveListener(this);

		super.setContents(deviceParentFigure);

		final FigureMouseListener paneTool = new FigureMouseListener();
		deviceParentFigure.addMouseListener(paneTool);
		deviceParentFigure.addMouseMotionListener(paneTool);

		final Device[] devices = Queries.getAllDevices(floorUnderDisplay);
		for (Device d : devices) {
			if (d.isVisible()) {
				addDevice(d);
			}
		}
	}

	/**
	 * Adds a device figure to the floor.
	 * <p>
	 * Checks if a device figure object already exists, if not it creates a new
	 * one and saves it in the device figures map.
	 * 
	 * @param d
	 *            the device whose figure should be created.
	 */
	private void addDevice(final Device d) {
		if (!deviceFigures.containsKey(d)) {
			final DeviceFigure deviceFigure = DeviceFigureFactory
					.createDeviceFigure(d, this, getShell());
			deviceFigures.put(d, deviceFigure);
			deviceParentFigure.add(deviceFigure);
		}
	}

	/**
	 * Saves the coordinates of a device.
	 * 
	 * @param d
	 *            the device to be remembered
	 */
	private void saveCoordinates(final Device d) {
		final Point p = deviceCoordinatesMemory.get(d);
		if (p != null) {
			/*
			 * update
			 */
			p.x = d.getXCoordinate();
			p.y = d.getYCoordinate();
		} else {
			/*
			 * create new
			 */
			deviceCoordinatesMemory.put(d,
					new Point(d.getXCoordinate(), d.getYCoordinate()));
		}
	}

	/**
	 * Remembers the coordinates of a device.
	 * 
	 * @param d
	 *            the device to be remembered
	 * @return a point containing the last coordinates of the device of
	 *         <code>null</code> if the device was never added to the plan.
	 */
	private Point getSavedCoordinates(final Device d) {
		return deviceCoordinatesMemory.get(d);
	}

	private void ensureCoordinates(final Device d, final DeviceFigure f,
			final Point clientCoords) {
		if (d.hasValidCoordinates()) {
			final Point location = getFloorCoordinates(new Point(
					d.getXCoordinate(), d.getYCoordinate()));

			f.setCenterCoordinates(location);
		} else {
			final Point deviceCoordinates;

			/*
			 * Do we have a place to drop the device?
			 */
			if (clientCoords != null) {
				f.setCenterCoordinates(clientCoords);

				deviceCoordinates = getDeviceCoordinates(clientCoords);
			} else {
				/*
				 * Does the floor remember the coordinates?
				 */
				final Point p = getSavedCoordinates(d);
				final boolean remembersCoordinates = p != null;

				if (remembersCoordinates) {
					final Point lastLocation = getFloorCoordinates(p);
					f.setCenterCoordinates(lastLocation);

					deviceCoordinates = p;
				} else {
					final Point viewportCenter = getViewportCenterCoordinates();
					f.setCenterCoordinates(viewportCenter);

					deviceCoordinates = getDeviceCoordinates(viewportCenter);
				}
			}

			/*
			 * Set the new coordinates for the device
			 */
			d.setXCoordinate(deviceCoordinates.x);
			d.setYCoordinate(deviceCoordinates.y);
		}

		saveCoordinates(d);
	}

	private Set<Object> extractDevicesFromSelection(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			final Set<Object> incomingSelection = new HashSet<Object>();
			for (Object o : structuredSelection.toArray()) {
				incomingSelection.add(o);
			}

			return incomingSelection;
		} else {
			return null;
		}
	}

	private DeviceFigure findDeviceFigure(final Device d) {
		return deviceFigures.get(d);
	}

	/**
	 * Notifies any selection changed listeners that the viewer's selection has
	 * changed.
	 * <p>
	 * Only listeners registered at the time this method is called are notified.
	 * 
	 * @param selection
	 *            the new selection
	 * @see ISelectionChangedListener#selectionChanged
	 */
	protected void fireSelectionChangedEvent(final ISelection selection) {
		final SelectionChangedEvent e = new SelectionChangedEvent(this,
				selection);
		final ISelectionChangedListener[] listeners = selectionChangeListeners
				.toArray(new ISelectionChangedListener[0]);

		for (ISelectionChangedListener l : listeners) {
			l.selectionChanged(e);
		}
	}

	/**
	 * Updates the dimensions and positions of all floor figures using a given
	 * zoom scale.
	 * 
	 * @param zoomScale
	 *            the current zoom level
	 */
	private void updateAllInternal(final double zoomScale) {
		for (DeviceFigure figure : deviceFigures.values()) {
			deviceParentFigure.remove(figure);
		}
		deviceFigures.clear();

		final Device[] devices = Queries.getAllDevices(floorUnderDisplay);
		for (Device d : devices) {
			updateDeviceInternal(d, null, zoomScale);
		}
	}

	/**
	 * Updates the status of a device figure on the blueprint.
	 * 
	 * @param device
	 *            the device object
	 * @param clientCoords
	 *            the window coordinates where the device figure is to be placed
	 * @param zoomLevel
	 *            the zoom level of the plan image
	 */
	private void updateDeviceInternal(final Device device,
			final Point clientCoords, final double zoomLevel) {
		final boolean deviceExists = deviceFigures.containsKey(device);
		final boolean canShow = isImageReady();

		if (deviceExists) {
			final DeviceFigure deviceFigure = deviceFigures.get(device);

			deviceFigure.setVisible(false);
			deviceFigure.scale(zoomLevel / referenceScale);
			ensureCoordinates(device, deviceFigure, clientCoords);
			if (canShow) {
				deviceFigure.setVisible(device.isVisible());
			} else {
				deviceFigure.setVisible(false);
			}
		} else {
			if (device.isVisible()) {
				addDevice(device);
				deviceParentFigure.setVisible(canShow);

				final DeviceFigure deviceFigure = deviceFigures.get(device);
				if (deviceFigure != null) {
					deviceFigure.setVisible(false);
					deviceFigure.scale(zoomLevel / referenceScale);
					ensureCoordinates(device, deviceFigure, clientCoords);
					deviceFigure.setVisible(canShow);
				}
			}
		}
	}

	/**
	 * Marks a set of devices as being selected.
	 * <p>
	 * This method is incremental in the sense that its sends performs
	 * incremental select actions considering the the currently selected
	 * devices.
	 * 
	 * @param selection
	 *            the collection containing the devices to be selected.
	 */
	private void updateSelection(final Set<Object> selection) {
		if (selection != null) {
			/*
			 * New devices to be selected
			 */
			for (Object d : selection) {
				if (d instanceof Device) {
					final DeviceFigure f = findDeviceFigure((Device) d);
					if (f != null) {
						f.setSelected(true);
					}
				}
			}

			/*
			 * Devices to be unselected
			 */
			if (currentSelection != null) {
				for (Object d : currentSelection) {
					if (!selection.contains(d) && d instanceof Device) {
						final DeviceFigure f = findDeviceFigure((Device) d);
						if (f != null) {
							f.setSelected(false);
						}

					}
				}
			}
		}

		currentSelection = selection;
	}

	/**
	 * Updates the scale correction ratio variables.
	 * <p>
	 * The scale correction ratio is computed based on the current resolution
	 * and reference resolution. If the any of them is <code>null</code>, the
	 * scale correction is set to <tt>1.0</tt>.
	 */
	private void updateScaleCorrection() {
		final org.eclipse.swt.graphics.Point currentImageResolution = getCanvas()
				.getImageResolution();
		if (currentImageResolution != null
				&& previewReferenceResolution != null
				&& shouldPreviewResolution) {
			widthScaleCorrection = currentImageResolution.x * 1.0
					/ previewReferenceResolution.x;
			heightScaleCorrection = currentImageResolution.y * 1.0
					/ previewReferenceResolution.y;
		} else {
			widthScaleCorrection = 1.0;
			heightScaleCorrection = 1.0;
		}
	}

	/**
	 * Sets the current selection but does without firing any selection change
	 * events.
	 * 
	 * @param selection
	 *            the new selection to be set
	 */
	private void setCurrentSelectionInternal(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			updateSelection(extractDevicesFromSelection(structuredSelection));
		}
	}

	/**
	 * Implements the behavior of the mouse.
	 */
	private final class FigureMouseListener implements MouseListener,
			MouseMotionListener {
		private Dimension offset = new Dimension();
		private final Cursor handCursor = new Cursor(getShell().getDisplay(),
				SWT.CURSOR_SIZEALL);

		private Cursor lastMouseCursor;
		private boolean isPanning;

		private void saveMouseLastCusor() {
			lastMouseCursor = new Cursor(getShell().getDisplay(),
					SWT.CURSOR_ARROW);

			getShell().getCursor();
		}

		private void setHandCursor() {
			getShell().setCursor(handCursor);
		}

		private void restoreMouseSavedCursor() {
			getShell().setCursor(lastMouseCursor);
		}

		public void mousePressed(final MouseEvent event) {
			final org.eclipse.swt.graphics.Point p = getCanvas().getScroll();

			offset.width = event.x - p.x;
			offset.height = event.y - p.y;

			isPanning = true;

			event.consume();

			saveMouseLastCusor();
			setHandCursor();
		}

		public void mouseReleased(final MouseEvent event) {
			offset.width = 0;
			offset.height = 0;

			isPanning = false;

			restoreMouseSavedCursor();
		}

		public void mouseDragged(final MouseEvent event) {
			final int tx = event.x - offset.width;
			final int ty = event.y - offset.height;

			getCanvas().scrollTo(tx, ty);
		}

		public void mouseExited(final MouseEvent e) {
			if (isPanning) {
				restoreMouseSavedCursor();
			}
		}

		public void mouseEntered(final MouseEvent e) {
			if (isPanning) {
				setHandCursor();
			}
		}

		public void mouseHover(MouseEvent mouseevent) {
			// do nothing
		}

		public void mouseMoved(MouseEvent mouseevent) {
			// do nothing
		}

		public void mouseDoubleClicked(MouseEvent mouseevent) {
			// do nothing
		}
	}

	/**
	 * Reacts zoom scale changes.
	 * <p>
	 * Updates all devices with the specified scale, causing them to be updated
	 * via {
	 * 
	 * @param ratio
	 *            the new zoom ratio
	 */
	@Override
	protected void handleSetZoomRatio(final double ratio) {
		updateAllInternal(ratio);
		getCanvas().redraw();
	}

	/**
	 * Reacts to the availability of a new image.
	 * <p>
	 * If a new background image is ready, the device figures are shown.
	 */
	@Override
	protected void handleImageReady() {
		if (isImageReady()) {
			updateScaleCorrection();
			deviceParentFigure.setVisible(true);
		}
	}

	/**
	 * Reacts to the change of visibility of the figure.
	 * <p>
	 * Shows the device images if the background image is ready.
	 * 
	 * @param visible
	 *            specifies whether the figure should be made visible or
	 *            invisible.
	 */
	@Override
	protected void handleSetVisible(boolean visible) {
		if (visible && isImageReady()) {
			updateScaleCorrection();
			deviceParentFigure.setVisible(true);
		} else {
			deviceParentFigure.setVisible(false);
		}
	}

	/**
	 * Accepts the preview resolution change and updates the positions of the
	 * devices.
	 */
	public void acceptPreviewResolution() {
		if (previewReferenceResolution != null && shouldPreviewResolution) {
			/*
			 * update the devices displaying
			 */
			for (Device d : deviceFigures.keySet()) {
				d.setXCoordinate((int) (d.getXCoordinate() * widthScaleCorrection));
				d.setYCoordinate((int) (d.getYCoordinate() * heightScaleCorrection));
			}

			/*
			 * update the device positions memory
			 */
			for (Device d : deviceCoordinatesMemory.keySet()) {
				d.setXCoordinate((int) (d.getXCoordinate() * widthScaleCorrection));
				d.setYCoordinate((int) (d.getYCoordinate() * heightScaleCorrection));
			}
		}

		/*
		 * reset the scale correction used so far
		 */
		shouldPreviewResolution = false;
		previewReferenceResolution = null;
		updateScaleCorrection();
	}

	/**
	 * Adds a device the selection.
	 * <p>
	 * This is the method to be called when the user clicks on a device figure
	 * with CTRL key pressed.
	 * 
	 * @param device
	 *            the device to be selected that must exist in the list of
	 *            devices being displayed
	 */
	public void addDeviceToSelection(final Device device) {
		// XXX:
		throw new UnsupportedOperationException("Not implemented");
	}

	/*
	 * (non-Javadoc) Method declared on ISelectionProvider
	 */
	public void addSelectionChangedListener(
			final ISelectionChangedListener listener) {
		if (!selectionChangeListeners.contains(listener)) {
			selectionChangeListeners.add(listener);
		}
	}

	public void centerViewPort(final int cx, final int cy) {
		final Point p = getCanvasCoordinates(new Point(cx, cy));
		getCanvas().center(p.x, p.y);
	}

	/**
	 * Disposes the current figure.
	 */
	public void dispose() {
		getCanvas().removeMoveListener(this);
	}

	/**
	 * Handles the scrolls event of the floor figure.
	 * <p>
	 * Updates the position of the parent figure, which contains the device
	 * figures representing the devices. This responds to changes in the
	 * position sent by the canvas.
	 * 
	 * @param x
	 *            the new x coordinate
	 * @param y
	 *            the new y coordinate
	 * @param width
	 *            the width to be rendered
	 * @param height
	 *            the height to be rendered
	 */
	public void moveTo(int x, int y, int width, int height) {
		final Rectangle area = new Rectangle(-x, -y, width, height);
		deviceParentFigure.setBounds(area);
		deviceParentFigure.repaint();
	}

	/**
	 * Computes the logical coordinates of an object from the coordinates of the
	 * floor image.
	 * <p>
	 * This method computes the zoom-independent and translation independent
	 * coordinates. It compensates the floor zoom level.
	 * 
	 * @param floorCoords
	 *            the coordinates on the floor image
	 * @return the logical coordinates of the object
	 * @see FloorFigure#getFloorCoordinates(Point)
	 */
	public Point getDeviceCoordinates(final Point floorCoords) {
		final double scale = getZoomScale();
		final Point t = deviceParentFigure.getLocation();

		return floorCoords.getCopy().translate(-t.x + 1, -t.y + 1)
				.scale(1 / scale)
				.scale(1 / widthScaleCorrection, 1 / heightScaleCorrection);
	}

	/**
	 * Computes the size of the floor figure.
	 * <p>
	 * Computes the zoom-independent size of the floor figure.
	 * 
	 * @return the size of the figure.
	 */
	public Dimension getSize() {
		final double scale = getZoomScale();
		final Dimension d = deviceParentFigure.getSize();

		return d.getCopy().scale(1 / scale)
				.scale(1 / widthScaleCorrection, 1 / heightScaleCorrection);
	}

	/**
	 * Computes the coordinates of a object in the floor image.
	 * <p>
	 * It takes into account the floor zoom level.
	 * 
	 * @param deviceCoordinates
	 *            the original device coordinates
	 * @return the new coordinates for the device
	 * @see #getFloorCoordinates(Point)
	 */
	public Point getFloorCoordinates(final Point deviceCoordinates) {
		final Point translation = deviceParentFigure.getLocation();

		return getCanvasCoordinates(deviceCoordinates).translate(translation);
	}

	/**
	 * Computes the coordinates of an object in the canvas.
	 * <p>
	 * This method translates a point in the device normalized coordinates into
	 * the canvas coordinates. This can be used for example if we need to
	 * display the mean point of two devices directly in the canvas.
	 * 
	 * @param deviceCoordinates
	 *            the coordinates of the object.
	 * @return the canvas coordinates
	 */
	public Point getCanvasCoordinates(final Point deviceCoordinates) {
		return deviceCoordinates.getCopy().scale(getZoomScale())
				.scale(widthScaleCorrection, heightScaleCorrection);
	}

	public Point getViewportCenterCoordinates() {
		final Point center = getViewportArea().getCenter();
		return center;
	}

	/**
	 * Obtains the currently set preview resolution. If
	 * {@link #acceptPreviewResolution()} has been called the result will also
	 * be <code>null</code>.
	 * 
	 * @return the currently set preview resolution or <code>null</code>
	 */
	public org.eclipse.swt.graphics.Point getPreviewResolution() {
		return previewReferenceResolution;
	}

	/**
	 * @return the reference scale
	 */
	public double getReferenceScale() {
		return referenceScale;
	}

	/**
	 * Retrieves the current selection.
	 * <p>
	 * This method is part of the implementation of {@link ISelectionProvider}.
	 * 
	 * @return a {@link StructuredSelection} object with a list reflecting
	 *         currently selected devices (or an empty list if no device is
	 *         selected)
	 */
	public ISelection getSelection() {
		if (currentSelection == null) {
			return new StructuredSelection();
		} else {
			final StructuredSelection selection = new StructuredSelection(
					currentSelection.toArray());
			return selection;
		}
	}

	/**
	 * Checks is a selection is valid.
	 * 
	 * @param selection
	 *            the new selection to be set
	 * @return <code>true</code> if the selection is an
	 *         {@link IStructuredSelection}, and <code>false</code> otherwise.
	 */
	public final boolean isValidSelection(final ISelection selection) {
		final boolean isValid = selection instanceof IStructuredSelection;

		// post-condition
		assert isValid && (selection instanceof IStructuredSelection);
		return isValid;
	}

	/**
	 * Sets the preview of the device positions on of off.
	 * <p>
	 * The devices will be change position on the next image rendering.
	 * 
	 * @param preview
	 *            if set to <code>true</code> means that the preview is on.
	 */
	public void previewResolutionChange(final boolean preview) {
		shouldPreviewResolution = preview;
	}

	/**
	 * Sets the preview resolution.
	 * 
	 * @param resolution
	 *            the resolution of the starting image; can be <code>null</code>
	 *            indicating the preview no longer applies.
	 */
	public void setPreviewResolution(
			final org.eclipse.swt.graphics.Point resolution) {
		if (resolution == null) {
			/*
			 * reset the scale correction variables to 1.0
			 */
			previewReferenceResolution = null;
			updateScaleCorrection();

			// CHECKSTYLE:OFF - These constants are only used here
			assert Math.abs(widthScaleCorrection - 1.0) < 0.0001;
			assert Math.abs(heightScaleCorrection - 1.0) < 0.0001;
			// CHECKSTYLE:ON
		} else {
			previewReferenceResolution = resolution;
		}
	}

	/**
	 * Updates a devices in the blueprint.
	 * <p>
	 * Turns the device visible/invisible or updates its scale.
	 * 
	 * @param device
	 *            the device to be updated.
	 * @param clientCoords
	 *            the client coordinates
	 */
	public void updateDevice(final Device device, final Point clientCoords) {
		updateDeviceInternal(device, clientCoords, getZoomScale());
	}

	/*
	 * (non-Javadoc) Method declared on ISelectionProvider
	 */
	public void removeSelectionChangedListener(
			final ISelectionChangedListener listener) {
		if (selectionChangeListeners.contains(listener)) {
			selectionChangeListeners.remove(listener);
		}
	}

	public void removeDevice(final Device d) {
		if (deviceFigures.containsKey(d)) {
			final DeviceFigure deviceFigure = findDeviceFigure(d);
			deviceParentFigure.remove(deviceFigure);
			deviceFigures.remove(d);
		}
	}

	/**
	 * Sets the selection to a device of the blueprint.
	 * <p>
	 * This is the method to be called when the user clicks on a device figure.
	 * 
	 * @param device
	 *            the device to be selected that must exist in the list of
	 *            devices being displayed
	 */
	public void selectDevice(final Device device) {
		if (deviceFigures.containsKey(device)) {
			final HashSet<Object> deviceSelection = new HashSet<Object>();
			deviceSelection.add(device);
			updateSelection(deviceSelection);

			fireSelectionChangedEvent(getSelection());
		}
	}

	/**
	 * Sets the selection to a set of devices on the blueprint.
	 * <p>
	 * If a device does not have a corresponding figure in the blueprint, it is
	 * discarded.
	 * 
	 * @param devices
	 *            the array containing the devices whose figures have to be
	 *            selected.
	 */
	public void selectDevices(final Device[] devices) {
		final HashSet<Object> deviceSelection = new HashSet<Object>();
		for (Device d : devices) {
			if (deviceFigures.containsKey(d)) {
				deviceSelection.add(d);
			}
		}

		updateSelection(deviceSelection);
	}

	/**
	 * Sets the reference scale.
	 * <p>
	 * Causes all devices to be updated via {{@link #updateAll()}. Throws
	 * {@link IllegalArgumentException} if the zoom level is not positive.
	 * 
	 * @param scale
	 *            the new level to be set
	 */
	public void setReferenceScale(double scale) {
		if (scale > 0.0) {
			this.referenceScale = scale;
			updateAll();
		} else {
			throw new IllegalArgumentException("Level should be positive.");
		}
	}

	/**
	 * Sets the current selection.
	 * <p>
	 * This method is part of the implementation of {@link ISelectionProvider}.
	 * Informs the selection listeners that the selection has changed.
	 * 
	 * @param selection
	 *            the new selection to be set
	 */
	public void setSelection(final ISelection selection) {
		if (isValidSelection(selection)) {
			setCurrentSelectionInternal(selection);
			fireSelectionChangedEvent(selection);
		}
	}

	/**
	 * Updates the dimensions and positions of all floor figures.
	 * <p>
	 * Taking into account the current zoom scale.
	 */
	public void updateAll() {
		updateAllInternal(getZoomScale());
	}

}
