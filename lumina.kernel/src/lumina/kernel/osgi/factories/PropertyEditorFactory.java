package lumina.kernel.osgi.factories;

import lumina.api.properties.IPropertyEditor;



/**
 * A factory for creating PropertyEditor objects.
 */
public class PropertyEditorFactory extends
		AbstractExtensionFactory<IPropertyEditor> {

	/**
	 * Instantiates a new property editor factory.
	 *
	 * @param serviceClass the service class
	 */
	public PropertyEditorFactory(Class<IPropertyEditor> serviceClass) {
		super(serviceClass);
	}
}
