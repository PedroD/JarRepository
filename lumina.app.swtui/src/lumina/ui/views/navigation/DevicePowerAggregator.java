package lumina.ui.views.navigation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.Floor;
import lumina.base.model.ModelItem;
import lumina.base.model.Project;
import lumina.base.model.devices.PowerPropertyDevice;
import lumina.qp.AbstractSum;
import lumina.qp.AggregateFunction;
import lumina.qp.AggregateResult;
import lumina.qp.Aggregator;
import lumina.kernel.util.PowerUnit;

/**
 * Power aggregator device.
 */
public class DevicePowerAggregator extends Aggregator<ModelItem> {
	/**
	 * String that is printed when the device has no power property.
	 */
	private static final String NO_POWER_CONSUMPTION = "-";

	/**
	 * Cache of groups.
	 */
	private final Map<ModelItem, Object[]> cachedGroups = new HashMap<ModelItem, Object[]>();

	/**
	 * Creates aggregate function array for model items.
	 * 
	 * @return an aggregation function for each item.Fs
	 */
	@Override
	@SuppressWarnings("unchecked")
	public final AggregateFunction<ModelItem, ?>[] createAggregateFunctions() {
		/**
		 * Aggregate function.
		 * <p>
		 * Sums all the power estimates of all devices that have a power
		 * property.
		 * 
		 * @see PowerPropertyDevice
		 * @see AbstractSum
		 */
		final AggregateFunction<? extends ModelItem, ?> f = new AbstractSum<Device>() {
			/**
			 * Extracts the power consumption estimate, or null if the specified
			 * device does not have a power property.
			 * 
			 * @param element
			 *            device to extract power consumption estimate from
			 * @return device power consumption estimate, or null if not
			 *         aplicable
			 */
			@Override
			public Number extract(Device element) {
				if (element.hasPower()) {
					// If the device does not have power assigned
					// do not display '0W'
					if (element.getPower() == PowerPropertyDevice.NO_POWER) {
						return null;
					} else {
						return element.getPowerConsumptionEstimate();
					}
				} else {
					return null;
				}
			}
		};

		return (AggregateFunction<ModelItem, ?>[]) new AggregateFunction[] { f };
	}

	/**
	 * Aggregates the result of a power consumption for a specified group of
	 * model items.
	 * 
	 * @param id
	 *            object identifier
	 * @param group
	 *            model item group
	 * @return aggregated value for the specified group of model items
	 */
	@Override
	protected final AggregateResult createGroupResult(final Object id,
			final Group<ModelItem> group) {
		final Object[] results = group.getResults();
		if (results.length > 0) {
			final String wattsHour;
			if (results[0] instanceof Number) {
				wattsHour = PowerUnit.formatPower(
						((Number) results[0]).doubleValue(), true);
			} else {
				wattsHour = NO_POWER_CONSUMPTION;
			}
			return new AggregateResult(id, new Object[] { wattsHour });
		} else {
			return null;
		}
	}

	private static Object[] getGroupsInternal(final ModelItem o) {
		Device d = null;
		Area a = null;
		Floor f = null;
		Project p = null;

		if (o instanceof Device) {
			d = (Device) o;
		} else if (o instanceof Area) {
			a = (Area) o;
		} else if (o instanceof Floor) {
			f = (Floor) o;
		} else if (o instanceof Project) {
			p = (Project) o;
		}

		if (a == null && d != null) {
			a = d.getParentArea();
		}

		if (f == null && a != null) {
			f = a.getParentFloor();
		}

		if (p == null && f != null) {
			p = f.getParentProject();
		}

		final List<ModelItem> itemGroups = new LinkedList<ModelItem>();

		if (d != null) {
			itemGroups.add(d);
		}

		if (a != null) {
			itemGroups.add(a);
		}

		if (f != null) {
			itemGroups.add(f);
		}

		if (p != null) {
			itemGroups.add(p);
		}

		return itemGroups.toArray(new ModelItem[0]);
	}

	/**
	 * Returns the available groups.
	 * 
	 * @param o
	 *            model item
	 * @return array of groups
	 */
	@Override
	protected final Object[] getGroups(final ModelItem o) {
		final Object[] groups = cachedGroups.get(o);
		if (groups == null) {
			final Object[] newGroups = getGroupsInternal(o);
			cachedGroups.put(o, newGroups);
			return newGroups;
		} else {
			return groups;
		}
	}

	/**
	 * Handle for element removal from a group.
	 * 
	 * @param elem
	 *            model item to be removed
	 * @param group
	 *            model item group
	 */
	@Override
	protected final void handleRemoveElement(final ModelItem elem,
			final Group<ModelItem> group) {
		cachedGroups.remove(elem);
	}
}
