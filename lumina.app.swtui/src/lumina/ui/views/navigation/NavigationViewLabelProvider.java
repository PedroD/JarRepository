package lumina.ui.views.navigation;

import lumina.base.model.Area;
import lumina.base.model.Device;
import lumina.base.model.DeviceStatus;
import lumina.base.model.DeviceTimer;
import lumina.base.model.DeviceType;
import lumina.base.model.Floor;
import lumina.base.model.IDeviceDriver;
import lumina.base.model.ModelItem;
import lumina.base.model.Project;
import lumina.base.model.Queries;
import lumina.base.model.validators.ValidationProblem;
import lumina.base.model.validators.ValidatorManager;
import lumina.network.gateways.api.INetworkAddress;
import lumina.qp.AggregateResult;
import lumina.qp.TableSink;
import lumina.ui.swt.CustomizableImageCache;
import lumina.ui.swt.OverlayImageIcon;
import lumina.ui.views.AbstractTreeViewLabelProvider;

import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import codebase.Strings;


/**
 * Class that implements a label provider for the TreeViewer.
 */
public class NavigationViewLabelProvider extends AbstractTreeViewLabelProvider
		implements ValidatorManager.ValidationEventListener {

	private static final String AREA_IMAGE_PATH = "/icons/model/area.gif";
	private static final String FLOOR_IMAGE_PATH = "/icons/model/floor.png";
	private static final String PROJECT_IMAGE_PATH = "/icons/model/project.gif";
	private static final String UNKNOWN_IMAGE_PATH = "/icons/model/unknown.gif";

	/**
	 * The column index of the model item name. Used in
	 * {@link #getColumnText(Object, int)}.
	 */
	private static final int ITEM_NAME_COL_INDEX = 0;

	/**
	 * The column index for the device status. Used in
	 * {@link #getColumnText(Object, int)} .
	 */
	private static final int DEVICE_STATUS_COL_INDEX = 1;

	/**
	 * The column index of the device address. Used in
	 * {@link #getColumnText(Object, int)} .
	 */
	private static final int DEVICE_ADDRESS_COL_INDEX = 2;

	/**
	 * The column index of the device timer. Used in
	 * {@link #getColumnText(Object, int)}.
	 */
	private static final int DEVICE_TIMER_COL_INDEX = 3;

	/**
	 * The column index of the current power consumption. Used in
	 * {@link #getColumnText(Object, int)}.
	 */
	private static final int POWER_COL_INDEX = 4;

	/**
	 * Image cache for the tree icons.
	 */
	private static final CustomizableImageCache IMAGE_CACHE = new CustomizableImageCache() {
		@Override
		protected Image makeDecoratedImage(Image base, Object qualifier) {
			if (qualifier == WARNING_OVERLAY) {
				final OverlayImageIcon overlayIcon = new OverlayImageIcon(base,
						WARNING_OVERLAY, OverlayImageIcon.Position.BOTTOM_LEFT);
				return overlayIcon.getImage();
			} else if (qualifier == ERROR_OVERLAY) {
				final OverlayImageIcon overlayIcon = new OverlayImageIcon(base,
						ERROR_OVERLAY, OverlayImageIcon.Position.BOTTOM_LEFT);
				return overlayIcon.getImage();
			} else if (qualifier == TIMER_ACTIVE_OVERLAY) {
				final OverlayImageIcon overlayIcon = new OverlayImageIcon(base,
						TIMER_ACTIVE_OVERLAY,
						OverlayImageIcon.Position.TOP_RIGHT);
				return overlayIcon.getImage();
			} else {
				return base;
			}
		}
	};

	private static final Image WARNING_OVERLAY = IMAGE_CACHE
			.getImage("/icons/overlays/overlay_warning.gif");

	private static final Image ERROR_OVERLAY = IMAGE_CACHE
			.getImage("/icons/overlays/overlay_error.png");

	private static final Image TIMER_ACTIVE_OVERLAY = IMAGE_CACHE
			.getImage("/icons/overlays/overlay_timer_on.gif");

	/**
	 * Decorates the item icon with the validation problem overlay.
	 * 
	 * @param baseImage
	 *            the base image icon
	 * @param element
	 *            the model item
	 * @return an image decorated with an <i>error</i> or <i>warning</i> overlay
	 */
	private Image decorateWithProblemOrValidationImage(final Image baseImage,
			final Object element) {
		if (element instanceof ModelItem) {
			final ModelItem item = (ModelItem) element;
			final Project project = Queries.getAncestorProject(item);
			if (project == null) {
				return baseImage;
			}

			final ValidatorManager modelValidator = project
					.getValidatorManager();

			final ValidationProblem firstProblem = modelValidator
					.getMostImportantProblemFor(item);

			if (firstProblem == null) {
				return baseImage;
			}

			final Image overlayedImage;
			if (firstProblem.getSeverity() == ValidationProblem.Severity.WARNING) {
				overlayedImage = IMAGE_CACHE.getDecoratedImage(baseImage,
						WARNING_OVERLAY);
			} else if (firstProblem.getSeverity() == ValidationProblem.Severity.ERROR) {
				overlayedImage = IMAGE_CACHE.getDecoratedImage(baseImage,
						ERROR_OVERLAY);
			} else {
				overlayedImage = null;
			}

			return overlayedImage;
		}

		return null;
	}

	/**
	 * Decorates the item icon with a timer overlay.
	 * 
	 * @param baseImage
	 *            the image icon
	 * @param element
	 *            the model item
	 * @return the icon decorated with an <i>active</i> or <i>inactive</i> timer
	 *         overlay
	 */
	private Image decorateWithTimerImage(final Image baseImage,
			final Object element) {
		if (element instanceof Device) {
			final Device device = (Device) element;

			if (device.getDeviceTimer() != null) {
				final Image overlayedImage;

				if (device.isDeviceTimerEnabled()) {
					return IMAGE_CACHE.getDecoratedImage(baseImage,
							TIMER_ACTIVE_OVERLAY);
				} else {
					/*
					 * We decided that when the timer is inactive we treat it as
					 * if no timer is associated. Because sometimes it is not
					 * easy to distinguish whether the timer is active or not
					 * with the timer disabled image.
					 */
					overlayedImage = baseImage;
				}
				return overlayedImage;
			}
		}

		return baseImage;
	}

	/**
	 * Terminate.
	 * <p>
	 * Clears the image cache.
	 */
	@Override
	public final void dispose() {
		IMAGE_CACHE.disposeAll();
	}

	/**
	 * Reports a problem in the navigation view.
	 * 
	 * @param item
	 *            model item
	 * @param p
	 *            validation problem
	 */
	public final void reportProblem(final ModelItem item,
			final ValidationProblem p) {
		// Logger.getInstance().log(LogService.LOG_DEBUG,"Ooops: trouble " + item.getId() + ": " +
		// p.getDescription()
		// + " : " + p.getProblemDetails());

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(new LabelProviderChangedEvent(
						NavigationViewLabelProvider.this, new Object[] { item }));
			}
		});
	}

	/**
	 * Clears a problem from the navigation view.
	 * 
	 * @param item
	 *            model item
	 * @param p
	 *            validation problem
	 */
	public final void clearProblem(final ModelItem item,
			final ValidationProblem p) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(new LabelProviderChangedEvent(
						NavigationViewLabelProvider.this, new Object[] { item }));
			}
		});
	}

	/**
	 * Returns the model item image depending on the object specified object
	 * type.
	 * 
	 * @param obj
	 *            object to get image from
	 * @return object type image of unknown if object type is other than area,
	 *         device, floor or project.
	 */
	public final Image getImage(final Object obj) {
		final String imagePath;
		if (obj instanceof Device) {
			final Device d = (Device) obj;
			final DeviceType t = d.getDeviceType();
			if (t != null) {
				imagePath = t.getDefaultIcon();
			} else {
				imagePath = UNKNOWN_IMAGE_PATH;
			}
		} else if (obj instanceof Area) {
			imagePath = AREA_IMAGE_PATH;
		} else if (obj instanceof Floor) {
			imagePath = FLOOR_IMAGE_PATH;
		} else if (obj instanceof Project) {
			imagePath = PROJECT_IMAGE_PATH;
		} else {
			imagePath = UNKNOWN_IMAGE_PATH;
		}

		return decorateImage(IMAGE_CACHE.getImage(imagePath), obj);
	}

	/**
	 * Returns an image for decoration.
	 * <p>
	 * The image depends on the status and problem existence in the specified
	 * element.
	 * 
	 * @param baseImage
	 *            default image to use
	 * @param element
	 *            element to get image
	 * @return decorate image
	 */
	public final Image decorateImage(final Image baseImage, final Object element) {
		final Image validationOverlay = decorateWithProblemOrValidationImage(
				baseImage, element);
		final Image timerOverlay = decorateWithTimerImage(validationOverlay,
				element);
		return timerOverlay;
	}

	/**
	 * Decorate text.
	 * 
	 * @param text
	 *            text
	 * @param element
	 *            element
	 * @return input text
	 */
	public final String decorateText(String text, Object element) {
		return text;
	}

	/**
	 * Method that returns the string associated with a model element.
	 * 
	 * @param element
	 *            element to get string from
	 * @return element.toString()
	 */
	public final String getText(Object element) {
		return element.toString();
	}

	/**
	 * Returns the column text depending on the element type.
	 * 
	 * @param element
	 *            element to get text from
	 * @param columnIndex
	 *            column index
	 * @return column text
	 */
	public final String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
		case ITEM_NAME_COL_INDEX:
			// model item
			return getText(element);

		case DEVICE_STATUS_COL_INDEX:
			// device status
			if (element instanceof Device) {
				final DeviceStatus status = ((Device) element).getStatus();
				if (status != null) {
					return status.asString();
				}
			}
			return "";

		case DEVICE_ADDRESS_COL_INDEX:
			// area number OR device channel number
			if (element instanceof Area) {
				return Integer.toString(((Area) element).getAreaNumber());
			} else if (element instanceof Device) {
				final IDeviceDriver driver = ((Device) element).getDriver();
				if (driver != null) {
					final INetworkAddress[] addrs = driver.getAddresses();
					return Strings.join(addrs, ",");
				}
			}
			return "";

		case DEVICE_TIMER_COL_INDEX:
			// device timer
			if (element instanceof Device) {
				final DeviceTimer timer = ((Device) element).getDeviceTimer();
				if (timer != null) {
					return timer.getName();
				}
			}
			return "";
		case POWER_COL_INDEX:
			// power consumption
			final TableSink<AggregateResult> queryResult = getQueryResult();
			if (queryResult != null) {
				final AggregateResult tuple = queryResult.get(element);
				if (tuple != null) {
					final Object[] results = tuple.getFunctionResults();
					if (results != null && results.length > 0) {
						return results[0].toString();
					}
				}
			} else {
				return "";
			}

			// if (element instanceof Device) {
			// final double consumption = ((Device) element)
			// .getPowerConsumptionEstimate();
			// return PowerEstimatorStatus.formatConsumption(consumption);
			// }

		default:
			return null;
		}
	}
}
