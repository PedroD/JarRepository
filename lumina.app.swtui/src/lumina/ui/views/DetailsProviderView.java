package lumina.ui.views;

/**
 * The interface of views that provide detailed information about the elements.
 */
public interface DetailsProviderView {
	/**
	 * Sets the displaying of details.
	 * 
	 * @param displayDetails
	 *            indicates whether details should be displayed or not.
	 */
	void setInfoMode(final boolean displayDetails);

	/**
	 * Checks if view is in info mode.
	 * 
	 * @return <code>true</code> if the info mode is turned on;
	 *         <code>false</code> otherwise.
	 */
	boolean isInfoMode();
}
