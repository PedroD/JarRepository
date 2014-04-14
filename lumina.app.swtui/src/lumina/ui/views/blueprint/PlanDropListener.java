package lumina.ui.views.blueprint;

import lumina.base.model.Device;
import lumina.base.model.ModelUtils;
import lumina.base.model.transfer.DeviceTransfer;
import lumina.license.Capabilities;
import lumina.license.Capabilities.Capability;
import lumina.ui.views.blueprint.propertytesters.CanAddToPlanPropertyTester;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.graphics.Point;

/**
 * A {@link DropTargetListener drop target listener} that receives the devices
 * dragged into the plan view.
 */
public class PlanDropListener implements DropTargetListener {

	/**
	 * Holds the reference to the parent plan view.
	 */
	private final BlueprintView planView;

	/**
	 * Constructs a new drop listener for a given plan.
	 * 
	 * @param blueprint
	 *            the plan object
	 */
	PlanDropListener(final BlueprintView blueprint) {
		planView = blueprint;
	}

	/**
	 * Obtains the list of devices being dragged over the plan view.
	 * 
	 * @return a list of devices.
	 */
	private Device[] getDraggingDevices() {
		final ISelection selection = planView.getDraggingSelection();
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection s = (IStructuredSelection) selection;
			final Object[] objects = s.toArray();
			final Device[] devices = ModelUtils.toDevices(objects);

			return devices;
		} else {
			return null;
		}
	}

	private void dragUpdate(final DropTargetEvent event) {
		final boolean hasDevice = DeviceTransfer.getInstance().isSupportedType(
				event.currentDataType);

		final boolean appModeAllowsDropOnBluePrint = Capabilities
				.canDo(Capability.DEVICE_EDIT_PROPERTIES);

		if (hasDevice && planView.isImageReady()
				&& appModeAllowsDropOnBluePrint) {
			final Device[] devices = getDraggingDevices();
			if (devices != null && planView.getCurrentFloor() != null) {
				/*
				 * All the devices being dragged must be in the same floor.
				 */

				if (CanAddToPlanPropertyTester.canAddToBluePrint(devices,
						planView.getCurrentFloor())
						&& planView.canDropIn(event.x, event.y)) {
					event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL
							| DND.FEEDBACK_EXPAND | DND.FEEDBACK_INSERT_AFTER;
					event.detail = DND.DROP_COPY;

					return;
				}
			}
		}

		/*
		 * Show the "forbidden" cursor
		 */
		event.feedback = DND.FEEDBACK_NONE;
		event.detail = DND.DROP_NONE;
	}

	@Override
	public final void dragEnter(DropTargetEvent event) {
		dragUpdate(event);
	}

	@Override
	public final void dragOver(DropTargetEvent event) {
		dragUpdate(event);
	}

	@Override
	public final void dragOperationChanged(DropTargetEvent event) {
		dragUpdate(event);
	}

	@Override
	public final void dragLeave(DropTargetEvent event) {
		dragUpdate(event);
	}

	@Override
	public final void dropAccept(DropTargetEvent event) {
		dragUpdate(event);
	}

	@Override
	public final void drop(DropTargetEvent event) {
		if (event.data instanceof Object[]) {
			final Object[] objects = (Object[]) event.data;
			final Device[] devices = ModelUtils.toDevices(objects);
			final Point[] locations = new Point[devices.length];
			final Point currentLocation = new Point(event.x, event.y);

			for (int i = 0; i < devices.length; i++) {
				locations[i] = new Point(currentLocation.x, currentLocation.y);
				if (planView.canDropIn(currentLocation.x + 1,
						currentLocation.y + 1)) {
					currentLocation.x += 2;
					currentLocation.y += 2;
				}
			}

			planView.dropDevicesAt(devices, locations);
		}
	}
}
