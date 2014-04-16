package lumina.kernel.osgi;

import javax.naming.OperationNotSupportedException;

/**
 * Defines a lumina object that can be extended through the OSGi service layer.
 * Framework
 */
public interface LuminaExtension {

	/**
	 * Returns an unique identifier for this OSGi extension.
	 * 
	 * @throws OperationNotSupportedException
	 */
	public String getExtensionId();
}
