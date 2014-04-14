package lumina.ui.actions;

import lumina.base.model.ModelItem;
import lumina.base.model.ModelUtils;
import lumina.base.model.transfer.TransferFactory;
import lumina.ui.swt.ClipboardUtils;
import lumina.ui.swt.handlers.InterceptedHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Copies the selected items to the Clipboard.
 * <p>
 * Creates the following transfer types:
 * <ol>
 * <li>Text -- which results in the copying the items a list of names.</li>
 * <li>ModelItem -- a XML representation of the model tree.</li>
 * </ol>
 */
public final class CopyItemsToClipboardHandler extends InterceptedHandler {

	private static final String COPY_COMMAND_ID = "org.eclipse.ui.edit.copy";

	/**
	 * Translates the selection into text.<br/>
	 * Only ModelItem objects will be translated into strings.
	 * 
	 * @param selection
	 *            object array
	 * @return text selection as text
	 * @see lumina.base.model.ModelUtils#toModelItems(Object[])
	 * @see lumina.base.model.ModelUtils#toText(ModelItem[])
	 */
	protected static String asText(Object[] selection) {
		final ModelItem[] items = ModelUtils.toModelItems(selection);
		final String text = ModelUtils.toText(items);
		return text;
	}

	/**
	 * Returns the command identifier.
	 * 
	 * @return command identifier
	 * @see lumina.ui.swt.handlers.InterceptedHandler#getCommandId()
	 */
	@Override
	public String getCommandId() {
		return COPY_COMMAND_ID;
	}

	/**
	 * Copies the contents of the object into the clipboard.
	 * 
	 * @param event
	 *            event
	 * @return null
	 * @throws ExecutionException
	 *             not thrown
	 * @see lumina.ui.swt.handlers.InterceptedHandler#execute(ExecutionEvent)
	 */
	@Override
	public Object executeDefault(ExecutionEvent event)
			throws ExecutionException {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structSelection = (IStructuredSelection) selection;

			if (structSelection.size() == 0) {
				return null;
			}

			try {
				final ModelItem[] items = ModelUtils
						.asModelItems(structSelection.toArray());

				final Transfer transfer = TransferFactory.getTransferFor(items);
				final Clipboard targetClipboard = ClipboardUtils.getClipboard();
				targetClipboard.clearContents();

				if (transfer != null) {
					targetClipboard.setContents(new Object[] { asText(items),
							ModelUtils.asModelItems(items) }, new Transfer[] {
							TextTransfer.getInstance(), transfer });
				}

				// SelectionUtils.doUpdateSelectionSourceProvider(null,
				// selection);
				// reselect(selection); -- force upd propagation!
			} catch (SWTError error) {
				/*
				 * Copy to clipboard failed. This happens when another
				 * application is accessing the clipboard while we copy. Ignore
				 * the error.
				 */
			}
		}

		return null;
	}
}
