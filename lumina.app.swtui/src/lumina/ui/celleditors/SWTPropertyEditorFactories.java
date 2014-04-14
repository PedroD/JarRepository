package lumina.ui.celleditors;

import lumina.api.properties.IProperty;
import lumina.base.model.Floor;
import lumina.base.model.Floor.BackgroundColorPropertyType;
import lumina.base.model.Schedule.SchedulePeriodicityPropertyType;
import lumina.base.model.devices.ControlPanelDevice;
import lumina.extensions.base.properties.types.BooleanPropertyType;
import lumina.extensions.base.properties.types.ChoicePropertyType;
import lumina.extensions.base.properties.types.DatePropertyType;
import lumina.extensions.base.properties.types.FilepathPropertyType;
import lumina.extensions.base.properties.types.NumberPropertyType;
import lumina.extensions.base.properties.types.TextPropertyType;
import lumina.extensions.base.properties.types.TimePropertyType;
import lumina.kernel.osgi.factories.PropertyEditorFactory;
import lumina.kernel.osgi.registries.PropertyEditorFactoryRegistry;
import lumina.ui.swt.FileCellEditor;

import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Temporary class that adds the SWT property editors. TODO: Migrate to OSGi
 */
public class SWTPropertyEditorFactories {

	public static void registerPropertyEditorFactories() {

		// Boolean
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", BooleanPropertyType.class, new PropertyEditorFactory(
						null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						final String[] choices = (String[]) property
								.getChoices();
						if (choices != null && choices.length > 0) {
							return new BooleanCellEditorWrapper(
									new BooleanCellEditor(
											context,
											((String[]) property.getChoices())[0],
											((String[]) property.getChoices())[1],
											SWT.READ_ONLY | SWT.DROP_DOWN),
									property);
						} else {
							/*
							 * No choices found
							 */
							return new ChoiceCellEditorWrapper(
									new ComboBoxCellEditor((Composite) context,
											new String[0], SWT.READ_ONLY),
									property);
						}
					}
				});

		// Choice
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", ChoicePropertyType.class,
				new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						final String[] choices = (String[]) property
								.getChoices();

						if (choices != null && choices.length > 0) {
							return new ChoiceCellEditorWrapper(
									new ChoiceCellEditor((Composite) context,
											choices, SWT.READ_ONLY
													| SWT.DROP_DOWN), property);
						} else {
							/*
							 * No choices found
							 */
							return new ChoiceCellEditorWrapper(
									new ChoiceCellEditor((Composite) context,
											new String[0], SWT.READ_ONLY),
									property);
						}
					}
				});

		// Date
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", DatePropertyType.class, new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(new DateCellEditor(
								(Composite) context, SWT.NONE), property);
					}
				});

		// Time
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", TimePropertyType.class, new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(new TimeCellEditor(
								(Composite) context, SWT.NONE), property);
					}
				});

		// Filepath
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", FilepathPropertyType.class, new PropertyEditorFactory(
						null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(new FileCellEditor(
								(Composite) context, SWT.NONE), property);
					}
				});

		// Number
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", NumberPropertyType.class,
				new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(new TextCellEditor(
								(Composite) context), property);
					}
				});

		// Text
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", TextPropertyType.class, new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(new TextCellEditor(
								(Composite) context), property);
					}
				});

		// //////////////////////////////////////////////////////////////////////////////////////

		// Control Panel
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", ControlPanelDevice.PanelActionsPropertyType.class,
				new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(
								new ControlPanelLabelsCellEditor(
										(Composite) context, SWT.NONE),
								property);
					}
				});

		// Schedule Periodicity
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", SchedulePeriodicityPropertyType.class,
				new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(
								new SchedulePeriodicityCellEditor(
										(Composite) context, SWT.NONE),
								property);
					}
				});

		// Floor Plan
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", Floor.FloorPlanPropertyType.class,
				new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						// TODO: Floor.this
						return new SimpleCellEditorWrapper(
								new FloorPlanCellEditor((Composite) context,
										null), property);
					}
				});
		// @Override
		// public final CellEditor createCellEditor(final Composite parent) {
		// return new FloorPlanCellEditor((Composite)context, Floor.this)
		// }

		// Floor Color
		PropertyEditorFactoryRegistry.DEFAULT_INSTANCE.registerEditorFactory(
				"SWT", BackgroundColorPropertyType.class,
				new PropertyEditorFactory(null) {
					@Override
					public Object createInstance(Object... params)
							throws InstantiationException,
							IllegalAccessException {
						IProperty property = (IProperty) params[1];
						Composite context = (Composite) params[0];
						return new SimpleCellEditorWrapper(new ColorCellEditor(
								(Composite) context, SWT.NONE), property);
					}
				});

	}
}
