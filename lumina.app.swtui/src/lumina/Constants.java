package lumina;

/**
 * Project global constants.
 */
public final class Constants {

	/**
	 * Product name.
	 */
	public static final String PRODUCT_NAME = "iCANgraph"; //$NON-NLS-1$

	/**
	 * Short product name. One word only.
	 */
	public static final String APPLICATION_NAME_SHORT = PRODUCT_NAME;

	/**
	 * Product name as a sentence, used for OS registration.
	 */
	public static final String APPLICATION_NAME_LONG = "iCANGraph real-time lighting control software"; //$NON-NLS-1$

	/**
	 * Project file extension name, specifies the name of the project file
	 * extension.
	 */
	public static final String PROJECT_FILE_EXTENSION_NAME = "lff"; //$NON-NLS-1$

	/**
	 * Project file extension, specifies the extension of the project file.
	 */
	public static final String PROJECT_FILE_EXTENSION = ".lff"; //$NON-NLS-1$

	/**
	 * Report file filter, specifies the report file extension filter.
	 */
	public static final String REPORT_FILE_EXTENSION = ".xls"; //$NON-NLS-1$

	/**
	 * Project file filter, specifies the project file extension filter.
	 */
	public static final String PROJECT_FILE_EXTENSION_FILTER = "*.lff"; //$NON-NLS-1$

	/**
	 * Report file filter, specifies the report file extension filter.
	 */
	public static final String REPORT_FILE_EXTENSION_FILTER = "*.xls"; //$NON-NLS-1$

	/**
	 * All files filter, specifies the file filter for all files.
	 */
	public static final String ALL_FILE_EXTENSIONS = "*.*"; //$NON-NLS-1$

	/**
	 * String returned by getNamespace() used by menu factories.
	 * <p>
	 * Note: If a name space is not provided the menu factories will fail on
	 * exit will fail with a {@link NullPointerException}.
	 */
	public static final String APPLICATION_NAMESPACE = "Lumina"; //$NON-NLS-1$

	/**
	 * The path to the application icon. For example, depending on the operating
	 * system, we may need bigger icons.
	 */
	// FIXME: this must get more generic to work with icons of different size.
	public static final String APPLICATION_ICON_PATH = "/icons/application/icangraph-16-transparent.gif"; //$NON-NLS-1$

	/**
	 * Prevents instantiation.
	 */
	private Constants() {
	}
}
