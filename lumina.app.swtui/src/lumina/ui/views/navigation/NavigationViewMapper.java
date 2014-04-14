package lumina.ui.views.navigation;

import lumina.base.model.Device;
import lumina.base.model.ModelItem;
import lumina.base.model.Queries;
import lumina.qp.AbstractProcessor;

/**
 * Links the real-time query processor to the navigation view.
 */
public class NavigationViewMapper extends
		AbstractProcessor<ModelItem, ModelItem> {

	/**
	 * Creates an array of delete operations for all specified model item types.
	 * 
	 * @param in
	 *            model item type to get operations from
	 * @return array with delete operations for the specified model item type
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Operation<ModelItem>[] transformDelete(ModelItem in) {
		final Device[] devices = Queries.getAllDevices(in);
		final Operation<ModelItem>[] opers = new Operation[devices.length];
		for (int i = 0; i < opers.length; i++) {
			opers[i] = new Delete(devices[i]);
		}
		return opers;
	}

	/**
	 * Creates an array of insert operation for all specified model item types.
	 * 
	 * @param in
	 *            model item type to get operations from
	 * @return array with insert operations for the specified model item type
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Operation<ModelItem>[] transformInsert(ModelItem in) {
		final Device[] devices = Queries.getAllDevices(in);
		final Operation<ModelItem>[] opers = new Operation[devices.length];
		for (int i = 0; i < opers.length; i++) {
			opers[i] = new Insert(devices[i]);
		}
		return opers;
	}

	/**
	 * Creates an array of update operation for all specified model item types.
	 * 
	 * @param in
	 *            model item type to get operations from
	 * @return array with update operations for the specified model item type
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Operation<ModelItem>[] transformUpdate(ModelItem in) {
		if (in instanceof Device) {
			return new Operation[] { new Update(in) };
		} else {
			return null;
		}
	}
}
