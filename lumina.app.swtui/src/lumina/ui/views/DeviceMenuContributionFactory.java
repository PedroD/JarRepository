package lumina.ui.views;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lumina.Constants;
import lumina.base.model.Device;
import lumina.ui.jface.SelectionUtils;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Device menu contribution factory.
 * <p>
 * Factory with menu contribution from devices.
 */
public final class DeviceMenuContributionFactory extends
		AbstractContributionFactory implements ISelectionListener {

	/**
	 * Device menu contribution.
	 */
	private static final class DeviceMenuContribution extends ContributionItem {
		/**
		 * Last menu parent set by {@link #fill(Menu, int)}.
		 */
		private Menu lastMenu;

		/**
		 * Last device set by {@link #updateDevice(Device)}.
		 */
		private Device lastDevice;

		/**
		 * Set the original set of items in the menu before the add.
		 */
		private MenuItem[] lastAddedItems;

		/**
		 * Indicates if the contribution item has been disposed.
		 */
		private boolean isDisposed;

		private MenuItem[] getNewItems(final MenuItem[] oldItems,
				final MenuItem[] allItems) {
			final Set<MenuItem> old = new HashSet<MenuItem>();

			for (MenuItem i : oldItems) {
				old.add(i);
			}

			final List<MenuItem> result = new LinkedList<MenuItem>();
			for (MenuItem i : allItems) {
				if (!old.contains(i)) {
					result.add(i);
				}
			}

			return result.toArray(new MenuItem[0]);
		}

		private void removeMenuItems(final Menu menu, final MenuItem[] items) {
			if (menu != null && !menu.isDisposed()) {
				final Set<MenuItem> existingItems = new HashSet<MenuItem>();
				for (MenuItem i : menu.getItems()) {
					existingItems.add(i);
				}

				for (MenuItem i : items) {
					if (existingItems.contains(i)) {
						i.dispose();
					}
				}
			}
		}

		void updateDeviceInternal() {
			removeLast();

			if (lastMenu != null && !lastMenu.isDisposed()) {
				if (lastDevice != null) {
					final MenuItem[] existingItems = lastMenu.getItems();
					lastDevice.getStatus().getContributionContextMenu(lastMenu);
					lastAddedItems = getNewItems(existingItems,
							lastMenu.getItems());
				}
			}
		}

		/**
		 * Removes the last.
		 */
		public synchronized void removeLast() {
			if (lastMenu != null) {
				if (lastAddedItems != null) {
					removeMenuItems(lastMenu, lastAddedItems);
					lastAddedItems = null;
				}
			}
		}

		/**
		 * Fills the menu.
		 * 
		 * @param menu
		 *            menu
		 * @param index
		 *            index
		 */
		@Override
		public synchronized void fill(Menu menu, int index) {
			super.fill(menu, index);
			lastMenu = menu;
			updateDeviceInternal();
		}

		/**
		 * Disposes dependents and sets disposed status to true.
		 */
		@Override
		public void dispose() {
			removeLast();
			super.dispose();
			isDisposed = true;
		}

		/**
		 * Checks if it has been disposed.
		 * 
		 * @return disposed status.
		 */
		public boolean isDisposed() {
			return isDisposed;
		}

		/**
		 * Updates the device.
		 * 
		 * @param d
		 *            device to update
		 */
		public synchronized void updateDevice(final Device d) {
			if (lastDevice != d) {
				lastDevice = d;
				updateDeviceInternal();
			}
		}
	};

	/**
	 * The menu contribution item object.
	 */
	private DeviceMenuContribution contributionItem;

	/**
	 * Builds a new factory at a give location.
	 * 
	 * @param location
	 *            the URI where to add the menu options
	 */
	public DeviceMenuContributionFactory(String location) {
		super(location, Constants.APPLICATION_NAMESPACE);
		contributionItem = new DeviceMenuContribution();
	}

	/**
	 * Creates the contribution items.
	 * 
	 * @param serviceLocator
	 *            service locator
	 * @param additions
	 *            contributions
	 */
	public void createContributionItems(final IServiceLocator serviceLocator,
			IContributionRoot additions) {
		// final IWorkbenchWindow window = PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow();

		// if (window != null) {
		// final ISelectionService selectionService =
		// window.getSelectionService();
		//
		// if (selectionService != null) {
		// selectionService.addPostSelectionListener(this);
		// Logger.getInstance().log(LogService.LOG_DEBUG,"** Menu command listener registered ***");
		// }
		//
		// }

		additions.addContributionItem(contributionItem, null);
	}

	/**
	 * Checks if a valid device was chosen uppon selection change.
	 * 
	 * @param part
	 *            workbench part
	 * @param selection
	 *            new selection
	 */
	public void selectionChanged(final IWorkbenchPart part,
			final ISelection selection) {
		// if (part == parentView) {
		if (contributionItem != null && !contributionItem.isDisposed()) {
			final Object[] selectedObjects = SelectionUtils
					.getSelection(selection);
			if (selectedObjects != null && selectedObjects.length == 1
					&& selectedObjects[0] instanceof Device) {
				final Device d = (Device) selectedObjects[0];
				contributionItem.updateDevice(d);
			} else {
				contributionItem.updateDevice(null);
				contributionItem.removeLast();
			}
		}
		// }
	}
}
