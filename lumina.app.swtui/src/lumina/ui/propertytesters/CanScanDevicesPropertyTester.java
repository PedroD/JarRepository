package lumina.ui.propertytesters;

import lumina.base.model.Area;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Queries;
import lumina.network.gateways.api.IGateway;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.TreeSelection;

/**
 * A property tester for the {@link lumina.ui.actions.ScanDevicesHandler}
 * operation.
 * <p>
 * Tests if a the devices selected can be manually scanned or not
 */
public class CanScanDevicesPropertyTester extends PropertyTester {

	/**
	 * Checks if at least one area as network connectivity.
	 * 
	 * @param areas
	 *            an array of areas
	 * @return <code>true</code> if the interface of at least one area with at
	 *         least one device has network connectivity ; returns
	 *         <code>false</code> otherwise.
	 */
	private static boolean atLeastOneAreaIsConnected(final Area[] areas) {
		for (Area a : areas) {
			final IGateway p = a.getAreaInterface();
			if (p != null && p.hasGatewayConnection() && a.hasDevices()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the model items selected have devices to be scanned.
	 * 
	 * @param items
	 *            the model items selected.
	 * @return <code>true</code> if the interface of at least one area is
	 *         connected and that area has a least one device.
	 */
	public static final boolean canScanDevices(final ModelItem[] items) {
		if (items.length > 0) {
			final lumina.base.model.Area[] areas = Queries.getAllAreas(items);
			return atLeastOneAreaIsConnected(areas);
		}

		return false;
	}

	/**
	 * Tests if devices can be scaned.
	 * <p>
	 * If the receiver is a tree selection and, if so, checks if it is possible
	 * to scan the model item devices.
	 * 
	 * @param receiver
	 *            selection ({@link TreeSelection})
	 * @param property
	 *            property
	 * @param args
	 *            arguments
	 * @param expectedValue
	 *            ignored
	 * @return true if devices can be scanned, false otherwise
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (receiver instanceof TreeSelection) {
			final TreeSelection selection = (TreeSelection) receiver;

			final ModelItem[] items = ModelUtils.toModelItems(selection
					.toArray());
			return canScanDevices(items);
		}

		return false;
	}
}
