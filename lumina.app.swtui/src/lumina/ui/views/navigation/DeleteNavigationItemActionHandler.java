package lumina.ui.views.navigation;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.ui.actions.DeleteAreasAction;
import lumina.ui.actions.DeleteDevicesAction;
import lumina.ui.actions.DeleteFloorsAction;
import lumina.ui.views.AbstractDeleteTreeItemActionHandler;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Action handler for navigation item deletion.
 */
public class DeleteNavigationItemActionHandler extends
		AbstractDeleteTreeItemActionHandler {

	/**
	 * Constructor.
	 * 
	 * @param window
	 *            workbench window
	 */
	public DeleteNavigationItemActionHandler(IWorkbenchWindow window) {
		super(window);
	}

	/**
	 * Performs the item deletion.
	 * <p>
	 * Detects the model item type, devices, areas or floors, and calls the
	 * corresponding action deletion.
	 * 
	 * @param items
	 *            array with model items to be deleted
	 * @param window
	 *            workbench window
	 * @see DeleteDevicesAction#run()
	 * @see DeleteAreasAction#run()
	 * @see DeleteFloorsAction#run()
	 */
	public void doDeleteItems(final ModelItem[] items,
			final IWorkbenchWindow window) {
		if (ModelUtils.areAllDevices(items)) {
			final Device[] devices = ModelUtils.toDevices(items);
			if (devices.length > 0) {
				final IAction deleteDevices = new DeleteDevicesAction(devices,
						window);
				deleteDevices.run();
			}
		} else if (ModelUtils.areAllAreas(items)) {
			final Area[] areas = ModelUtils.toAreas(items);
			if (areas.length > 0) {
				final IAction deleteAreas = new DeleteAreasAction(areas, window);
				deleteAreas.run();
			}
		} else if (ModelUtils.areAllFloors(items)) {
			final Floor[] floors = ModelUtils.toFloors(items);
			if (floors.length > 0) {
				final IAction deleteFloors = new DeleteFloorsAction(floors,
						window);
				deleteFloors.run();
			}
		}
	}
}
