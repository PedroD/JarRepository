package lumina.ui.views.timer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lumina.base.model.DeviceTimer;
import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.Project;
import lumina.base.model.Queries;
import lumina.base.model.Schedule;
import lumina.base.model.Task;
import lumina.base.model.commands.DeviceCommand;
import lumina.base.model.validators.ValidationProblem;
import lumina.base.model.validators.ValidatorManager;
import lumina.ui.swt.ApplicationImageCache;
import lumina.ui.swt.CustomizableImageCache;
import lumina.ui.swt.OverlayImageIcon;
import lumina.ui.views.AbstractTreeViewLabelProvider;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Class that implements a label provider for the
 * {@link org.eclipse.jface.viewers.TreeViewer}.
 */
public class TimerViewLabelProvider extends AbstractTreeViewLabelProvider
		implements ValidatorManager.ValidationEventListener,
		ITableColorProvider {

	private static final String UNDEFINED_NEXT_FIRE_TIME = "-";

	/**
	 * The color of read-only (disabled properties).
	 */
	private static final int PROPERTY_READ_ONLY_COLOR = SWT.COLOR_DARK_GRAY;

	/**
	 * Relative path of the project icon.
	 */
	private static final String PROJECT_IMAGE_PATH = "/icons/model/project.gif";

	/**
	 * Path of the icon to be displayed when the model is unknown.
	 */
	private static final String UNKNOWN_IMAGE_PATH = "/icons/model/unknown.gif";

	/**
	 * Warning overlay image.
	 */
	private static final Image WARNING_OVERLAY = ApplicationImageCache
			.getInstance().getImage("/icons/overlays/overlay_warning.gif");
	/**
	 * Error overlay image.
	 */
	private static final Image ERROR_OVERLAY = ApplicationImageCache
			.getInstance().getImage("/icons/overlays/overlay_warning.gif");
	/**
	 * Pause overlay image.
	 */
	private static final Image PAUSE_OVERLAY = ApplicationImageCache
			.getInstance().getImage("/icons/overlays/overlay_pause.png");
	/**
	 * Forbiden overlay image.
	 */
	private static final Image FORBIDEN_OVERLAY = ApplicationImageCache
			.getInstance().getImage("/icons/overlays/overlay_forbiden.png");

	/**
	 * The date format for the next fire.
	 */
	private static final DateFormat NEXT_FIRE_DAY_FORMAT;

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
			} else if (qualifier == PAUSE_OVERLAY) {
				final OverlayImageIcon overlayIcon = new OverlayImageIcon(base,
						PAUSE_OVERLAY, OverlayImageIcon.Position.BOTTOM_RIGHT);
				return overlayIcon.getImage();
			} else if (qualifier == FORBIDEN_OVERLAY) {
				final OverlayImageIcon overlayIcon = new OverlayImageIcon(base,
						FORBIDEN_OVERLAY,
						OverlayImageIcon.Position.BOTTOM_RIGHT);
				return overlayIcon.getImage();
			} else {
				return base;
			}
		}
	};

	/**
	 * The column index of the model item name. Used in
	 * {@link #getColumnText(Object, int)}.
	 */
	private static final int ITEM_NAME_COL_INDEX = 0;

	/**
	 * The column index of the next fire time. Used in
	 * {@link #getColumnText(Object, int)} .
	 */
	private static final int NEXT_FIRE_TIME_COL_INDEX = 1;

	/**
	 * The column index of the task action text. Used in
	 * {@link #getColumnText(Object, int)}.
	 */
	private static final int TASK_ACTION_COL_INDEX = 2;

	/**
	 * The column index of the schedule periodicity. Used in
	 * {@link #getColumnText(Object, int)}.
	 */
	private static final int SCHEDULE_PERIODICITY_COL_INDEX = 3;

	/**
	 * Static initializer that sets locale formats.
	 */
	static {
		final String language = Platform.getNL();
		Locale locale;
		try {
			locale = new Locale(language);
		} catch (NullPointerException e) {
			locale = null;
		}

		if (locale != null) {
			NEXT_FIRE_DAY_FORMAT = DateFormat.getDateInstance(
					DateFormat.MEDIUM, locale);
		} else {
			NEXT_FIRE_DAY_FORMAT = new SimpleDateFormat("EEE, MMM d yyyy"); // $NON-NLS-1$
		}
	}

	/**
	 * String displayed when no action is assigned to the task.
	 */
	private static final String NO_ACTION = UNDEFINED_NEXT_FIRE_TIME;

	/**
	 * The read only color object. Initialized in
	 * {@link #createPartControl(org.eclipse.swt.widgets.Composite)}.
	 */
	private Color readOnlyColor = Display.getCurrent().getSystemColor(
			PROPERTY_READ_ONLY_COLOR);

	/**
	 * Returns the background color.
	 * 
	 * @param element
	 *            object
	 * @param columnIndex
	 *            column index
	 * @return null
	 */
	public Color getBackground(Object element, int columnIndex) {
		return null;
	}

	/**
	 * Checks if a given model item is enabled or not.
	 * <p>
	 * Model items can be of type timer, schedule or task.
	 * 
	 * @param item
	 *            model item to check enable status
	 * @return false if the item is not enabled, true otherwise
	 */
	public static final boolean isEnabled(final ModelItem item) {
		if (ModelUtils.isTimerItem(item)) {
			if (item instanceof DeviceTimer) {
				final DeviceTimer dt = (DeviceTimer) item;
				return dt.isTimerEnabled();
			}

			if (item instanceof Schedule) {
				final Schedule s = (Schedule) item;
				return s.isScheduleEnabled();
			}

			if (item instanceof Task) {
				final Task t = (Task) item;
				return t.isTaskEnabled();
			}
		}

		return true;
	}

	/**
	 * Checks if an item can be shown as enabled.
	 * <p>
	 * An item can be shown as enabled if it is enabled and all its descendants
	 * are enabled.
	 * 
	 * @param item
	 *            model item
	 * @return false if item or item descendant is not enabled, true otherwise
	 */
	public static final boolean canShowEnabled(final ModelItem item) {
		if (ModelUtils.isTimerItem(item)) {
			if (item instanceof DeviceTimer) {
				final DeviceTimer dt = (DeviceTimer) item;
				return dt.isTimerEnabled();
			}

			if (item instanceof Schedule) {
				final Schedule s = (Schedule) item;
				return s.isScheduleEnabled()
						&& canShowEnabled(s.getParentTimer());
			}

			if (item instanceof Task) {
				final Task t = (Task) item;
				return t.isTaskEnabled()
						&& canShowEnabled(t.getParentSchedule());
			}
		}

		return true;
	}

	/**
	 * Returns the foreground color of a given timer item.
	 * <p>
	 * If the element is a model item timer and if it cannot be shown the read
	 * only color will be returned. Otherwise, null will be returned.
	 * 
	 * @param element
	 *            object
	 * @param columnIndex
	 *            column index
	 * @return read only color if element cannot be shown enabled, null
	 *         otherwise
	 * @see #canShowEnabled(ModelItem)
	 */
	public Color getForeground(Object element, int columnIndex) {
		if (ModelUtils.isTimerItem(element)) {
			final ModelItem item = (ModelItem) element;
			if (!canShowEnabled(item)) {
				return readOnlyColor;
			}
		}

		return null;
	}

	/**
	 * Terminates.
	 * <p>
	 * Disposes all image caches.
	 */
	@Override
	public void dispose() {
		IMAGE_CACHE.disposeAll();
	}

	/**
	 * Report a problem.
	 * <p>
	 * Triggers a label provider change with a label provider change event for
	 * the specified item.
	 * 
	 * @param item
	 *            model item that has changed
	 * @param p
	 *            validation problem
	 * @see #fireLabelProviderChanged(LabelProviderChangedEvent)
	 * @see LabelProviderChangedEvent
	 */
	public void reportProblem(final ModelItem item, final ValidationProblem p) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(new LabelProviderChangedEvent(
						TimerViewLabelProvider.this, new Object[] { item }));
			}
		});
	}

	/**
	 * Clears the problem.
	 * <p>
	 * Triggers a label provider change with a label provider change event for
	 * the specified item.
	 * 
	 * @param item
	 *            model item that has changed
	 * @param p
	 *            validation problem
	 * @see #fireLabelProviderChanged(LabelProviderChangedEvent)
	 * @see LabelProviderChangedEvent
	 */
	public void clearProblem(final ModelItem item, final ValidationProblem p) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(new LabelProviderChangedEvent(
						TimerViewLabelProvider.this, new Object[] { item }));
			}
		});
	}

	/**
	 * Gets the model item image.
	 * <p>
	 * Checks the specified object class type and returns the corresponding
	 * image. If object belongs to an unidentified class, and unknown image will
	 * be returned.
	 * 
	 * @param obj
	 *            object to get image from
	 * @return image
	 */
	public Image getImage(final Object obj) {
		final String imagePath;

		if (obj instanceof Project) {
			imagePath = PROJECT_IMAGE_PATH;
		} else if (obj instanceof DeviceTimer) {
			imagePath = DeviceTimer.DEVICE_TIMER_ICON_PATH;
		} else if (obj instanceof Schedule) {
			imagePath = Schedule.SCHEDULE_ICON_PATH;
		} else if (obj instanceof Task) {
			imagePath = Task.TASK_ICON_PATH;
		} else {
			imagePath = UNKNOWN_IMAGE_PATH;
		}

		return decorateImage(
				ApplicationImageCache.getInstance().getImage(imagePath), obj);
	}

	/**
	 * Decorates the image of the tree item icon.
	 * <ul>
	 * <li>If the element is not a model item, null will be returned.</li>
	 * <li>If the most important problem found is not an error or warning, null
	 * will be returned</li>
	 * <li>If the element is a device timer that is disabled, a pause image will
	 * be returned</li>
	 * <li>Otherwise if the element is not enabled, a forbidden image will be
	 * returned</li>
	 * </ul>
	 * 
	 * @param baseImage
	 *            default image to return
	 * @param element
	 *            object
	 * @return image or null if element is not a model item
	 */
	public Image decorateImage(final Image baseImage, final Object element) {
		if (element instanceof ModelItem) {
			final ModelItem item = (ModelItem) element;
			final Project project = Queries.getAncestorProject(item);

			final ValidatorManager modelValidator = project
					.getValidatorManager();

			final ValidationProblem firstProblem = modelValidator
					.getMostImportantProblemFor(item);

			if (firstProblem != null) {
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

			if (element instanceof DeviceTimer) {
				if (!isEnabled((ModelItem) element)) {
					return IMAGE_CACHE.getDecoratedImage(baseImage,
							PAUSE_OVERLAY);
				}
			} else {
				if (!isEnabled((ModelItem) element)) {
					return IMAGE_CACHE.getDecoratedImage(baseImage,
							FORBIDEN_OVERLAY);
				}
			}

			return baseImage;
		}

		return null;
	}

	/**
	 * Decorates the text of an Object.
	 * <p>
	 * Decoration only returns the specified input text.
	 * 
	 * @param text
	 *            text
	 * @param element
	 *            object
	 * @return text
	 */
	public String decorateText(String text, Object element) {
		return text;
	}

	/**
	 * Returns the text of an object by using .toString() method.
	 * 
	 * @param obj
	 *            object
	 * @return obj.toString()
	 */
	public String getText(Object obj) {
		return obj.toString();
	}

	/**
	 * Returns the text to apply to the specified column.
	 * <p>
	 * This method is responsible for returning strings that render when
	 * detailed information for the timer view is turned on.
	 * 
	 * @param element
	 *            object
	 * @param columnIndex
	 *            column index
	 * @return the column text
	 */
	public synchronized String getColumnText(Object element, int columnIndex) {
		/*
		 * This method has to be synchronized due to known limitations of the
		 * DateFormat class.
		 */
		switch (columnIndex) {
		case ITEM_NAME_COL_INDEX:
			// model item
			return getText(element);

		case NEXT_FIRE_TIME_COL_INDEX:
			// task next fire time
			if (element instanceof Task) {
				final Date dt = ((Task) element).getNextFireTime();
				if (dt != null) {
					final DateFormat formatDay = new SimpleDateFormat("HH:mm");
					return formatDay.format(dt);
				} else {
					return UNDEFINED_NEXT_FIRE_TIME;
				}
			} else {
				return "";
			}

		case TASK_ACTION_COL_INDEX:
			// task action
			if (element instanceof Task) {
				final DeviceCommand cmd = ((Task) element).getTaskCommand();
				if (cmd != null) {
					return cmd.getDescription();
				} else {
					return NO_ACTION;
				}
			} else {
				return "";
			}

		case SCHEDULE_PERIODICITY_COL_INDEX:
			// schedule periodicity OR task next fire date
			if (element instanceof Schedule) {
				return ((Schedule) element).getPeriodicity()
						.getValueDescriptionAbbrev();
			} else if (element instanceof Task) {
				final Date dt = ((Task) element).getNextFireTime();
				if (dt != null) {
					return NEXT_FIRE_DAY_FORMAT.format(dt);
				} else {
					return UNDEFINED_NEXT_FIRE_TIME;
				}
			} else {
				return "";
			}

		default:
			return null;
		}
	}
}
