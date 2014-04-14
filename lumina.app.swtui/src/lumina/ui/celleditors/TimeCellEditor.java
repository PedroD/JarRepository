package lumina.ui.celleditors;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.nebula.widgets.formattedtext.DateTimeFormatter;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A cell editor that manages a text entry field. The cell editor's value is the
 * text string itself.
 * <p>
 * This class may be instantiated or subclassed.
 * </p>
 * 
 * @author João Sequeira
 */
public class TimeCellEditor extends CellEditor {

	/**
	 * The Formatted text control; initially <code>00:00</code>.
	 */
	protected FormattedText text;

	private ModifyListener modifyListener;

	/**
	 * State information for updating action enablement.
	 */
	private boolean isSelection = false;

	private boolean isDeleteable = false;

	private boolean isSelectable = false;

	/**
	 * Default TextCellEditor style specify no borders on text widget as cell
	 * outline in table already provides the look of a border.
	 */
	private static final int defaultStyle = SWT.SINGLE;

	/**
	 * Creates a new text string cell editor with no control The cell editor
	 * value is the string itself, which is initially the empty string.
	 * Initially, the cell editor has no cell validator.
	 * 
	 * @since 2.1
	 */
	public TimeCellEditor() {
		setStyle(defaultStyle);
	}

	/**
	 * Creates a new text string cell editor parented under the given control.
	 * The cell editor value is the string itself, which is initially the empty
	 * string. Initially, the cell editor has no cell validator.
	 * 
	 * @param parent
	 *            the parent control
	 */
	public TimeCellEditor(Composite parent) {
		this(parent, defaultStyle);
	}

	/**
	 * Creates a new text string cell editor parented under the given control.
	 * The cell editor value is the string itself, which is initially the empty
	 * string. Initially, the cell editor has no cell validator.
	 * 
	 * @param parent
	 *            the parent control
	 * @param style
	 *            the style bits
	 * @since 2.1
	 */
	public TimeCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	/**
	 * Checks to see if the "deletable" state (can delete/ nothing to delete)
	 * has changed and if so fire an enablement changed notification.
	 */
	private void checkDeleteable() {
		boolean oldIsDeleteable = isDeleteable;
		isDeleteable = isDeleteEnabled();
		if (oldIsDeleteable != isDeleteable) {
			fireEnablementChanged(DELETE);
		}
	}

	/**
	 * Checks to see if the "selectable" state (can select) has changed and if
	 * so fire an enablement changed notification.
	 */
	private void checkSelectable() {
		boolean oldIsSelectable = isSelectable;
		isSelectable = isSelectAllEnabled();
		if (oldIsSelectable != isSelectable) {
			fireEnablementChanged(SELECT_ALL);
		}
	}

