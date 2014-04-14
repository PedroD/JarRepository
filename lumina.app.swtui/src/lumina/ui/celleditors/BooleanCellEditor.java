package lumina.ui.celleditors;

import java.text.MessageFormat;

import lumina.ui.swt.SWTUtils;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor for boolean properties that presents a check-box.
 * <p>
 * This class may be instantiated; it is not intended to be sub-classed.
 */
public class BooleanCellEditor extends CellEditor {

	/**
	 * Default SWT ComboBoxCellEditor style.
	 */
	private static final int DEFAULT_STYLE = SWT.CHECK | SWT.FLAT | SWT.DOWN;

	/**
	 * Minimum width for the layout.
	 */
	private static final int MIN_WIDTH = 60;

	/**
	 * The checkbox control, or <code>null</code> if none.
	 */
	private Button checkBox;

	/**
	 * The previously selected, or "before", value.
	 */
	private boolean wasSelected;

	/**
	 * The text to be displayed when checked.
	 */
	private final String checkedChoiceText;

	/**
	 * The text to be displayed when not checked.
	 */
	private final String uncheckedChoiceText;

	/**
	 * Creates a new cell editor with a check box and two choices with the
	 * default style.
	 * 
	 * @param parent
	 *            the parent control
	 * @param checked
	 *            the text to be displayed when checked, can be
	 *            <code>null</code>
	 * @param unchecked
	 *            the text to be displayed not checked, can be <code>null</code>
	 */
	public BooleanCellEditor(final Composite parent, final String checked,
			final String unchecked) {
		this(parent, checked, unchecked, DEFAULT_STYLE);
	}

	/**
	 * Creates a new cell editor with a check box containing the given style.
	 * 
	 * @param parent
	 *            the parent control
	 * @param checked
	 *            the text to be displayed when checked, can be
	 *            <code>null</code>
	 * @param unchecked
	 *            the text to be displayed not checked, can be <code>null</code>
	 * @param style
	 *            the style bits
	 */
	public BooleanCellEditor(final Composite parent, final String checked,
			final String unchecked, int style) {
		super(parent, style);
		checkedChoiceText = checked;
		uncheckedChoiceText = unchecked;
	}

	private static String safeString(final String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

	private void updateCheckBoxLabelInternal() {
		if (checkBox != null) {
			if (checkBox.getSelection()) {
				checkBox.setText(safeString(checkedChoiceText));
			} else {
				checkBox.setText(safeString(uncheckedChoiceText));
			}
		}
	}

	/**
	 * Applies the currently selected value and deactivates the cell editor.
	 */
	private void applyEditorValueAndDeactivate() {
		// must set the selection before getting value
		wasSelected = checkBox.getSelection();
		final Object newValue = doGetValue();
		markDirty();

		final boolean isValid = isCorrect(newValue);
		setValueValid(isValid);

		if (!isValid) {
			setErrorMessage(MessageFormat.format(getErrorMessage(), newValue));
		}

		fireApplyEditorValue();
		deactivate();
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected Control createControl(final Composite parent) {
		if (checkBox == null) {
			checkBox = new Button(parent, DEFAULT_STYLE);
			checkBox.setFont(parent.getFont());

			checkBox.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					final boolean isSelected = checkBox.getSelection();
					updateCheckBoxLabelInternal();
					/*
					 * An eclipse bug in the CellEditorListener created in
					 * ColumnViewerEditor.initCellEditorListener is preventing
					 * this method form
					 */
					valueChanged(wasSelected, isSelected);
					wasSelected = isSelected;
				}
			});

			checkBox.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					checkBox = null;
				}
			});

			checkBox.addKeyListener(new KeyAdapter() {
				// hook key pressed - see PR 14201
				public void keyPressed(KeyEvent e) {
					keyReleaseOccured(e);
				}
			});

			checkBox.addTraverseListener(new TraverseListener() {
				public void keyTraversed(TraverseEvent e) {
					if (e.detail == SWT.TRAVERSE_ESCAPE
							|| e.detail == SWT.TRAVERSE_RETURN) {
						e.doit = false;
					}
				}
			});

			checkBox.addFocusListener(new FocusAdapter() {
				public void focusLost(FocusEvent e) {
					BooleanCellEditor.this.focusLost();
				}
			});
		}
		return checkBox;
	}

	/**
	 * The <code>ComboBoxCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method returns the zero-based index of
	 * the current selection.
	 * 
	 * @return the zero-based index of the current selection wrapped as an
	 *         <code>Integer</code>
	 */
	protected Object doGetValue() {
		return Boolean.valueOf(checkBox.getSelection());
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected void doSetFocus() {
		checkBox.setFocus();
	}

	/**
	 * Sets the value to be represented by the <code>CheckBoxEditor</code>.
	 * 
	 * @param value
	 *            a {@link Boolean} value
	 */
	protected void doSetValue(Object value) {
		assert checkBox != null && (value instanceof Boolean);
		final Boolean boolValue = (Boolean) value;
		checkBox.setSelection(boolValue);
		updateCheckBoxLabelInternal();
		wasSelected = boolValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#focusLost()
	 */
	protected void focusLost() {
		if (isActivated()) {
			applyEditorValueAndDeactivate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt
	 * .events.KeyEvent )
	 */
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\u001b') { // Escape character
			fireCancelEditor();
		} else if (keyEvent.character == '\t') { // tab key
			applyEditorValueAndDeactivate();
		}
	}

	/**
	 * Gets the layout data of the cell editor.
	 * 
	 * @return the layout data object.
	 */
	public LayoutData getLayoutData() {
		final LayoutData layoutData = super.getLayoutData();
		if ((checkBox == null) || checkBox.isDisposed()) {
			layoutData.minimumWidth = MIN_WIDTH;
		} else {
			layoutData.minimumWidth = Math.max(
					MIN_WIDTH,
					SWTUtils.getMaxStringDimensions(checkBox, new String[] {
							safeString(checkedChoiceText),
							safeString(uncheckedChoiceText) }).x);
		}

		return layoutData;
	}

	/**
	 * Returns this field editor's current value.
	 * 
	 * @return the value
	 */
	public boolean getBooleanValue() {
		return checkBox.getSelection();
	}
}
