package lumina.ui.views.blueprint;

import lumina.ui.views.blueprint.actions.ZoomLevelCombo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * Dropdown used to set the zoom levels.
 * <p>
 * Widgets appearing on the top left corner of the views are
 * {@link WorkbenchWindowControlContribution}s.
 */
public class ZoomComboContribution extends WorkbenchWindowControlContribution {

	/**
	 * The contribution ID.
	 */
	public static final String ID = "lumina.ui.views.blueprint.contributions.zoomLevelCombo";

	/**
	 * The combo-box object.
	 */
	private ZoomLevelCombo zoomLevelCombo;

	public ZoomComboContribution() {
		super(ID);
	}

	/**
	 * Creates a composite with a {@link ZoomLevelCombo} widget.
	 * <p>
	 * The widget is disabled by default.
	 * 
	 * @param parent
	 *            the parent composite
	 * @return the control object
	 */
	@Override
	protected Control createControl(Composite parent) {
		final Composite zoomComposite = new Composite(parent, SWT.NONE);

		final GridLayout layout = new GridLayout(2, false);
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);

		layout.marginHeight = 2;
		layout.marginTop = 0;

		zoomComposite.setLayout(layout);
		zoomComposite.setLayoutData(layoutData);

		zoomLevelCombo = new ZoomLevelCombo(zoomComposite, SWT.NONE);
		zoomLevelCombo.setEnabled(false);

		zoomComposite.pack();

		return zoomComposite;
	}

	/**
	 * Enables/diables the control.
	 * 
	 * @param enabled
	 *            the enabled status; set to <code>true</code> to enable, set to
	 *            <code>false</code> to disable.
	 */
	public final void setEnabled(final boolean enabled) {
		if (zoomLevelCombo != null) {
			zoomLevelCombo.setEnabled(enabled);
		}
	}

	/**
	 * Set the zoom level based on the zoom scale.
	 * 
	 * @param ratio
	 *            the zoom scale.
	 */
	public final void updateZoom(final double ratio) {
		if (zoomLevelCombo != null) {
			zoomLevelCombo.setZoom(ratio);
		}
	}

	/**
	 * Sets the client to be notified when the zoom level changes.
	 * 
	 * @param client
	 *            the zoom client to be notified
	 */
	public final void setZoomClient(final ZoomLevelCombo.Zoomable client) {
		if (zoomLevelCombo != null) {
			zoomLevelCombo.setZoomClient(client);
		}
	}
}