	/**
	 * Checks to see if the selection state (selection / no selection) has
	 * changed and if so fire an enablement changed notification.
	 */
	private void checkSelection() {
		boolean oldIsSelection = isSelection;
		isSelection = text.getControl().getSelectionCount() > 0;
		if (oldIsSelection != isSelection) {
			fireEnablementChanged(COPY);
			fireEnablementChanged(CUT);
		}
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected Control createControl(Composite parent) {
		text = new FormattedText(parent, getStyle());
		text.setFormatter(new DateTimeFormatter("HH:mm"));

		text.getControl().addSelectionListener(new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				handleDefaultSelection(e);
			}
		});
		text.getControl().addKeyListener(new KeyAdapter() {
			// hook key pressed - see PR 14201
			public void keyPressed(KeyEvent e) {
				keyReleaseOccured(e);

				// as a result of processing the above call, clients may have
				// disposed this cell editor
				if ((getControl() == null) || getControl().isDisposed()) {
					return;
				}
				checkSelection(); // see explanation below
				checkDeleteable();
				checkSelectable();
			}
		});
		text.getControl().addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_ESCAPE
						|| e.detail == SWT.TRAVERSE_RETURN) {
					e.doit = false;
				}
			}
		});
		// We really want a selection listener but it is not supported so we
		// use a key listener and a mouse listener to know when selection
		// changes
		// may have occurred
		text.getControl().addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				checkSelection();
				checkDeleteable();
				checkSelectable();
			}
		});
		text.getControl().addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				TimeCellEditor.this.focusLost();
			}
		});
		text.getControl().setFont(parent.getFont());
		text.getControl().setBackground(parent.getBackground());
		text.getControl().setText(":");//$NON-NLS-1$
		text.getControl().addModifyListener(getModifyListener());
		return text.getControl();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method returns the text string.
	 * 
	 * @return the text string
	 */
	protected Object doGetValue() {

		String time;

		if (text.getControl().getText().equals("  :  ")) {
			time = "00:00";
		} else {
			time = text.getValue().toString().substring(11, 16);
		}
		return time;
	}

	/*
	 * (non-Javadoc) Method declared on CellEditor.
	 */
	protected void doSetFocus() {
		if (text.getControl() != null) {
			text.getControl().selectAll();
			text.getControl().setFocus();
			checkSelection();
			checkDeleteable();
			checkSelectable();
		}
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> framework method accepts a text string (type
	 * <code>String</code>).
	 * 
	 * @param value
	 *            a text string (type <code>String</code>)
	 */
	protected void doSetValue(Object value) {
		Assert.isTrue(text.getControl() != null && (value instanceof String));
		text.setFormatter(new DateTimeFormatter("HH:mm"));
		text.getControl().setText((String) value);
	}

	/**
	 * Processes a modify event that occurred in this text cell editor. This
	 * framework method performs validation and sets the error message
	 * accordingly, and then reports a change via
	 * <code>fireEditorValueChanged</code>. Subclasses should call this method
	 * at appropriate times. Subclasses may extend or reimplement.
	 * 
	 * @param e
	 *            the SWT modify event
	 */
	protected void editOccured(ModifyEvent e) {
		String value = text.getControl().getText();
		if (value == null) {
			value = "";//$NON-NLS-1$
		}
		Object typedValue = value;
		boolean oldValidState = isValueValid();
		boolean newValidState = isCorrect(typedValue);
		if (!newValidState) {
			// try to insert the current value into the error message.
			setErrorMessage(MessageFormat.format(getErrorMessage(),
					new Object[] { value }));
		}
		valueChanged(oldValidState, newValidState);
	}

	/**
	 * Since a text editor field is scrollable we don't set a minimumSize.
	 */
	public LayoutData getLayoutData() {
		LayoutData data = new LayoutData();
		data.minimumWidth = 0;
		return data;
	}

	/**
	 * Return the modify listener.
	 */
	private ModifyListener getModifyListener() {
		if (modifyListener == null) {
			modifyListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					editOccured(e);
				}
			};
		}
		return modifyListener;
	}

	/**
	 * Handles a default selection event from the text control by applying the
	 * editor value and deactivating this cell editor.
	 * 
	 * @param event
	 *            the selection event
	 * @since 3.0
	 */
	protected void handleDefaultSelection(SelectionEvent event) {
		// same with enter-key handling code in keyReleaseOccured(e);
		fireApplyEditorValue();
		deactivate();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method returns <code>true</code> if the current
	 * selection is not empty.
	 */
	public boolean isCopyEnabled() {
		if (text == null || text.getControl().isDisposed()) {
			return false;
		}
		return text.getControl().getSelectionCount() > 0;
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method returns <code>true</code> if the current
	 * selection is not empty.
	 */
	public boolean isCutEnabled() {
		if (text == null || text.getControl().isDisposed()) {
			return false;
		}
		return text.getControl().getSelectionCount() > 0;
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method returns <code>true</code> if there is a
	 * selection or if the caret is not positioned at the end of the text.
	 */
	public boolean isDeleteEnabled() {
		if (text == null || text.getControl().isDisposed()) {
			return false;
		}
		return text.getControl().getSelectionCount() > 0
				|| text.getControl().getCaretPosition() < text.getControl()
						.getCharCount();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method always returns <code>true</code>.
	 */
	public boolean isPasteEnabled() {
		if (text == null || text.getControl().isDisposed()) {
			return false;
		}
		return true;
	}

	/**
	 * Check if save all is enabled
	 * 
	 * @return true if it is
	 */
	public boolean isSaveAllEnabled() {
		if (text == null || text.getControl().isDisposed()) {
			return false;
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if this cell editor is able to perform the
	 * select all action.
	 * <p>
	 * This default implementation always returns <code>false</code>.
	 * </p>
	 * <p>
	 * Subclasses may override
	 * </p>
	 * 
	 * @return <code>true</code> if select all is possible, <code>false</code>
	 *         otherwise
	 */
	public boolean isSelectAllEnabled() {
		if (text == null || text.getControl().isDisposed()) {
			return false;
		}
		return text.getControl().getCharCount() > 0;
	}

	/**
	 * Processes a key release event that occurred in this cell editor.
	 * <p>
	 * The <code>TextCellEditor</code> implementation of this framework method
	 * ignores when the RETURN key is pressed since this is handled in
	 * <code>handleDefaultSelection</code>. An exception is made for Ctrl+Enter
	 * for multi-line texts, since a default selection event is not sent in this
	 * case.
	 * </p>
	 * 
	 * @param keyEvent
	 *            the key event
	 */
	protected void keyReleaseOccured(KeyEvent keyEvent) {
		if (keyEvent.character == '\r') { // Return key
			// Enter is handled in handleDefaultSelection.
			// Do not apply the editor value in response to an Enter key event
			// since this can be received from the IME when the intent is -not-
			// to apply the value.
			// See bug 39074 [CellEditors] [DBCS] canna input mode fires bogus
			// event from Text Control
			//
			// An exception is made for Ctrl+Enter for multi-line texts, since
			// a default selection event is not sent in this case.
			if (text != null && !text.getControl().isDisposed()
					&& (text.getControl().getStyle() & SWT.MULTI) != 0) {
				if ((keyEvent.stateMask & SWT.CTRL) != 0) {
					super.keyReleaseOccured(keyEvent);
				}
			}
			return;
		}
		super.keyReleaseOccured(keyEvent);
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method copies the current selection to the
	 * clipboard.
	 */
	public void performCopy() {
		text.getControl().copy();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method cuts the current selection to the
	 * clipboard.
	 */
	public void performCut() {
		text.getControl().cut();
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method deletes the current selection or, if there
	 * is no selection, the character next character from the current position.
	 */
	public void performDelete() {
		if (text.getControl().getSelectionCount() > 0) {
			// remove the contents of the current selection
			text.getControl().insert(""); //$NON-NLS-1$
		} else {
			// remove the next character
			int pos = text.getControl().getCaretPosition();
			if (pos < text.getControl().getCharCount()) {
				text.getControl().setSelection(pos, pos + 1);
				text.getControl().insert(""); //$NON-NLS-1$
			}
		}
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method pastes the the clipboard contents over the
	 * current selection.
	 */
	public void performPaste() {
		text.getControl().paste();
		checkSelection();
		checkDeleteable();
		checkSelectable();
	}

	/**
	 * The <code>TextCellEditor</code> implementation of this
	 * <code>CellEditor</code> method selects all of the current text.
	 */
	public void performSelectAll() {
		text.getControl().selectAll();
		checkSelection();
		checkDeleteable();
	}

	/**
	 * This implementation of
	 * {@link CellEditor#dependsOnExternalFocusListener()} returns false if the
	 * current instance's class is TextCellEditor, and true otherwise.
	 * Subclasses that hook their own focus listener should override this method
	 * and return false. See also bug 58777.
	 * 
	 * @since 3.4
	 */

	protected boolean dependsOnExternalFocusListener() {
		return getClass() != TimeCellEditor.class;
	}
}
