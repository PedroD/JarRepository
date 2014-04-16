package lumina.kernel.osgi.registries;

import java.util.HashMap;
import java.util.Map;

import lumina.api.properties.IPropertyType;
import lumina.kernel.osgi.factories.PropertyEditorFactory;

//TODO: Os editores não têm nada a haver com o kernel. Mudar isto assim que o SWT tiver separado.
public class PropertyEditorFactoryRegistry {
	public static final PropertyEditorFactoryRegistry DEFAULT_INSTANCE = new PropertyEditorFactoryRegistry();

	public Map<String, Map<Class<? extends IPropertyType>, PropertyEditorFactory>> propertyEditors = new HashMap<String, Map<Class<? extends IPropertyType>, PropertyEditorFactory>>();

	public void registerEditorFactory(String frontEnd,
			Class<? extends IPropertyType> propertyType,
			PropertyEditorFactory factory) {
		Map<Class<? extends IPropertyType>, PropertyEditorFactory> propertyEditorFactories = propertyEditors
				.get(frontEnd);
		if (propertyEditorFactories == null) {
			propertyEditorFactories = new HashMap<Class<? extends IPropertyType>, PropertyEditorFactory>();
			propertyEditors.put(frontEnd, propertyEditorFactories);
		}

		propertyEditorFactories.put(propertyType, factory);
	}

	public PropertyEditorFactory findEditorFactory(String frontEnd,
			Class<? extends IPropertyType> propertyType) {
		Map<Class<? extends IPropertyType>, PropertyEditorFactory> propertyEditorFactories = propertyEditors
				.get(frontEnd);
		if (propertyEditorFactories != null) {
			return propertyEditorFactories.get(propertyType);
		}
		return null;
	}
}
