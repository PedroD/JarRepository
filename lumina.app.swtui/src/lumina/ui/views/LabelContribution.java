package lumina.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

/**
 * A contribution for a label.
 * 
 * @author Paulo Carreira
 */
public class LabelContribution extends WorkbenchWindowControlContribution {

	/**
	 * The ID of the label contribution.
	 */
	private static final String ID = "lumina.ui.views.contributions.label";

	private Composite composite;
	private Label label;

	/**
	 * Constructor.
	 */
	public LabelContribution() {
		super(ID);
	}

	/**
	 * Creates the GUI control.
	 * 
	 * @param parent
	 *            parent component
	 * @return label composite
	 */
	protected Control createControl(final Composite parent) {
		composite = parent;
		final Composite labelComposite = new Composite(parent, SWT.NONE);

		labelComposite.setLayout(new GridLayout(2, false));
		// ((GridLayout) labelComposite.getLayout()).marginHeight = 2;
		// ((GridLayout) labelComposite.getLayout()).marginTop = 0;

		label = new Label(labelComposite, SWT.NONE);
		label.setText("                      ");

		labelComposite.pack();
		return labelComposite;
	}

	/**
	 * Sets the label.
	 * 
	 * @param s
	 *            label text
	 */
	public final void setLabel(final String s) {
		assert s != null;

		if (s.length() == 0) {
			label.setText("");
			label.setToolTipText("");
		} else {
			label.setText(s);

			final String toolTip = label.getToolTipText();
			final boolean mustSetTooltip = toolTip != null
					&& toolTip.length() == 0;
			if (mustSetTooltip) {
				label.setToolTipText(Messages
						.getString("LabelContribution.toolTipText")); // NON-NLS-1
			}

			label.pack(true);
			composite.layout(true, true);

		}
	}
}
